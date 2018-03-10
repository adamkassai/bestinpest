package com.bestinpest.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ApplicationController {

    @GetMapping("/rabbitmq-rx-url")
    public String rabbitmqRxUrl() {
        return System.getenv("RABBITMQ_BIGWIG_RX_URL");
    }

}
