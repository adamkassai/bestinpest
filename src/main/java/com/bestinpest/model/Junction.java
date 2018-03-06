package com.bestinpest.model;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import java.util.List;

@Entity
public class Junction {

    @Id
    private String id;

    private String name;

    @OneToMany(mappedBy="junction")
    private List<Stop> stops;

    public Junction() {
    }


}
