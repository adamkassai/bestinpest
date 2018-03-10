package com.bestinpest.controller;

import com.bestinpest.exception.NotFoundException;
import com.bestinpest.model.Coordinates;
import com.bestinpest.model.Junction;
import com.bestinpest.model.Lobby;
import com.bestinpest.model.Player;
import com.bestinpest.repository.JunctionRepository;
import com.bestinpest.repository.LobbyRepository;
import com.bestinpest.repository.PlayerRepository;
import com.bestinpest.service.RouteService;
import com.rabbitmq.tools.json.JSONWriter;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.persistence.Lob;
import javax.validation.Valid;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@RestController
public class LobbyController {

    @Autowired
    LobbyRepository lobbyRepository;

    @Autowired
    PlayerRepository playerRepository;

    @Autowired
    JunctionRepository junctionRepository;

    @Autowired
    RabbitTemplate rabbitTemplate;

    @Autowired
    RouteService routeService;

    JSONWriter jsonWriter = new JSONWriter();

    @GetMapping("/lobbies")
    public List<Lobby> lobbies() {
        rabbitTemplate.convertAndSend("bip-exchange", "", jsonWriter.write(lobbyRepository.findAll()));
        return lobbyRepository.findAll();
    }

    @PostMapping("/lobbies")
    public Lobby createLobby(@Valid @RequestBody Lobby lobby) {

        if (lobby.getLeader()!=null) {
            Player leader = lobby.getLeader();
            playerRepository.save(leader);
            lobbyRepository.save(lobby);

            lobby.getPlayers().add(leader);
            leader.setLobby(lobby);
            playerRepository.save(leader);
        }
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


    @GetMapping("/lobbies/{id}/available-junctions")
    public List<Junction> getFreeJunctionsNearby(@PathVariable(value = "id") Long id, @RequestParam("lat") double lat, @RequestParam("lon") double lon) {

        Lobby lobby = lobbyRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Lobby", "id", id));

        return routeService.getFreeJunctionsNearby(lat, lon, lobby.getPlayers());
    }

    @GetMapping("/lobbies/available-junctions")
    public List<Junction> getFreeJunctionsNearbyForFirstPlayer(@RequestParam("lat") double lat, @RequestParam("lon") double lon) {

        return routeService.getFreeJunctionsNearby(lat, lon, new ArrayList<>());
    }

    @PostMapping("/lobbies/{id}/join/auth")
    public ResponseEntity<?> authToLobby(@PathVariable(value = "id") Long id, @RequestBody String password) {

        Lobby lobby = lobbyRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Lobby", "id", id));

        if (!lobby.isValidPassword(password))
        {
            return ResponseEntity.status(401).build();
        }

        return ResponseEntity.ok().build();
    }

    @PostMapping("/lobbies/{id}/criminal")
    public Lobby setCriminal(@PathVariable(value = "id") Long id, @Valid @RequestBody Player player) {

        Lobby lobby = lobbyRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Lobby", "id", id));

        Player existingPlayer = playerRepository.findById(player.getId())
                .orElseThrow(() -> new NotFoundException("Player", "id", player.getId()));

        if (!lobby.getPlayers().contains(existingPlayer))
        {
            throw new NotFoundException("Lobby", "player", player);
        }

        playerRepository.save(existingPlayer);

        lobby.setCriminalId(existingPlayer.getId());
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

        Lobby existingLobby = lobby.get();
        existingLobby.setLeader(null);
        lobbyRepository.save(existingLobby);
        playerRepository.delete(existingLobby.getPlayers());
        lobbyRepository.delete(existingLobby);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/lobbies/{id}/players/{playerId}")
    public ResponseEntity<?> deletePlayer(@PathVariable(value = "id") Long id, @PathVariable(value = "playerId") Long playerId) {

        Optional<Lobby> lobby = lobbyRepository.findById(id);

        if (!lobby.isPresent()) {
            return ResponseEntity.notFound().build();
        }

        Optional<Player> player = playerRepository.findById(playerId);

        if (!player.isPresent()) {
            return ResponseEntity.notFound().build();
        }
        Lobby existingLobby = lobby.get();
        existingLobby.getPlayers().remove(player);
        lobbyRepository.save(existingLobby);
        playerRepository.delete(player.get());
        return ResponseEntity.ok().build();
    }


}
