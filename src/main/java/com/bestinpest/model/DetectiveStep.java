package com.bestinpest.model;

import com.fasterxml.jackson.annotation.JsonIgnore;

import javax.persistence.*;
import java.util.HashMap;
import java.util.Map;

@Entity
public class DetectiveStep {

    @Id
    @GeneratedValue
    private Long id;

    @ManyToOne
    @JoinColumn(name="gameId")
    @JsonIgnore
    private Game game;

    private int round;

    @ElementCollection
    @MapKeyColumn(name="playerId")
    private Map<Long, Plan> plans = new HashMap<>();

    public DetectiveStep() {
    }

    public DetectiveStep(Game game) {
        this.game = game;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Game getGame() {
        return game;
    }

    public void setGame(Game game) {
        this.game = game;
    }

    public Map<Long, Plan> getPlans() {
        return plans;
    }

    public void setPlans(Map<Long, Plan> plans) {
        this.plans = plans;
    }

    public int getRound() {
        return round;
    }

    public void setRound(int round) {
        this.round = round;
    }
}
