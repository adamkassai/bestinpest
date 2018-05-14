package com.bestinpest.model;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import java.util.ArrayList;
import java.util.List;

@Entity
public class Junction {

    @Id
    private String id;

    private String name;

    @OneToMany(mappedBy="junction", fetch= FetchType.EAGER)
    private List<Stop> stops;

    public Junction() {
    }

    public Junction(String id, String name) {
        this.id = id;
        this.name = name;
        this.stops = new ArrayList<>();
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<Stop> getStops() {
        return stops;
    }

    public void setStops(List<Stop> stops) {
        this.stops = stops;
    }
}
