package com.bestinpest;

import com.bestinpest.model.Lobby;
import com.bestinpest.model.Player;
import com.bestinpest.repository.LobbyRepository;
import com.bestinpest.repository.PlayerRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

    private static final Logger log = LoggerFactory.getLogger(Application.class);

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
    public CommandLineRunner demo(LobbyRepository lobbyRepository, PlayerRepository playerRepository) {
        return (args) -> {

            Player player1 = new Player("Adam", "BKK_CSF01108");
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

            lobbyRepository.save(new Lobby("Elso probajatek"));
            lobbyRepository.save(new Lobby("Ez mar a masodik"));

           /* // save a couple of customers
           Lobby(String name, Player leader, int maxPlayerNumber, String password, Player criminal, List<Player> players)
            reposditory.save(new Customer("Jack", "Bauer"));
            repository.save(new Customer("Chloe", "O'Brian"));
            repository.save(new Customer("Kim", "Bauer"));
            repository.save(new Customer("David", "Palmer"));
            repository.save(new Customer("Michelle", "Dessler"));

            // fetch all customers
            log.info("Customers found with findAll():");
            log.info("-------------------------------");
            for (Customer customer : repository.findAll()) {
                log.info(customer.toString());
            }
            log.info("");

            // fetch an individual customer by ID
            Customer customer = repository.findOne(1L);
            log.info("Customer found with findOne(1L):");
            log.info("--------------------------------");
            log.info(customer.toString());
            log.info("");

            // fetch customers by last name
            log.info("Customer found with findByLastName('Bauer'):");
            log.info("--------------------------------------------");
            for (Customer bauer : repository.findByLastName("Bauer")) {
                log.info(bauer.toString());
            }
            log.info("");*/
        };
    }

}