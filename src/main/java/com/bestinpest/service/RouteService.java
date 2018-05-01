package com.bestinpest.service;

import com.bestinpest.model.*;
import com.bestinpest.repository.JunctionRepository;
import com.bestinpest.repository.RouteRepository;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.*;

@Service
public class RouteService {

    @Autowired
    JunctionRepository junctionRepository;

    @Autowired
    RouteRepository routeRepository;

    @Autowired
    RestTemplate restTemplate;

    public List<Route> getRoutesBetween(String departureId, String arrivalId)
    {
        Optional<Junction> departure = junctionRepository.findById(departureId);
        Optional<Junction> arrival = junctionRepository.findById(arrivalId);
        List<Route> routesBetween = new ArrayList<>();

        if (departure.isPresent() && arrival.isPresent()) {

            List<Route> routesByDeparture = new ArrayList<>();
            List<Route> routesByArrival = new ArrayList<>();

            for (Stop stop: departure.get().getStops())
            {
                routesByDeparture.addAll(routeRepository.findByDeparture(stop));
            }

            for (Stop stop: arrival.get().getStops())
            {
                routesByArrival.addAll(routeRepository.findByArrival(stop));
            }

            for (Route route : routesByDeparture)
            {
                if (routesByArrival.contains(route))
                    routesBetween.add(route);
            }
        }

        return routesBetween;
    }


    public List<Junction> getFreeJunctionsNearby(double lat, double lon, List<Player> players) {
        List<Junction> junctions = junctionRepository.findAll();
        List<Junction> reservedJunctions = new ArrayList<>();
        List<Junction> junctionsInOneStepRadius = new ArrayList<>();
        List<Junction> junctionsInTwoStepRadius = new ArrayList<>();

        for (Player player : players) {

            Optional<Junction> reservedJunction = junctionRepository.findById(player.getJunctionId());

            if (reservedJunction.isPresent()) {
                reservedJunctions.add(reservedJunction.get());
                junctionsInOneStepRadius.addAll(getJunctionsFromJunction(reservedJunction.get()));
                junctionsInOneStepRadius.addAll(getJunctionsToJunction(reservedJunction.get()));
            }
        }

        for (Junction junction : junctionsInOneStepRadius) {
            junctionsInTwoStepRadius.addAll(getJunctionsFromJunction(junction));
            junctionsInTwoStepRadius.addAll(getJunctionsToJunction(junction));
        }

        junctions.removeAll(reservedJunctions);
        junctions.removeAll(junctionsInOneStepRadius);
        junctions.removeAll(junctionsInTwoStepRadius);

        Collections.sort(junctions, new DistanceComparator(lat, lon));

        if (junctions.isEmpty()) { return junctions; }

        int max = junctions.size();
        if (10<max) { max=10; }

        return junctions.subList(0, max);
    }

    public List<Junction> getJunctionsFromJunction(Junction junction) {
        List<Stop> stops = junction.getStops();
        List<Route> routes = new ArrayList<>();
        List<Junction> arrivals = new ArrayList<>();

        for (Stop stop : stops) {
            routes.addAll(routeRepository.findByDeparture(stop));
        }

        for (Route route : routes) {
            if (!arrivals.contains(route.getArrival().getJunction()))
                arrivals.add(route.getArrival().getJunction());
        }

        return arrivals;
    }

    public List<Junction> getJunctionsToJunction(Junction junction) {
        List<Stop> stops = junction.getStops();
        List<Route> routes = new ArrayList<>();
        List<Junction> departures = new ArrayList<>();

        for (Stop stop : stops) {
            routes.addAll(routeRepository.findByArrival(stop));
        }

        for (Route route : routes) {
            if (!departures.contains(route.getDeparture().getJunction()))
                departures.add(route.getDeparture().getJunction());
        }

        return departures;
    }


    public List<Trip> getSchedule(Route route) {

        String response = restTemplate.getForObject(
                "http://futar.bkk.hu/bkk-utvonaltervezo-api/ws/otp/api/where/arrivals-and-departures-for-stop.json?includeReferences=true&minutesBefore=2&minutesAfter=30&stopId="+route.getDeparture().getId(), String.class);

        JsonParser parser = new JsonParser();
        JsonArray stopTimesArray = parser.parse(response).getAsJsonObject().getAsJsonObject("data").getAsJsonObject("entry").getAsJsonArray("stopTimes");

        List<Trip> trips = new ArrayList<>();

        for (JsonElement stopTimesElement : stopTimesArray) {
            JsonObject stopTimesObject = stopTimesElement.getAsJsonObject();
            String tripId = stopTimesObject.get("tripId").getAsString();

            JsonObject tripObject = parser.parse(response).getAsJsonObject().getAsJsonObject("data").getAsJsonObject("references").getAsJsonObject("trips")
                    .getAsJsonObject(tripId);

            String relationId = tripObject.get("routeId").getAsString();

            if (route.containsRelation(relationId)) {

                String relationName = parser.parse(response).getAsJsonObject().getAsJsonObject("data").getAsJsonObject("references").getAsJsonObject("routes")
                        .getAsJsonObject(relationId).get("shortName").getAsString();

                String tripHeadsign = tripObject.get("tripHeadsign").getAsString();
                JsonElement departureTime = stopTimesObject.get("departureTime");
                JsonElement predictedDepartureTime = stopTimesObject.get("predictedDepartureTime");
                JsonElement arrivalTime = stopTimesObject.get("arrivalTime");
                JsonElement predictedArrivalTime = stopTimesObject.get("predictedArrivalTime");
                Long time=null;
                Long predictedTime=null;

                if (departureTime!=null) { time=departureTime.getAsLong();
                }else if (arrivalTime!=null) { time=arrivalTime.getAsLong(); }

                if (predictedDepartureTime!=null) { predictedTime=predictedDepartureTime.getAsLong();
                }else if (predictedArrivalTime!=null) { predictedTime=predictedArrivalTime.getAsLong(); }

                trips.add(new Trip(time, predictedTime, relationName, relationId, tripHeadsign, tripId));
            }

        }

        return trips;
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


    class DistanceComparator implements Comparator<Junction>
    {

        private double lat;
        private double lon;

        public DistanceComparator(double lat, double lon) {
            this.lat = lat;
            this.lon = lon;
        }

        public int compare(Junction j1, Junction j2)
        {
            Stop s1 = j1.getStops().get(0);
            Stop s2 = j2.getStops().get(0);
            Double d1 = distance(lat, s1.getLat(), lon, s1.getLon());
            Double d2 = distance(lat, s2.getLat(), lon, s2.getLon());
            return d1.compareTo(d2);
        }
    }


}
