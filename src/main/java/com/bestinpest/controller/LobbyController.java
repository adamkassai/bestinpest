package com.bestinpest.controller;

import com.bestinpest.exception.NotFoundException;
import com.bestinpest.model.Lobby;
import com.bestinpest.model.Player;
import com.bestinpest.repository.LobbyRepository;
import com.bestinpest.repository.PlayerRepository;
import com.rabbitmq.tools.json.JSONWriter;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;
import java.util.Optional;

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

    @PostMapping("/lobbies/{id}/join")
    public Lobby addPlayerToLobby(@PathVariable(value = "id") Long id, @Valid @RequestBody Player player) {

        Lobby lobby = lobbyRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Lobby", "id", id));

        playerRepository.save(player);
        lobby.getPlayers().add(player);

        player.setLobby(lobby);
        playerRepository.save(player);

        return lobbyRepository.save(lobby);
    }

    @GetMapping("/lobbies/{id}")
    public Lobby getLobbyById(@PathVariable(value = "id") Long id) {
        return lobbyRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Lobby", "id", id));
    }

    @DeleteMapping("/lobbies/{id}")
    public ResponseEntity<?> deleteLobby(@PathVariable(value = "id") Long id) {

        Optional<Lobby> lobby = lobbyRepository.findById(id);

        if (!lobby.isPresent()) {
            return ResponseEntity.notFound().build();
        }

        lobbyRepository.delete(lobby.get());
        return ResponseEntity.ok().build();
    }


}
