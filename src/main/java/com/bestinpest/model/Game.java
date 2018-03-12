package com.bestinpest.model;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
public class Game {

    @Id
    @GeneratedValue
    private Long id;

    @ManyToOne
    @JoinColumn(name="criminalId")
    private Player criminal;

    @OneToMany(mappedBy="lobby")
    private List<Player> players = new ArrayList<>();

    @OneToMany(mappedBy="game")
    private List<CriminalStep> criminalSteps = new ArrayList<>();

    @OneToMany(mappedBy="game")
    private List<DetectiveStep> detectiveSteps = new ArrayList<>();

    public Game() {
    }

    public Game(Long id, Player criminal, List<Player> players) {
        this.id = id;
        this.criminal = criminal;
        this.players = players;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Player getCriminal() {
        return criminal;
    }

    public void setCriminal(Player criminal) {
        this.criminal = criminal;
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
}
