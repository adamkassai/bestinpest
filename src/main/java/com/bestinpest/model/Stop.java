package com.bestinpest.model;

import javax.persistence.*;
import java.util.List;

@Entity
public class Stop {

    @Id
    private String id;
    private String name;
    private double lat;
    private double lon;

    @ManyToOne
    @JoinColumn(name="junctionId")
    private Junction junction;

    @OneToMany(mappedBy="departure")
    private List<Route> routesFromHere;

    public Stop() {
    }


}
