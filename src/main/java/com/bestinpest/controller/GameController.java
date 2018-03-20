package com.bestinpest.controller;

import com.bestinpest.exception.BadRequestException;
import com.bestinpest.exception.NotFoundException;
import com.bestinpest.model.*;
import com.bestinpest.repository.*;
import com.bestinpest.service.GameService;
import com.bestinpest.service.RouteService;
import io.swagger.annotations.ApiParam;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

@RestController
public class GameController {

    @Autowired
    GameRepository gameRepository;

    @Autowired
    GameService gameService;

    @Autowired
    PlayerRepository playerRepository;

    @Autowired
    JunctionRepository junctionRepository;

    @Autowired
    RouteService routeService;

    @Autowired
    DetectiveStepRepository detectiveStepRepository;

    @Autowired
    PlanRepository planRepository;

    @Autowired
    RecommendationRepository recommendationRepository;

    @Autowired
    RabbitTemplate rabbitTemplate;

    @GetMapping("/games/{id}")
    public Game getGameById(@PathVariable(value = "id") Long id) {
        return gameRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Game", "id", id));
    }

    @GetMapping("/players/{id}/available-junctions")
    public List<Junction> getAvailableJunctions(@PathVariable(value = "id") Long id) {

        Player player = playerRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Player", "id", id));

        Junction junction = junctionRepository.findById(player.getJunctionId())
                .orElseThrow(() -> new NotFoundException("Junction", "id", player.getJunctionId()));

        return routeService.getJunctionsFromJunction(junction);

    }

    @PostMapping("/games/{id}/criminal-step")
    public Game addCriminalStep(@PathVariable(value = "id") Long id, @Valid @RequestBody CriminalStep step) {

        Game game = gameRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Game", "id", id));

        return gameService.addCriminalStep(game, step);
    }

    @PostMapping("/games/{id}/detective-plan")
    public Game addPlan(@PathVariable(value = "id") Long id, @Valid @RequestBody Plan plan) {

        Game game = gameRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Game", "id", id));

        return gameService.addDetectivePlan(game, plan);
    }

    @PostMapping("/games/{id}/recommendation")
    public Game addRecommendation(@PathVariable(value = "id") Long id, @Valid @RequestBody Recommendation recommendation) {

        Game game = gameRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Game", "id", id));

        return gameService.addRecommendation(game, recommendation);
    }

    @PostMapping("/games/{id}/plans/{planId}/react")
    public Plan approvePlan(@PathVariable(value = "id") Long id, @PathVariable(value = "planId") Long planId, @RequestParam("playerId") Long playerId,
                            @ApiParam(value = "approve or refuse", required = true)
                            @RequestParam("reaction") String reaction) {

        Game game = gameRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Game", "id", id));

        Plan plan = planRepository.findById(planId)
                .orElseThrow(() -> new NotFoundException("Plan", "id", id));

        plan.getReactions().put(playerId, reaction);
        plan = planRepository.save(plan);

        RabbitMessage m = new RabbitMessage(playerId+" reacted to a plan.", "plan-reaction", game);
        rabbitTemplate.convertAndSend("bip-exchange", "game:" + game.getId(), m.toString());

        gameService.evaluateRound(game);
        return plan;
    }

    @DeleteMapping("/games/{id}/recommendations/{recommendationId}")
    public Game deleteRecommendation(@PathVariable(value = "id") Long id, @PathVariable(value = "recommendationId") Long recommendationId) {

        Game game = gameRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Game", "id", id));

        Recommendation recommendation = recommendationRepository.findById(recommendationId)
                .orElseThrow(() -> new NotFoundException("Plan", "id", id));

        DetectiveStep step = game.getDetectiveStepByRound(game.getRound());

        step.getRecommendations().remove(recommendation);
        detectiveStepRepository.save(step);
        recommendationRepository.delete(recommendation);
        game = gameRepository.save(game);

        RabbitMessage m = new RabbitMessage(recommendationId+" recommendation is removed.", "recommendation-removed", game);
        rabbitTemplate.convertAndSend("bip-exchange", "game:" + game.getId(), m.toString());

        return game;
    }

    @PostMapping("/games/{id}/players/{playerId}/ready")
    public Player setPlayerReady(@PathVariable(value = "id") Long id, @PathVariable(value = "playerId") Long playerId) {

        Game game = gameRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Game", "id", id));

        Player player = playerRepository.findById(playerId)
                .orElseThrow(() -> new NotFoundException("Player", "id", playerId));

        if (player.getReady()) {
            player.setReady(false);
        } else {
            player.setReady(true);
        }
        player = playerRepository.save(player);

        RabbitMessage m = new RabbitMessage(player.getName() + " has reached his new junction.", "player-ready", game);
        rabbitTemplate.convertAndSend("bip-exchange", "game:" + game.getId(), m.toString());

        return player;
    }


    @DeleteMapping("/games/{id}")
    public Game deleteGame(@PathVariable(value = "id") Long id) {

        Game game = gameRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Game", "id", id));

        return gameService.deleteGame(game);
    }


}
