package com.bestinpest.controller;

import com.bestinpest.model.Lobby;
import com.bestinpest.repository.LobbyRepository;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class LobbyController {

    @Autowired
    LobbyRepository lobbyRepository;

    @Autowired
    RabbitTemplate rabbitTemplate;

    @RequestMapping("/lobbies")
    public List<Lobby> lobbies() {
        rabbitTemplate.convertAndSend("bip-exchange", "", "Hello from RabbitMQ!");
        return lobbyRepository.findAll();
    }

}
