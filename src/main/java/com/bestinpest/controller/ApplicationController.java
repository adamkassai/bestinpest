package com.bestinpest.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
public class ApplicationController {

    @GetMapping("/rabbitmq-rx-url")
    public Map<String, String> rabbitmqRxUrl() {
        HashMap<String, String> map = new HashMap<>();
        map.put("url", System.getenv("RABBITMQ_BIGWIG_RX_URL"));
        return map;
    }

}
