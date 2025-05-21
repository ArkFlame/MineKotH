package com.arkflame.minekoth.koth.events;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.entity.Player;

import com.arkflame.minekoth.MineKoth;
import com.arkflame.minekoth.utils.MineClansHook;

public class CapturingPlayers {
    private String name;
    private final List<Player> players;
    private int score = 0;

    public CapturingPlayers(Player player) {
        this.name = player.getName();
        if (MineKoth.getInstance().isMineClansEnabled()) { // MineClans plugin enabled
            String factionName = MineClansHook.getClanName(player);
            if (factionName != null) {
                this.name = factionName;
            }
        }
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

    public int getScore() {
        return score;
    }

    public void setScore(int score) {
        this.score = score;
    }
    
    public void addScore(int score) {
        this.score += score;
    }

    public String getCaptureTimeFormatted() {
        return String.format("%02d:%02d", score / 60, score % 60);
    }

    public String getName() {
        return name;
    }

    public boolean isSameTeam(Player player) {
        if (players.isEmpty()) {
            return false;
        }
        if (player == null) {
            return false;
        }
        // Placeholder logic for determining if two players are on the same team.
        if (MineKoth.getInstance().isMineClansEnabled()) { // MineClans plugin enabled
            return MineClansHook.isSameTeam(getPlayers().get(0), player);
        }
        return false;
    }
}
