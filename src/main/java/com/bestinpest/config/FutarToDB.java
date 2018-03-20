package com.bestinpest.config;

import com.bestinpest.Application;
import com.bestinpest.model.*;
import com.bestinpest.repository.JunctionRepository;
import com.bestinpest.repository.RelationRepository;
import com.bestinpest.repository.RouteRepository;
import com.bestinpest.repository.StopRepository;
import com.google.gson.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.*;

@Component
public class FutarToDB implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(Application.class);

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

    @Override
    public void run(String... args) throws Exception {

        String response = restTemplate.getForObject(
                "http://futar.bkk.hu/bkk-utvonaltervezo-api/ws/otp/api/where/stops-for-location.json?lat=47.497638&lon=19.053021&radius="+gameConfig.getCityRadius(), String.class);

        JsonParser parser = new JsonParser();
        JsonArray stopsArray = parser.parse(response).getAsJsonObject().getAsJsonObject("data").getAsJsonArray("list");

        List<String> routeList = new ArrayList<>();

        for (JsonElement stopElement : stopsArray)
        {
            JsonObject stopObject = stopElement.getAsJsonObject();
            String id = stopObject.get("id").getAsString();
            String name = stopObject.get("name").getAsString();
            Double lat = stopObject.get("lat").getAsDouble();
            Double lon = stopObject.get("lon").getAsDouble();


            JsonArray routesArray = stopObject.get("routeIds").getAsJsonArray();

            if (routesArray.size()>0) {

                Stop stop = new Stop(id, name, lat, lon);


                for (JsonElement routeElement : routesArray) {
                    String routeId = routeElement.getAsString();
                    if (!routeList.contains(routeId) && !routeId.substring(0, 5).equals("BKK_9")) {
                        routeList.add(routeId);
                    }
                }


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


        for (String routeId : routeList)
        {
            response = restTemplate.getForObject(
                    "http://futar.bkk.hu/bkk-utvonaltervezo-api/ws/otp/api/where/route-details.json?routeId="+routeId+"&related=false", String.class);

            JsonObject entry = parser.parse(response).getAsJsonObject().getAsJsonObject("data").getAsJsonObject("entry");
            String relationName = entry.get("shortName").getAsString();
            String relationId = entry.get("id").getAsString();
            String type = entry.get("type").getAsString();
            String textColor = entry.get("textColor").getAsString();
            String backgroundColor = entry.get("color").getAsString();
            JsonArray variantsArray = entry.getAsJsonArray("variants");

            for (JsonElement variantElement: variantsArray)
            {
                JsonObject variant = variantElement.getAsJsonObject();
                String headsign = variant.get("headsign").getAsString();
                JsonArray stopIds = variant.get("stopIds").getAsJsonArray();

                for (int i=0; i<stopIds.size(); i++)
                {
                    String stopId = stopIds.get(i).getAsString();
                    Optional<Stop> stop = stopRepository.findById(stopId);

                    if (stop.isPresent()) {

                        int end = i+1;
                        for (int j=i+1; j<=end && j<stopIds.size(); j++)
                        {
                            String terminalStopId = stopIds.get(j).getAsString();
                            Optional<Stop> terminalStop = stopRepository.findById(terminalStopId);

                            if (terminalStop.isPresent())
                            {
                                String id = stop.get().getId()+terminalStop.get().getId()+type;
                                Route route;
                                if (routes.containsKey(id))
                                {
                                    route = routes.get(id);
                                }else{
                                    route = routeRepository.save(new Route(stop.get(), terminalStop.get(), type));
                                    routes.put(id, route);
                                }

                                Relation relation = relationRepository.save(new Relation(relationId, relationName, headsign, textColor, backgroundColor, route));
                                route.getRelations().add(relation);
                                routeRepository.save(route);
                            }else{
                                end++;
                            }

                        }

                    }



                }

            }

        }

    }
}
