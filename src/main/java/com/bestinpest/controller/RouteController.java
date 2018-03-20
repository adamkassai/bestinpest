package com.bestinpest.controller;

import com.bestinpest.exception.NotFoundException;
import com.bestinpest.model.Junction;
import com.bestinpest.model.Route;
import com.bestinpest.model.Trip;
import com.bestinpest.repository.JunctionRepository;
import com.bestinpest.repository.RouteRepository;
import com.bestinpest.repository.StopRepository;
import com.bestinpest.service.RouteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
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

    @Autowired
    RouteService routeService;

    @GetMapping("/junctions")
    public List<Junction> junctions() {
        return junctionRepository.findAll();
    }

    @GetMapping("/routes")
    public List<Route> routes() {
        return routeRepository.findAll();
    }

    @GetMapping("/routes-between")
    public List<Route> getRoutesBetween(@RequestParam("departure") String departureId, @RequestParam("arrival") String arrivalId) {
        return routeService.getRoutesBetween(departureId, arrivalId);
    }

    @GetMapping("/routes/{id}")
    public Route getRouteById(@PathVariable(value = "id") Long id) {
        return routeRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Route", "id", id));
    }

    @GetMapping("/junctions/{id}")
    public Junction getJunctionById(@PathVariable(value = "id") String id) {
        return junctionRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Junction", "id", id));
    }

    @GetMapping("/routes/{id}/schedule")
    public List<Trip> getSchedule(@PathVariable(value = "id") Long id) {
        Route route = routeRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Route", "id", id));

        return routeService.getSchedule(route);
    }

}
