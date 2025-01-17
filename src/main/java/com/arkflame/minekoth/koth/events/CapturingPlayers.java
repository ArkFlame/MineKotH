package com.arkflame.minekoth.koth.events;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.entity.Player;

public class CapturingPlayers {
    private final List<Player> players;

    public CapturingPlayers(Player player) {
        this.players = new ArrayList<>();
        this.players.add(player);
    }

    public List<Player> getPlayers() {
        return players;
    }

    public CapturingPlayers addPlayer(Player player) {
        players.add(player);
        return this;
    }

    public CapturingPlayers removePlayer(Player player) {
        players.remove(player);
        return this;
    }

    public boolean containsPlayer(Player player) {
        return players.contains(player);
    }
}
