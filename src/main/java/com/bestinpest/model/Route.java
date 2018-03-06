package com.bestinpest.model;

import javax.persistence.*;

@Entity
public class Route {

    @Id
    @GeneratedValue
    private Long id;

    private String type;

    @ManyToOne
    @JoinColumn(name="departure")
    private Stop departure;

    @ManyToOne
    @JoinColumn(name="arrival")
    private Stop arrival;


    public Route() {
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


}
