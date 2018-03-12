package com.bestinpest.model;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Entity
public class Plan {

    @Id
    @GeneratedValue
    private Long id;

    private String departureJunctionId;
    private String arrivalJunctionId;
    private String routeId;
    private String playerId;

    @ElementCollection
    private Set<String> approvers = new HashSet();

    public Plan() {
    }

    public Plan(String departureJunctionId, String arrivalJunctionId, String routeId, String playerId) {
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

    public String getRouteId() {
        return routeId;
    }

    public void setRouteId(String routeId) {
        this.routeId = routeId;
    }

    public String getPlayerId() {
        return playerId;
    }

    public void setPlayerId(String playerId) {
        this.playerId = playerId;
    }

    public Set<String> getApprovers() {
        return approvers;
    }

    public void setApprovers(Set<String> approvers) {
        this.approvers = approvers;
    }
}
