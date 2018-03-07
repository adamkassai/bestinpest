package com.bestinpest.controller;

import com.bestinpest.model.Junction;
import com.bestinpest.model.Route;
import com.bestinpest.repository.JunctionRepository;
import com.bestinpest.repository.RouteRepository;
import com.bestinpest.repository.StopRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class RouteController {

    @Autowired
    StopRepository stopRepository;

    @Autowired
    JunctionRepository junctionRepository;

    @Autowired
    RouteRepository routeRepository;

    @GetMapping("/junctions")
    public List<Junction> junctions() {
        return junctionRepository.findAll();
    }

    @GetMapping("/routes")
    public List<Route> routes() {
        return routeRepository.findAll();
    }

}
