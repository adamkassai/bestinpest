package com.bestinpest.model;

import com.bestinpest.Application;
import com.bestinpest.service.RouteService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.client.RestTemplate;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
public class Route {

    @Transient
    private static final Logger log = LoggerFactory.getLogger(Application.class);

    @Id
    @GeneratedValue
    private Long id;

    @ManyToOne
    @JoinColumn(name="departure")
    private Stop departure;

    @ManyToOne
    @JoinColumn(name="arrival")
    private Stop arrival;

    @OneToMany(mappedBy="route", cascade = CascadeType.ALL)
    private List<Relation> relations = new ArrayList<>();

    private String type;

    public Route() {
    }

    public Route(Stop departure, Stop arrival, String type) {
        this.departure = departure;
        this.arrival = arrival;
        this.type = type;
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

    public List<Relation> getRelations() {
        return relations;
    }

    public void setRelations(List<Relation> relations) {
        this.relations = relations;
    }

    public boolean containsRelation(String relationId){
        return relations.stream().filter(o -> o.getRelationId().equals(relationId)).findFirst().isPresent();
    }

}
