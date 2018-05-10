package com.bestinpest.config;

import com.bestinpest.model.*;
import com.bestinpest.repository.JunctionRepository;
import com.bestinpest.repository.RelationRepository;
import com.bestinpest.repository.RouteRepository;
import com.bestinpest.repository.StopRepository;
import com.bestinpest.service.RouteService;
import com.google.gson.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.*;

@Component
public class FutarToDB implements CommandLineRunner {

    @Autowired
    StopRepository stopRepository;

    @Autowired
    JunctionRepository junctionRepository;

    @Autowired
    RouteRepository routeRepository;

    @Autowired
    RelationRepository relationRepository;

    @Autowired
    RestTemplate restTemplate;

    @Autowired
    GameConfig gameConfig;

    @Autowired
    RouteService routeService;

    @Override
    public void run(String... args) {

        String response = restTemplate.getForObject(
                "http://futar.bkk.hu/bkk-utvonaltervezo-api/ws/otp/api/where/stops-for-location.json?lat=47.497638&lon=19.053021&radius=" + gameConfig.getCityRadius(), String.class);

        JsonParser parser = new JsonParser();
        JsonArray stopsArray = parser.parse(response).getAsJsonObject().getAsJsonObject("data").getAsJsonArray("list");

        List<String> routeList = new ArrayList<>();

        for (JsonElement stopElement : stopsArray) {
            JsonObject stopObject = stopElement.getAsJsonObject();
            String id = stopObject.get("id").getAsString();
            String name = stopObject.get("name").getAsString();
            Double lat = stopObject.get("lat").getAsDouble();
            Double lon = stopObject.get("lon").getAsDouble();


            JsonArray routesArray = stopObject.get("routeIds").getAsJsonArray();

            int routeNumber = 0;

            if (routesArray.size() > 0) {

                for (JsonElement routeElement : routesArray) {
                    String routeId = routeElement.getAsString();
                    if (!routeId.substring(0, 5).equals("BKK_9")) {

                        routeNumber++;

                        if (!routeList.contains(routeId)) {
                            routeList.add(routeId);
                        }
                    }
                }

            }

            if (routeNumber > 0) {

                Stop stop = new Stop(id, name, lat, lon);

                if (stopObject.get("parentStationId") != null) {

                    String parentStationId = stopObject.get("parentStationId").getAsString();

                    Optional<Junction> junction = junctionRepository.findById(parentStationId);

                    if (!junction.isPresent()) {
                        Junction newJunction = new Junction(parentStationId, name);
                        junctionRepository.save(newJunction);
                        stop.setJunction(newJunction);
                    } else {
                        stop.setJunction(junction.get());
                    }

                }

                stopRepository.save(stop);
            }


        }

        Map<String, Route> routes = new HashMap<>();


        for (String routeId : routeList) {
            response = restTemplate.getForObject(
                    "http://futar.bkk.hu/bkk-utvonaltervezo-api/ws/otp/api/where/route-details.json?routeId=" + routeId + "&related=false", String.class);

            JsonObject entry = parser.parse(response).getAsJsonObject().getAsJsonObject("data").getAsJsonObject("entry");
            String relationName = entry.get("shortName").getAsString();
            String relationId = entry.get("id").getAsString();
            String type = entry.get("type").getAsString();
            String textColor = entry.get("textColor").getAsString();
            String backgroundColor = entry.get("color").getAsString();
            JsonArray variantsArray = entry.getAsJsonArray("variants");

            for (JsonElement variantElement : variantsArray) {
                JsonObject variant = variantElement.getAsJsonObject();
                String headsign = variant.get("headsign").getAsString();
                JsonArray stopIds = variant.get("stopIds").getAsJsonArray();

                for (int i = 0; i < stopIds.size(); i++) {
                    String stopId = stopIds.get(i).getAsString();
                    Optional<Stop> stop = stopRepository.findById(stopId);

                    if (stop.isPresent()) {

                        int end = i + 1;
                        for (int j = i + 1; j <= end && j < stopIds.size(); j++) {
                            String terminalStopId = stopIds.get(j).getAsString();
                            Optional<Stop> terminalStop = stopRepository.findById(terminalStopId);

                            if (terminalStop.isPresent()) {
                                String id = stop.get().getId() + terminalStop.get().getId() + type;
                                Route route;
                                if (routes.containsKey(id)) {
                                    route = routes.get(id);
                                } else {
                                    route = routeRepository.save(new Route(stop.get(), terminalStop.get(), type));
                                    routes.put(id, route);
                                }

                                Relation relation = relationRepository.save(new Relation(relationId, relationName, headsign, textColor, backgroundColor, route));
                                route.getRelations().add(relation);
                                routeRepository.save(route);
                            } else {
                                end++;
                            }

                        }

                    }

                }

            }

        }

        List<Junction> junctions = junctionRepository.findAll();
        String type = "WALK";

        for (int i = 0; i < junctions.size(); i++) {

            for (int j = 0; j < junctions.size(); j++) {

                Junction j1 = junctions.get(i);
                Junction j2 = junctions.get(j);


                if (!j1.equals(j2) && j1.getStops().size() > 0 && j2.getStops().size() > 0) {

                    Stop s1 = j1.getStops().get(0);
                    Stop s2 = j2.getStops().get(0);


                    if (routeService.distance(s1.getLat(), s2.getLat(), s1.getLon(), s2.getLon()) <= gameConfig.getMaxWalkDistance()
                            && routeService.getRoutesBetween(j1.getId(), j2.getId()).isEmpty()) {

                        String id = s1.getId() + s2.getId() + type;
                        Route route;
                        if (!routes.containsKey(id)) {
                            route = routeRepository.save(new Route(s1, s2, type));
                            routes.put(id, route);

                            Relation relation = relationRepository.save(new Relation(id, "WALK", j2.getName(), gameConfig.getWalkTextColor(), gameConfig.getWalkBackgroundColor(), route));
                            route.getRelations().add(relation);
                            routeRepository.save(route);
                        }


                    }

                }

            }
        }


    }
}
