package com.bestinpest.model;


import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
public class Lobby {

    @Id
    @GeneratedValue
    private Long id;

    private String name;

    @ManyToOne
    @JoinColumn(name="leaderId")
    private Player leader;

    private int maxPlayerNumber;
    private String password;
    private Long criminalId;

    @OneToMany(mappedBy="lobby")
    private List<Player> players = new ArrayList<>();

    public Lobby() {}

    public Lobby(String name, Player leader, int maxPlayerNumber, String password, Long criminalId, List<Player> players) {
        this.name = name;
        this.leader = leader;
        this.maxPlayerNumber = maxPlayerNumber;
        this.password = password;
        this.criminalId = criminalId;
        this.players = players;
    }

    public Lobby(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Player getLeader() {
        return leader;
    }

    public void setLeader(Player leader) {
        this.leader = leader;
    }

    public int getMaxPlayerNumber() {
        return maxPlayerNumber;
    }

    public void setMaxPlayerNumber(int maxPlayerNumber) {
        this.maxPlayerNumber = maxPlayerNumber;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public Long getCriminalId() {
        return criminalId;
    }

    public void setCriminalId(Long criminalId) {
        this.criminalId = criminalId;
    }

    public List<Player> getPlayers() {
        return players;
    }

    public void setPlayers(List<Player> players) {
        this.players = players;
    }

    public boolean getPasswordSet()
    {
        return (password!=null);
    }

    public boolean isValidPassword(String password) {

        if (this.password==null)
            return true;

        return (this.password.equals(password));
    }
}
