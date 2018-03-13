package com.bestinpest.model;

import javax.persistence.*;
import java.util.*;

@Entity
public class Plan {

    @Id
    @GeneratedValue
    private Long id;

    private String departureJunctionId;
    private String arrivalJunctionId;
    private Long routeId;
    private Long playerId;

    @ElementCollection
    @MapKeyColumn(name="playerId")
    private Map<Long, String> reactions = new HashMap<>();

    public Plan() {
    }

    public Plan(String departureJunctionId, String arrivalJunctionId, Long routeId, Long playerId) {
        this.departureJunctionId = departureJunctionId;
        this.arrivalJunctionId = arrivalJunctionId;
        this.routeId = routeId;
        this.playerId = playerId;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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

    public Long getPlayerId() {
        return playerId;
    }

    public void setPlayerId(Long playerId) {
        this.playerId = playerId;
    }

    public Map<Long, String> getReactions() {
        return reactions;
    }

    public void setReactions(Map<Long, String> reactions) {
        this.reactions = reactions;
    }
}
