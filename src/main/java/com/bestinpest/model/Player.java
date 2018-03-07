package com.bestinpest.model;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;

import javax.persistence.*;

@Entity
@JsonIdentityInfo(generator=ObjectIdGenerators.PropertyGenerator.class, property="id")
public class Player {

    @Id
    @GeneratedValue
    private Long id;

    @ManyToOne
    @JoinColumn(name="lobbyId")
    private Lobby lobby;

    private String name;

    public Player() {
    }

    public Player(String name) {
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
}
