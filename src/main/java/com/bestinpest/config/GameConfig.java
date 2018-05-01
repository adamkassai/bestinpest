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

    public GameConfig() {

        maxRoundNumber=20;
        visibleCriminalRounds = Arrays.asList(3, 8, 13, 18);
        cityRadius = 2000;

        tickets = new HashMap<>();
        tickets.put("BUS-TROLLEY", 15);
        tickets.put("TRAM", 10);
        tickets.put("SUBWAY", 5);

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
}
