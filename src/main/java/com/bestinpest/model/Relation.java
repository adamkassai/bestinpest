package com.bestinpest.model;

import com.fasterxml.jackson.annotation.JsonIgnore;

import javax.persistence.*;

@Entity
public class Relation {

    @Id
    @GeneratedValue
    private Long id;
    private String relationId;
    private String relationName;
    private String headsign;
    private String textColor;
    private String backgroundColor;

    @ManyToOne
    @JoinColumn(name="routeId")
    @JsonIgnore
    private Route route;

    public Relation() {
    }

    public Relation(String relationId, String relationName, String headsign, Route route) {
        this.relationId = relationId;
        this.relationName = relationName;
        this.headsign = headsign;
        this.route = route;
    }

    public Relation(String relationId, String relationName, String headsign, String textColor, String backgroundColor, Route route) {
        this.relationId = relationId;
        this.relationName = relationName;
        this.headsign = headsign;
        this.textColor = textColor;
        this.backgroundColor = backgroundColor;
        this.route = route;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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

    public Route getRoute() {
        return route;
    }

    public void setRoute(Route route) {
        this.route = route;
    }

    public String getTextColor() {
        return textColor;
    }

    public void setTextColor(String textColor) {
        this.textColor = textColor;
    }

    public String getBackgroundColor() {
        return backgroundColor;
    }

    public void setBackgroundColor(String backgroundColor) {
        this.backgroundColor = backgroundColor;
    }
}
