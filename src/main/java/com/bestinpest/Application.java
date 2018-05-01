package com.bestinpest;

import com.bestinpest.config.GameConfig;
import com.bestinpest.model.Game;
import com.bestinpest.model.Lobby;
import com.bestinpest.model.Player;
import com.bestinpest.repository.GameRepository;
import com.bestinpest.repository.LobbyRepository;
import com.bestinpest.repository.PlayerRepository;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;

@SpringBootApplication
public class Application {

    @Bean
    TopicExchange exchange() {
        return new TopicExchange("bip-exchange");
    }

    @Bean
    public RestTemplate restTemplate(RestTemplateBuilder builder) {
        return builder.build();
    }

    public static void main(String[] args) {
        SpringApplication.run(Application.class);
    }

    @Bean
    public CommandLineRunner demo(LobbyRepository lobbyRepository, PlayerRepository playerRepository, GameRepository gameRepository, GameConfig gameConfig) {
        return (args) -> {

            Player player1 = new Player("Adam", "BKK_CSF01108", "Astoria M");
            Player player2 = new Player("Bobi", "BKK_CS009026");
            playerRepository.save(player1);
            playerRepository.save(player2);

            List<Player> players = new ArrayList<>();
            players.add(player1);
            players.add(player2);

            Lobby lobby = new Lobby("Elso fullos probajatek", player1, 5, "asdf", player2.getId(), players);
            lobbyRepository.save(lobby);

            player1.setLobby(lobby);
            playerRepository.save(player1);
            player2.setLobby(lobby);
            playerRepository.save(player2);


            //

            Player player3 = new Player("Adam", "BKK_CSF01108", "Astoria M");
            Player player4 = new Player("Bobi", "BKK_CS009026", "Zichy Jenő utca");
            Player player5 = new Player("Maki", "BKK_CSF01340", "Közraktár utca");
            playerRepository.save(player3);
            playerRepository.save(player4);
            playerRepository.save(player5);

            Game game = new Game(1000L, player3.getId());
            game.getPlayers().add(player3);
            game.getPlayers().add(player4);
            game.getPlayers().add(player5);
            gameRepository.save(game);
            player3.setGame(game);
            player4.setGame(game);
            player5.setGame(game);
            player3.setTickets(gameConfig.getTickets());
            player4.setTickets(gameConfig.getTickets());
            player5.setTickets(gameConfig.getTickets());
            playerRepository.save(player3);
            playerRepository.save(player4);
            playerRepository.save(player5);

        };
    }



}