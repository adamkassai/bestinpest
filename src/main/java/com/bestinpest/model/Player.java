package com.bestinpest.model;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;

import javax.persistence.*;

@Entity
public class Player {

    @Id
    @GeneratedValue
    private Long id;

    @ManyToOne
    @JoinColumn(name="lobbyId")
    @JsonIgnore
    private Lobby lobby;

    @ManyToOne
    @JoinColumn(name="gameId")
    @JsonIgnore
    private Game game;

    private String junctionId;

    private String name;

    public Player() {
    }

    public Player(String name) {
        this.name = name;
    }

    public Player(String name, String junctionId) {
        this.junctionId = junctionId;
        this.name = name;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setLobby(Lobby lobby) {
        this.lobby = lobby;
    }

    public String getJunctionId() {
        return junctionId;
    }

    public void setJunctionId(String junctionId) {
        this.junctionId = junctionId;
    }

    public void setGame(Game game) {
        this.game = game;
    }
}
