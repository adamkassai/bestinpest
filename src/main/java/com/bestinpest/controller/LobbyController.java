package com.bestinpest.controller;

import com.bestinpest.config.GameConfig;
import com.bestinpest.exception.BadRequestException;
import com.bestinpest.model.*;
import com.bestinpest.exception.NotFoundException;
import com.bestinpest.repository.GameRepository;
import com.bestinpest.repository.JunctionRepository;
import com.bestinpest.repository.LobbyRepository;
import com.bestinpest.repository.PlayerRepository;
import com.bestinpest.service.RouteService;
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
    GameRepository gameRepository;

    @Autowired
    GameConfig gameConfig;

    @Autowired
    JunctionRepository junctionRepository;

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

        if (lobby.getLeader() == null) {
            throw new BadRequestException("Lobby must have a leader.");
        }

        Player leader = lobby.getLeader();
        leader = playerRepository.save(leader);
        lobbyRepository.save(lobby);

        lobby.getPlayers().add(leader);
        lobby.setCriminalId(leader.getId());
        leader.setLobby(lobby);
        playerRepository.save(leader);
        lobby = lobbyRepository.save(lobby);

        RabbitMessage m = new RabbitMessage(lobby.getName() + " is added to the lobbies.", "lobby-added", lobbyRepository.findAll());
        rabbitTemplate.convertAndSend("bip-exchange", "lobbies", m.toString());

        return lobby;
    }

    @PostMapping("/lobbies/{id}/join")
    public Lobby addPlayerToLobby(@PathVariable(value = "id") Long id, @Valid @RequestBody Player player) {

        Lobby lobby = lobbyRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Lobby", "id", id));

        if (lobby.getPlayers().size() >= lobby.getMaxPlayerNumber()) {
            throw new BadRequestException("Lobby has reached the maximum player number.");
        }

        playerRepository.save(player);
        lobby.getPlayers().add(player);

        Junction junction = junctionRepository.findById(player.getJunctionId())
                .orElseThrow(() -> new NotFoundException("departureJunction", "id", player.getJunctionId()));

        player.setJunctionName(junction.getName());

        player.setLobby(lobby);
        playerRepository.save(player);

        lobby = lobbyRepository.save(lobby);

        RabbitMessage m = new RabbitMessage(player.getName() + " joined the lobby.", "player-joined", lobby);
        rabbitTemplate.convertAndSend("bip-exchange", "lobby:" + lobby.getId(), m.toString());

        m = new RabbitMessage(lobby.getName() + " has a new player.", "player-joined", lobbyRepository.findAll());
        rabbitTemplate.convertAndSend("bip-exchange", "lobbies", m.toString());

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

        for (Player player : players) {
            player.setLobby(null);
            player.setGame(game);
            player.setTickets(gameConfig.getTickets());
            playerRepository.save(player);
            game.getPlayers().add(player);
        }
        lobby.setPlayers(new ArrayList<>());

        game = gameRepository.save(game);

        RabbitMessage m = new RabbitMessage("Lobby is ready to play the game.", "game-started", game);
        rabbitTemplate.convertAndSend("bip-exchange", "lobby:" + lobby.getId(), m.toString());

        lobbyRepository.save(lobby);
        lobbyRepository.delete(lobby);

        m = new RabbitMessage(lobby.getName() + " started the game.", "game-started", lobbyRepository.findAll());
        rabbitTemplate.convertAndSend("bip-exchange", "lobbies", m.toString());

        return game;
    }

    @GetMapping("/lobbies/available-junctions")
    public List<Junction> getFreeJunctionsNearbyForFirstPlayer(@RequestParam("lat") double lat, @RequestParam("lon") double lon) {

        return routeService.getFreeJunctionsNearby(lat, lon, new ArrayList<>());
    }

    @GetMapping("/lobbies/{id}/join/auth")
    public Lobby authToLobby(@PathVariable(value = "id") Long id, @RequestParam("password") String password) {

        Lobby lobby = lobbyRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Lobby", "id", id));

        if (!lobby.isValidPassword(password)) {
            throw new BadRequestException("Invalid password");
        }

        return lobby;
    }

    @PostMapping("/lobbies/{id}/criminal")
    public Lobby setCriminal(@PathVariable(value = "id") Long id, @Valid @RequestBody Player player) {

        Lobby lobby = lobbyRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Lobby", "id", id));

        Player existingPlayer = playerRepository.findById(player.getId())
                .orElseThrow(() -> new NotFoundException("Player", "id", player.getId()));

        if (!lobby.getPlayers().contains(existingPlayer)) {
            throw new NotFoundException("Lobby", "player", player);
        }

        playerRepository.save(existingPlayer);

        lobby.setCriminalId(existingPlayer.getId());

        RabbitMessage m = new RabbitMessage(existingPlayer.getName() + " is the new criminal.", "criminal-changed", lobby);
        rabbitTemplate.convertAndSend("bip-exchange", "lobby:" + lobby.getId(), m.toString());

        return lobbyRepository.save(lobby);
    }

    @GetMapping("/lobbies/{id}")
    public Lobby getLobbyById(@PathVariable(value = "id") Long id) {
        return lobbyRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Lobby", "id", id));
    }

    @DeleteMapping("/lobbies/{id}")
    public List<Lobby> deleteLobby(@PathVariable(value = "id") Long id) {

        Lobby lobby = lobbyRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Lobby", "id", id));

        lobby.setLeader(null);
        lobbyRepository.save(lobby);
        playerRepository.delete(lobby.getPlayers());

        RabbitMessage m = new RabbitMessage("Lobby is deleted.", "lobby-deleted", lobby);
        rabbitTemplate.convertAndSend("bip-exchange", "lobby:" + lobby.getId(), m.toString());

        lobbyRepository.delete(lobby);

        m = new RabbitMessage(lobby.getName() + " is deleted.", "lobby-deleted", lobbyRepository.findAll());
        rabbitTemplate.convertAndSend("bip-exchange", "lobbies", m.toString());

        return lobbyRepository.findAll();
    }

    @DeleteMapping("/lobbies/{id}/players/{playerId}")
    public Lobby deletePlayer(@PathVariable(value = "id") Long id, @PathVariable(value = "playerId") Long playerId) {


        Lobby lobby = lobbyRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Lobby", "id", id));

        Player player = playerRepository.findById(playerId)
                .orElseThrow(() -> new NotFoundException("Player", "id", playerId));

        if (player.getId().equals(lobby.getCriminalId())) {
            lobby.setCriminalId(lobby.getLeader().getId());
        }

        lobby = lobbyRepository.save(lobby);
        playerRepository.delete(player);

        RabbitMessage m = new RabbitMessage(player.getName() + " is removed from the lobby.", "player-removed", lobby);
        rabbitTemplate.convertAndSend("bip-exchange", "lobby:" + lobby.getId(), m.toString());

        m = new RabbitMessage(lobby.getName() + " has a player removed.", "player-removed", lobbyRepository.findAll());
        rabbitTemplate.convertAndSend("bip-exchange", "lobbies", m.toString());

        return lobby;
    }

    @PostMapping("/lobbies/{id}/players/{playerId}/ready")
    public Player setPlayerReady(@PathVariable(value = "id") Long id, @PathVariable(value = "playerId") Long playerId) {

        Lobby lobby = lobbyRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Lobby", "id", id));

        Player player = playerRepository.findById(playerId)
                .orElseThrow(() -> new NotFoundException("Player", "id", playerId));

        if (player.getReady()) {
            player.setReady(false);
        } else {
            player.setReady(true);
        }
        player = playerRepository.save(player);

        RabbitMessage m = new RabbitMessage(player.getName() + " is ready to play.", "player-ready", lobby);
        rabbitTemplate.convertAndSend("bip-exchange", "lobby:" + lobby.getId(), m.toString());

        return player;
    }

}
