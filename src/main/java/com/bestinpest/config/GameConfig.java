package com.bestinpest.config;

import org.springframework.stereotype.Component;


import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class GameConfig {

    private int maxRoundNumber;
    private List<Integer> visibleCriminalRounds;
    private int cityRadius;
    private Map<String, Integer> tickets;
    private int maxWalkDistance;
    private String walkBackgroundColor;
    private String walkTextColor;

    public GameConfig() {

        maxRoundNumber=Integer.parseInt(System.getenv("BIP_MAX_ROUND_NUMBER"));
        visibleCriminalRounds = Arrays.asList(3, 8, 13, 18);
        cityRadius = Integer.parseInt(System.getenv("BIP_CITY_RADIUS"));
        maxWalkDistance = Integer.parseInt(System.getenv("BIP_MAX_WALK_DISTANCE"));
        walkBackgroundColor = System.getenv("BIP_WALK_BACKGROUND_COLOR");
        walkTextColor = System.getenv("BIP_WALK_TEXT_COLOR");

        tickets = new HashMap<>();
        tickets.put("BUS-TROLLEY", 15);
        tickets.put("TRAM", 10);
        tickets.put("SUBWAY", 5);
        tickets.put("WALK", 25);

    }

    public int getMaxRoundNumber() {
        return maxRoundNumber;
    }

    public List<Integer> getVisibleCriminalRounds() {
        return visibleCriminalRounds;
    }

    public int getCityRadius() {
        return cityRadius;
    }

    public Map<String, Integer> getTickets() {
        return tickets;
    }

    public int getMaxWalkDistance() {
        return maxWalkDistance;
    }

    public String getWalkBackgroundColor() {
        return walkBackgroundColor;
    }

    public String getWalkTextColor() {
        return walkTextColor;
    }
}
