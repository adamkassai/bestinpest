package com.bestinpest.controller;

import com.bestinpest.exception.BadRequestException;
import com.bestinpest.model.*;
import com.bestinpest.exception.NotFoundException;
import com.bestinpest.repository.GameRepository;
import com.bestinpest.repository.JunctionRepository;
import com.bestinpest.repository.LobbyRepository;
import com.bestinpest.repository.PlayerRepository;
import com.bestinpest.service.RouteService;
import com.rabbitmq.tools.json.JSONWriter;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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
    GameRepository gameRepository;

    @Autowired
    RabbitTemplate rabbitTemplate;

    @Autowired
    RouteService routeService;

    @GetMapping("/lobbies")
    public List<Lobby> lobbies() {
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
            lobby = lobbyRepository.save(lobby);

            RabbitMessage m = new RabbitMessage(lobby.getName()+" is added to the lobbies.", "lobby-added", lobbyRepository.findAll());
            rabbitTemplate.convertAndSend("bip-exchange", "lobbies", m.toString());

        }
        return lobby;
    }

    @PostMapping("/lobbies/{id}/join")
    public Lobby addPlayerToLobby(@PathVariable(value = "id") Long id, @Valid @RequestBody Player player) {

        Lobby lobby = lobbyRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Lobby", "id", id));

        if (lobby.getPlayers().size()>=lobby.getMaxPlayerNumber())
        {
            throw new BadRequestException("Lobby has reached the maximum player number.");
        }

        playerRepository.save(player);
        lobby.getPlayers().add(player);

        player.setLobby(lobby);
        playerRepository.save(player);

        lobby = lobbyRepository.save(lobby);

        RabbitMessage m = new RabbitMessage(player.getName()+" joined the lobby.", "player-joined", lobby);
        rabbitTemplate.convertAndSend("bip-exchange", "lobby:"+lobby.getId(), m.toString());

        return lobby;
    }


    @GetMapping("/lobbies/{id}/available-junctions")
    public List<Junction> getFreeJunctionsNearby(@PathVariable(value = "id") Long id, @RequestParam("lat") double lat, @RequestParam("lon") double lon) {

        Lobby lobby = lobbyRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Lobby", "id", id));

        return routeService.getFreeJunctionsNearby(lat, lon, lobby.getPlayers());
    }

    @PostMapping("/lobbies/{id}/start-game")
    public Game startGame(@PathVariable(value = "id") Long id) {

        Lobby lobby = lobbyRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Lobby", "id", id));

        Game game = new Game(lobby.getId(), lobby.getCriminalId());
        gameRepository.save(game);

        List<Player> players = lobby.getPlayers();

        for (Player player : players)
        {
            player.setLobby(null);
            player.setGame(game);
            playerRepository.save(player);
            game.getPlayers().add(player);
        }
        lobby.setPlayers(new ArrayList<>());

        game = gameRepository.save(game);

        RabbitMessage m = new RabbitMessage("Lobby is ready to play the game.", "game-started", game);
        rabbitTemplate.convertAndSend("bip-exchange", "lobby:"+lobby.getId(), m.toString());

        lobbyRepository.save(lobby);
        lobbyRepository.delete(lobby);
        return game;
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

        RabbitMessage m = new RabbitMessage(existingPlayer.getName()+" is the new criminal.", "criminal-changed", lobby);
        rabbitTemplate.convertAndSend("bip-exchange", "lobby:"+lobby.getId(), m.toString());

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

        RabbitMessage m = new RabbitMessage("Lobby is deleted.", "lobby-deleted", existingLobby);
        rabbitTemplate.convertAndSend("bip-exchange", "lobby:"+existingLobby.getId(), m.toString());

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

        existingLobby = lobbyRepository.save(existingLobby);
        playerRepository.delete(player.get());

        RabbitMessage m = new RabbitMessage(player.get().getName()+" is removed from the lobby.", "player-removed", existingLobby);
        rabbitTemplate.convertAndSend("bip-exchange", "lobby:"+existingLobby.getId(), m.toString());

        return ResponseEntity.ok().build();
    }


}
