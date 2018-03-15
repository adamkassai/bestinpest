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

        if (!game.getTurn().equals("detectives"))
        {
            throw new BadRequestException("It's not your turn.");
        }

        Player player = playerRepository.findById(plan.getPlayerId())
                .orElseThrow(() -> new NotFoundException("Player", "id", id));

        planRepository.save(plan);
        DetectiveStep step = game.getDetectiveStepByRound(game.getRound());
        step.getPlans().put(player.getId(), plan);
        detectiveStepRepository.save(step);
        return gameRepository.save(game);
    }

    @GetMapping("/games/{id}/plans/{planId}/react")
    public Plan approvePlan(@PathVariable(value = "id") Long id, @PathVariable(value = "planId") Long planId, @RequestParam("playerId") Long playerId,
                            @ApiParam(value = "approve or refuse", required = true)
                            @RequestParam("reaction") String reaction) {

        Game game = gameRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Game", "id", id));

        Plan plan = planRepository.findById(planId)
                .orElseThrow(() -> new NotFoundException("Plan", "id", id));

        plan.getReactions().put(playerId, reaction);
        plan = planRepository.save(plan);
        gameService.evaluateRound(game);
        return plan;
    }

}
