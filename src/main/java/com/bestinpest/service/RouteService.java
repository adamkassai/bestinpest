package com.bestinpest.service;

import com.bestinpest.model.*;
import com.bestinpest.repository.JunctionRepository;
import com.bestinpest.repository.RouteRepository;
import com.bestinpest.repository.StopRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class RouteService {

    @Autowired
    StopRepository stopRepository;

    @Autowired
    JunctionRepository junctionRepository;

    @Autowired
    RouteRepository routeRepository;

    public List<Junction> getFreeJunctionsNearby(double lat, double lon, List<Player> players)
    {
        List<Junction> junctions = junctionRepository.findAll();
        List<Junction> reservedJunctions = new ArrayList<>();
        List<Junction> junctionsInOneStepRadius = new ArrayList<>();
        List<Junction> junctionsInTwoStepRadius = new ArrayList<>();

        for (Player player: players)
        {

            Optional<Junction> reservedJunction = junctionRepository.findById(player.getJunctionId());

            if (reservedJunction.isPresent()) {
                reservedJunctions.add(reservedJunction.get());
                junctionsInOneStepRadius.addAll(getJunctionsFromJunction(reservedJunction.get()));
                junctionsInOneStepRadius.addAll(getJunctionsToJunction(reservedJunction.get()));
            }
        }

        for (Junction junction: junctionsInOneStepRadius)
        {
            junctionsInTwoStepRadius.addAll(getJunctionsFromJunction(junction));
            junctionsInTwoStepRadius.addAll(getJunctionsToJunction(junction));
        }

        junctions.removeAll(reservedJunctions);
        junctions.removeAll(junctionsInOneStepRadius);
        junctions.removeAll(junctionsInTwoStepRadius);

        return junctions;
    }

    public List<Junction> getJunctionsFromJunction(Junction junction)
    {
        List<Stop> stops = junction.getStops();
        List<Route> routes = new ArrayList<>();
        List<Junction> arrivals = new ArrayList<>();

        for (Stop stop: stops)
        {
            routes.addAll(routeRepository.findByDeparture(stop));
        }

        for (Route route: routes)
        {
            arrivals.add(route.getArrival().getJunction());
        }

        return arrivals;
    }

    public List<Junction> getJunctionsToJunction(Junction junction)
    {
        List<Stop> stops = junction.getStops();
        List<Route> routes = new ArrayList<>();
        List<Junction> departures = new ArrayList<>();

        for (Stop stop: stops)
        {
            routes.addAll(routeRepository.findByArrival(stop));
        }

        for (Route route: routes)
        {
            departures.add(route.getDeparture().getJunction());
        }

        return departures;
    }

    public List<Stop> getNearbyStops(Coordinates coords, int radius)
    {
        List<Stop> stops = stopRepository.findAll();
        //TODO
        return stops;
    }

    // Source:
    // https://stackoverflow.com/questions/3694380/calculating-distance-between-two-points-using-latitude-longitude-what-am-i-doi
    public double distance(double lat1, double lat2, double lon1, double lon2) {

        final int R = 6371;

        double latDistance = Math.toRadians(lat2 - lat1);
        double lonDistance = Math.toRadians(lon2 - lon1);
        double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        double distance = R * c * 1000;

        distance = Math.pow(distance, 2);

        return Math.sqrt(distance);
    }

}
