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

    private int round;

    private String departureJunctionId;
    private String departureJunctionName;
    private String arrivalJunctionId;
    private String arrivalJunctionName;
    private Long routeId;
    private Boolean visible=false;
    private String type;

    public CriminalStep() {
    }

    public CriminalStep(Game game, String departureJunctionId, String arrivalJunctionId, Long routeId) {
        this.game = game;
        this.departureJunctionId = departureJunctionId;
        this.arrivalJunctionId = arrivalJunctionId;
        this.routeId = routeId;
        this.visible=false;
    }

    public CriminalStep(Game game, String departureJunctionId, String departureJunctionName, String arrivalJunctionId, String arrivalJunctionName, Long routeId) {
        this.game = game;
        this.departureJunctionId = departureJunctionId;
        this.departureJunctionName = departureJunctionName;
        this.arrivalJunctionId = arrivalJunctionId;
        this.arrivalJunctionName = arrivalJunctionName;
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

    public Long getRouteId() {
        return routeId;
    }

    public void setRouteId(Long routeId) {
        this.routeId = routeId;
    }

    public int getRound() {
        return round;
    }

    public void setRound(int round) {
        this.round = round;
    }

    public Boolean getVisible() {
        return visible;
    }

    public void setVisible(Boolean visible) {
        this.visible = visible;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getDepartureJunctionName() {
        return departureJunctionName;
    }

    public void setDepartureJunctionName(String departureJunctionName) {
        this.departureJunctionName = departureJunctionName;
    }

    public String getArrivalJunctionName() {
        return arrivalJunctionName;
    }

    public void setArrivalJunctionName(String arrivalJunctionName) {
        this.arrivalJunctionName = arrivalJunctionName;
    }
}
