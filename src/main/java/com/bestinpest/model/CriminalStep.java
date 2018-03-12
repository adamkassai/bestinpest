package com.bestinpest.model;

import com.fasterxml.jackson.annotation.JsonIgnore;

import javax.persistence.*;

@Entity
public class CriminalStep {

    @Id
    @GeneratedValue
    private Long id;

    @ManyToOne
    @JoinColumn(name="gameId")
    @JsonIgnore
    private Game game;

    private String departureJunctionId;
    private String arrivalJunctionId;
    private String routeId;

    public CriminalStep() {
    }

    public CriminalStep(Game game, String departureJunctionId, String arrivalJunctionId, String routeId) {
        this.game = game;
        this.departureJunctionId = departureJunctionId;
        this.arrivalJunctionId = arrivalJunctionId;
        this.routeId = routeId;
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

    public String getDepartureJunctionId() {
        return departureJunctionId;
    }

    public void setDepartureJunctionId(String departureJunctionId) {
        this.departureJunctionId = departureJunctionId;
    }

    public String getArrivalJunctionId() {
        return arrivalJunctionId;
    }

    public void setArrivalJunctionId(String arrivalJunctionId) {
        this.arrivalJunctionId = arrivalJunctionId;
    }

    public String getRouteId() {
        return routeId;
    }

    public void setRouteId(String routeId) {
        this.routeId = routeId;
    }
}
