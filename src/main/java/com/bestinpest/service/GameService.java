package com.bestinpest.service;

import com.bestinpest.Application;
import com.bestinpest.exception.BadRequestException;
import com.bestinpest.exception.NotFoundException;
import com.bestinpest.model.*;
import com.bestinpest.repository.CriminalStepRepository;
import com.bestinpest.repository.DetectiveStepRepository;
import com.bestinpest.repository.GameRepository;
import com.bestinpest.repository.PlayerRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
    GameRepository gameRepository;

    private static final Logger log = LoggerFactory.getLogger(Application.class);

    public void changeTurn(Game game) {
        if (game.getTurn().equals("criminal")) {
            game.setTurn("detectives");
            DetectiveStep detectiveStep = new DetectiveStep(game);
            detectiveStep.setRound(game.getRound());
            detectiveStepRepository.save(detectiveStep);
            game.getDetectiveSteps().add(detectiveStep);
        }else{
            game.setTurn("criminal");
            game.setRound(game.getRound()+1);
        }
        gameRepository.save(game);
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

                    player.setJunctionId(step.getPlans().get(player.getId()).getArrivalJunctionId());
                    playerRepository.save(player);
                }
            }

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

    public Game addCriminalStep(Game game, CriminalStep step) {

        if (!game.getTurn().equals("criminal"))
        {
            throw new BadRequestException("It's not your turn.");
        }

        Player player = playerRepository.findById(game.getCriminalId())
                .orElseThrow(() -> new NotFoundException("Player", "id", game.getCriminalId()));


        step.setGame(game);
        step.setRound(game.getRound());
        criminalStepRepository.save(step);
        game.getCriminalSteps().add(step);
        player.setJunctionId(step.getArrivalJunctionId());
        playerRepository.save(player);
        changeTurn(game);
        return gameRepository.save(game);

    }

    public void evaluateRound(Game game) {

        if (!isAllPlanApproved(game))
            return;

        if (!isValidStep(game))
        {
            //Send message
            return;
        }

        changeDetectivesJunctions(game);

        if (isCriminalCaught(game))
        {
            //Send message
            //End game
            return;
        }

        changeTurn(game);
        //Check if game hasn't finished
    }

}
