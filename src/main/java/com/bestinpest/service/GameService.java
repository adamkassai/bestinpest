package com.bestinpest.service;

import com.bestinpest.config.GameConfig;
import com.bestinpest.exception.BadRequestException;
import com.bestinpest.exception.NotFoundException;
import com.bestinpest.model.*;
import com.bestinpest.repository.*;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class GameService {

    @Autowired
    CriminalStepRepository criminalStepRepository;

    @Autowired
    DetectiveStepRepository detectiveStepRepository;

    @Autowired
    PlayerRepository playerRepository;

    @Autowired
    RabbitTemplate rabbitTemplate;

    @Autowired
    GameRepository gameRepository;

    @Autowired
    PlanRepository planRepository;

    @Autowired
    JunctionRepository junctionRepository;

    @Autowired
    RecommendationRepository recommendationRepository;

    @Autowired
    RouteRepository routeRepository;

    @Autowired
    GameConfig gameConfig;


    public void changeTurn(Game game) {
        if (game.getTurn().equals("criminal")) {
            game.setTurn("detectives");
            DetectiveStep detectiveStep = new DetectiveStep(game);
            detectiveStep.setRound(game.getRound());
            detectiveStepRepository.save(detectiveStep);
            game.getDetectiveSteps().add(detectiveStep);
            game = gameRepository.save(game);

            RabbitMessage m = new RabbitMessage("Detectives' turn.", "turn-changed", game);
            rabbitTemplate.convertAndSend("bip-exchange", "game:" + game.getId(), m.toString());

        }else if (game.getRound()<gameConfig.getMaxRoundNumber()){
            game.setTurn("criminal");
            game.setRound(game.getRound()+1);

            game = gameRepository.save(game);

            RabbitMessage m = new RabbitMessage("Criminal's turn.", "turn-changed", game);
            rabbitTemplate.convertAndSend("bip-exchange", "game:" + game.getId(), m.toString());
        }else{
            game.setTurn("Criminal won");
            gameRepository.save(game);
            RabbitMessage m = new RabbitMessage("Criminal is not caught, the game ended.", "game-ended", game);
            rabbitTemplate.convertAndSend("bip-exchange", "game:" + game.getId(), m.toString());
        }

    }

    public boolean isAllPlanApproved(Game game) {
        DetectiveStep step = game.getDetectiveStepByRound(game.getRound());

        for (int i=0; i<game.getPlayers().size(); i++)
        {
            Player player = game.getPlayers().get(i);

            if (!player.getId().equals(game.getCriminalId()) && !step.getPlans().containsKey(player.getId())) {
                return false;
            }

            if (!player.getId().equals(game.getCriminalId())) {

                Plan plan = step.getPlans().get(player.getId());

                for (int j=0; j<game.getPlayers().size(); j++)
                {
                    Player reactor = game.getPlayers().get(j);
                    if (!reactor.getId().equals(game.getCriminalId()) && !reactor.getId().equals(player.getId()) &&
                            (!plan.getReactions().containsKey(reactor.getId()) || !plan.getReactions().get(reactor.getId()).equals("approve")))
                    {
                            return false;
                    }
                }
            }
        }

        return true;
    }

    public void changeDetectivesJunctions(Game game) {

        if (isAllPlanApproved(game)) {
            DetectiveStep step = game.getDetectiveStepByRound(game.getRound());

            for (Player player: game.getPlayers())
            {
                if (!player.getId().equals(game.getCriminalId())) {

                    Plan plan = step.getPlans().get(player.getId());

                    Junction arrivalJunction = junctionRepository.findById(plan.getArrivalJunctionId())
                            .orElseThrow(() -> new NotFoundException("Junction", "id", plan.getArrivalJunctionId()));

                    player.setJunctionId(arrivalJunction.getId());
                    player.setJunctionName(arrivalJunction.getName());
                    player.setReady(false);

                    Route route = routeRepository.findById(plan.getRouteId())
                            .orElseThrow(() -> new NotFoundException("Route", "id", plan.getRouteId()));

                    player.getTickets().put(getTicketType(route.getType()), player.getTickets().get(getTicketType(route.getType()))-1);

                    playerRepository.save(player);
                }
            }

            RabbitMessage m = new RabbitMessage("Detectives took a step.", "detectives-step", game);
            rabbitTemplate.convertAndSend("bip-exchange", "game:" + game.getId(), m.toString());

        }

    }

    public boolean isValidStep(Game game) {
        DetectiveStep step = game.getDetectiveStepByRound(game.getRound());
        List<String> arrivals = new ArrayList<>();

        for (Player player: game.getPlayers())
        {
            if (!player.getId().equals(game.getCriminalId())) {

                if (arrivals.contains(step.getPlans().get(player.getId()).getArrivalJunctionId()))
                    return false;

                arrivals.add(step.getPlans().get(player.getId()).getArrivalJunctionId());

            }
        }
        return true;
    }

    public boolean isCriminalCaught(Game game) {

        Player criminal = playerRepository.findById(game.getCriminalId())
                .orElseThrow(() -> new NotFoundException("Player", "id", game.getCriminalId()));

        for(Player player: game.getPlayers()) {

            if (!player.equals(criminal) && player.getJunctionId().equals(criminal.getJunctionId()))
                return true;

        }
        return false;
    }

    public String getTicketType(String type) {

        if (type.equals("BUS") || type.equals("TROLLEYBUS")) {
            return "BUS-TROLLEY";
        }

        return type;
    }

    public Game addDetectivePlan(Game game, Plan plan) {

        if (!game.getTurn().equals("detectives"))
        {
            throw new BadRequestException("It's not your turn.");
        }

        Player player = playerRepository.findById(plan.getPlayerId())
                .orElseThrow(() -> new NotFoundException("Player", "id", plan.getPlayerId()));

        Route route = routeRepository.findById(plan.getRouteId())
                .orElseThrow(() -> new NotFoundException("Route", "id", plan.getRouteId()));

        if (player.getTickets().get(getTicketType(route.getType()))==0) {
            throw new BadRequestException("You don't have ticket for this route.");
        }

        Junction departureJunction = junctionRepository.findById(plan.getDepartureJunctionId())
                .orElseThrow(() -> new NotFoundException("departureJunction", "id", plan.getDepartureJunctionId()));

        Junction arrivalJunction = junctionRepository.findById(plan.getArrivalJunctionId())
                .orElseThrow(() -> new NotFoundException("arrivalJunction", "id", plan.getArrivalJunctionId()));

        plan.setDepartureJunctionName(departureJunction.getName());
        plan.setArrivalJunctionName(arrivalJunction.getName());

        planRepository.save(plan);
        DetectiveStep step = game.getDetectiveStepByRound(game.getRound());
        step.getPlans().put(player.getId(), plan);
        detectiveStepRepository.save(step);

        game = gameRepository.save(game);

        RabbitMessage m = new RabbitMessage(player.getName()+" made a plan.", "detective-plan", game);
        rabbitTemplate.convertAndSend("bip-exchange", "game:" + game.getId(), m.toString());

        return game;
    }

    public Game addRecommendation(Game game, Recommendation recommendation) {

        Player player = playerRepository.findById(recommendation.getSenderPlayerId())
                .orElseThrow(() -> new NotFoundException("Player", "id", recommendation.getSenderPlayerId()));

        Junction departureJunction = junctionRepository.findById(recommendation.getDepartureJunctionId())
                .orElseThrow(() -> new NotFoundException("departureJunction", "id", recommendation.getDepartureJunctionId()));

        Junction arrivalJunction = junctionRepository.findById(recommendation.getArrivalJunctionId())
                .orElseThrow(() -> new NotFoundException("arrivalJunction", "id", recommendation.getArrivalJunctionId()));

        recommendation.setDepartureJunctionName(departureJunction.getName());
        recommendation.setArrivalJunctionName(arrivalJunction.getName());

        DetectiveStep step = game.getDetectiveStepByRound(game.getRound());
        recommendation.setStep(step);
        recommendationRepository.save(recommendation);
        step.getRecommendations().add(recommendation);
        detectiveStepRepository.save(step);
        game = gameRepository.save(game);

        RabbitMessage m = new RabbitMessage(player.getName()+" sent a recommendation", "recommendation-sent", game);
        rabbitTemplate.convertAndSend("bip-exchange", "game:" + game.getId(), m.toString());

        return game;
    }

    public Game addCriminalStep(Game game, CriminalStep step) {

        if (!game.getTurn().equals("criminal"))
        {
            throw new BadRequestException("It's not your turn.");
        }

        Player player = playerRepository.findById(game.getCriminalId())
                .orElseThrow(() -> new NotFoundException("Player", "id", game.getCriminalId()));

        Route route = routeRepository.findById(step.getRouteId())
                .orElseThrow(() -> new NotFoundException("Route", "id", step.getRouteId()));

        if (step.getType()==null) {
            step.setType(route.getType());
        }

        Junction departureJunction = junctionRepository.findById(step.getDepartureJunctionId())
                .orElseThrow(() -> new NotFoundException("departureJunction", "id", step.getDepartureJunctionId()));

        Junction arrivalJunction = junctionRepository.findById(step.getArrivalJunctionId())
                .orElseThrow(() -> new NotFoundException("arrivalJunction", "id", step.getArrivalJunctionId()));

        step.setDepartureJunctionName(departureJunction.getName());
        step.setArrivalJunctionName(arrivalJunction.getName());

        step.setGame(game);
        step.setRound(game.getRound());
        if (gameConfig.getVisibleCriminalRounds().contains(step.getRound())) { step.setVisible(true); }
        criminalStepRepository.save(step);
        game.getCriminalSteps().add(step);
        player.setJunctionId(arrivalJunction.getId());
        player.setJunctionName(arrivalJunction.getName());
        player.setReady(false);
        playerRepository.save(player);

        RabbitMessage m = new RabbitMessage("Criminal took a step.", "criminal-step", game);
        rabbitTemplate.convertAndSend("bip-exchange", "game:" + game.getId(), m.toString());

        changeTurn(game);
        return gameRepository.save(game);

    }

    public void evaluateRound(Game game) {

        if (isAllPlanApproved(game)) {
            RabbitMessage m = new RabbitMessage("Plan is approved by everyone.", "plan-approved", game);
            rabbitTemplate.convertAndSend("bip-exchange", "game:" + game.getId(), m.toString());
        }else {
            return;
        }

        if (!isValidStep(game))
        {
            RabbitMessage m = new RabbitMessage("People can't step to the same junction.", "invalid-step", game);
            rabbitTemplate.convertAndSend("bip-exchange", "game:" + game.getId(), m.toString());
            return;
        }

        changeDetectivesJunctions(game);

        if (isCriminalCaught(game))
        {
            game.setTurn("Detectives won");
            gameRepository.save(game);
            RabbitMessage m = new RabbitMessage("Criminal is caught.", "criminal-caught", game);
            rabbitTemplate.convertAndSend("bip-exchange", "game:" + game.getId(), m.toString());

            deleteGame(game);

            return;
        }

        changeTurn(game);
    }


    public Game deleteGame(Game game) {

        for(CriminalStep step: game.getCriminalSteps())
        {
            step.setGame(null);
            criminalStepRepository.save(step);
        }
        criminalStepRepository.delete(game.getCriminalSteps());
        game.getCriminalSteps().clear();

        for(DetectiveStep step: game.getDetectiveSteps())
        {
            for (Player player: game.getPlayers())
            {
                if (step.getPlans().containsKey(player.getId())) {
                    step.getPlans().get(player.getId()).getReactions().clear();
                }
            }

            step.getPlans().clear();

            for (Recommendation recommendation : step.getRecommendations())
            {
                recommendation.setStep(null);
                recommendationRepository.save(recommendation);
            }
            recommendationRepository.delete(step.getRecommendations());
            step.getRecommendations().clear();

            step.setGame(null);
            detectiveStepRepository.save(step);
        }
        detectiveStepRepository.delete(game.getDetectiveSteps());
        game.getDetectiveSteps().clear();

        for (Player player: game.getPlayers())
        {
            player.getTickets().clear();
            player.setGame(null);
            playerRepository.save(player);
        }
        playerRepository.delete(game.getPlayers());
        game.getPlayers().clear();

        game = gameRepository.save(game);
        gameRepository.delete(game);

        RabbitMessage m = new RabbitMessage("Game is removed.", "game-removed", game);
        rabbitTemplate.convertAndSend("bip-exchange", "game:" + game.getId(), m.toString());

        return game;
    }


}
