package com.bestinpest.service;

import com.bestinpest.Application;
import com.bestinpest.config.GameConfig;
import com.bestinpest.exception.BadRequestException;
import com.bestinpest.exception.NotFoundException;
import com.bestinpest.model.*;
import com.bestinpest.repository.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
    RouteRepository routeRepository;

    @Autowired
    GameConfig gameConfig;

    private static final Logger log = LoggerFactory.getLogger(Application.class);

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
                    if (!reactor.getId().equals(game.getCriminalId()) && !reactor.getId().equals(player.getId()))
                    {
                        if (!plan.getReactions().containsKey(reactor.getId()) || !plan.getReactions().get(reactor.getId()).equals("approve")) {
                            return false;
                        }
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

                    player.setJunctionId(plan.getArrivalJunctionId());
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

        planRepository.save(plan);
        DetectiveStep step = game.getDetectiveStepByRound(game.getRound());
        step.getPlans().put(player.getId(), plan);
        detectiveStepRepository.save(step);

        game = gameRepository.save(game);

        RabbitMessage m = new RabbitMessage(player.getName()+" made a plan.", "detective-plan", game);
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

        step.setGame(game);
        step.setRound(game.getRound());
        if (gameConfig.getVisibleCriminalRounds().contains(step.getRound())) { step.setVisible(true); }
        criminalStepRepository.save(step);
        game.getCriminalSteps().add(step);
        player.setJunctionId(step.getArrivalJunctionId());
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
            //End game
            return;
        }

        changeTurn(game);
    }

}
