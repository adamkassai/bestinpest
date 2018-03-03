package com.bestinpest.controller;

import com.bestinpest.model.Lobby;
import com.bestinpest.repository.LobbyRepository;
import com.rabbitmq.tools.json.JSONWriter;
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

    JSONWriter jsonWriter = new JSONWriter();

    @RequestMapping("/lobbies")
    public List<Lobby> lobbies() {
        rabbitTemplate.convertAndSend("bip-exchange", "", jsonWriter.write(lobbyRepository.findAll()));
        return lobbyRepository.findAll();
    }

}
