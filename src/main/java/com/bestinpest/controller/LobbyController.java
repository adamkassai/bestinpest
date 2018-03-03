package com.bestinpest.controller;

import com.bestinpest.model.Lobby;
import com.bestinpest.repository.LobbyRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class LobbyController {

    @Autowired
    LobbyRepository lobbyRepository;

    @RequestMapping("/lobbies")
    public List<Lobby> lobbies() {
        return lobbyRepository.findAll();
    }

}
