package com.bestinpest.model;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
public class Game {

    @Id
    private Long id;

    private Long criminalId;

    private String turn;

    private int round;

    @OneToMany(mappedBy="game")
    private List<Player> players = new ArrayList<>();

    @OneToMany(mappedBy="game", cascade = CascadeType.ALL)
    private List<CriminalStep> criminalSteps = new ArrayList<>();

    @OneToMany(mappedBy="game", cascade = CascadeType.ALL)
    private List<DetectiveStep> detectiveSteps = new ArrayList<>();

    public Game() {
    }

    public Game(Long id, Long criminalId) {
        this.id = id;
        this.criminalId = criminalId;
        this.turn="criminal";
        this.round=1;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getCriminalId() {
        return criminalId;
    }

    public void setCriminalId(Long criminalId) {
        this.criminalId = criminalId;
    }

    public List<Player> getPlayers() {
        return players;
    }

    public void setPlayers(List<Player> players) {
        this.players = players;
    }

    public List<CriminalStep> getCriminalSteps() {
        return criminalSteps;
    }

    public void setCriminalSteps(List<CriminalStep> criminalSteps) {
        this.criminalSteps = criminalSteps;
    }

    public List<DetectiveStep> getDetectiveSteps() {
        return detectiveSteps;
    }

    public void setDetectiveSteps(List<DetectiveStep> detectiveSteps) {
        this.detectiveSteps = detectiveSteps;
    }

    public String getTurn() {
        return turn;
    }

    public void setTurn(String turn) {
        this.turn = turn;
    }

    public int getRound() {
        return round;
    }

    public void setRound(int round) {
        this.round = round;
    }

    public DetectiveStep getDetectiveStepByRound(int round)
    {
        for (DetectiveStep step : detectiveSteps)
        {
            if (step.getRound()==round)
                return step;
        }
        return new DetectiveStep();
    }
}
