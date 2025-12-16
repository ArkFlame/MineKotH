package com.arkflame.minekoth.koth.events;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.entity.Player;

import com.arkflame.minekoth.MineKoth;
import com.arkflame.minekoth.utils.MineClansHook;
import com.arkflame.minekoth.utils.UClansHook;

public class CapturingPlayers {
    private String name;
    private final List<Player> players;
    private int score = 0;

    public CapturingPlayers(Player player) {
        this.name = player.getName();
        this.players = new ArrayList<>();
        this.players.add(player);

        MineKoth mineKoth = MineKoth.getInstance();
        boolean nameSet = false;

        // Priority 1: MineClans
        if (mineKoth.isMineClansEnabled()) {
            String factionName = MineClansHook.getClanName(player);
            if (factionName != null) {
                this.name = factionName;
                nameSet = true;
            }
        }

        // Priority 2: UClans (Only if name wasn't set by MineClans)
        if (!nameSet && mineKoth.isUClansEnabled()) {
            String clanName = UClansHook.getClanName(player);
            if (clanName != null) {
                this.name = clanName;
            }
        }
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
        
        Player capturingPlayer = getPlayers().get(0);
        MineKoth mineKoth = MineKoth.getInstance();
        
        // Check MineClans
        if (mineKoth.isMineClansEnabled()) {
            if (MineClansHook.isSameTeam(capturingPlayer, player)) {
                return true;
            }
        }

        // Check UClans
        if (mineKoth.isUClansEnabled()) {
            if (UClansHook.isSameTeam(capturingPlayer, player)) {
                return true;
            }
        }

        return false;
    }

    public Player getTopPlayer() {
        if (players.isEmpty()) {
            return null;
        }
        return players.get(0);
    }
}