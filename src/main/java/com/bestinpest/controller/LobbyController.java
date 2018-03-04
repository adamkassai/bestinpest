package com.bestinpest.controller;

import com.bestinpest.model.Lobby;
import com.bestinpest.model.Player;
import com.bestinpest.repository.LobbyRepository;
import com.bestinpest.repository.PlayerRepository;
import com.rabbitmq.tools.json.JSONWriter;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.util.List;

@RestController
public class LobbyController {

    @Autowired
    LobbyRepository lobbyRepository;

    @Autowired
    PlayerRepository playerRepository;

    @Autowired
    RabbitTemplate rabbitTemplate;

    JSONWriter jsonWriter = new JSONWriter();

    @GetMapping("/lobbies")
    public List<Lobby> lobbies() {
        rabbitTemplate.convertAndSend("bip-exchange", "", jsonWriter.write(lobbyRepository.findAll()));
        return lobbyRepository.findAll();
    }

    @PostMapping("/lobbies")
    public Lobby createLobby(@Valid @RequestBody Lobby lobby) {
        Player leader = lobby.getLeader();
        playerRepository.save(leader);
        lobbyRepository.save(lobby);

        lobby.getPlayers().add(leader);
        leader.setLobby(lobby);
        playerRepository.save(leader);
        return lobbyRepository.save(lobby);
    }

}
