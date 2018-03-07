package com.bestinpest.model;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;

import javax.persistence.*;

@Entity
@JsonIdentityInfo(generator=ObjectIdGenerators.PropertyGenerator.class, property="id")
public class Route {

    @Id
    @GeneratedValue
    private Long id;


    @ManyToOne
    @JoinColumn(name="departure")
    private Stop departure;

    @ManyToOne
    @JoinColumn(name="arrival")
    private Stop arrival;

    private String type;
    private String relationId;
    private String relationName;
    private String headsign;


    public Route() {
    }

    public Route(Stop departure, Stop arrival, String type, String relationId, String relationName, String headsign) {
        this.departure = departure;
        this.arrival = arrival;
        this.type = type;
        this.relationId = relationId;
        this.relationName = relationName;
        this.headsign = headsign;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Stop getDeparture() {
        return departure;
    }

    public void setDeparture(Stop departure) {
        this.departure = departure;
    }

    public Stop getArrival() {
        return arrival;
    }

    public void setArrival(Stop arrival) {
        this.arrival = arrival;
    }

    public String getRelationId() {
        return relationId;
    }

    public void setRelationId(String relationId) {
        this.relationId = relationId;
    }

    public String getRelationName() {
        return relationName;
    }

    public void setRelationName(String relationName) {
        this.relationName = relationName;
    }

    public String getHeadsign() {
        return headsign;
    }

    public void setHeadsign(String headsign) {
        this.headsign = headsign;
    }
}
