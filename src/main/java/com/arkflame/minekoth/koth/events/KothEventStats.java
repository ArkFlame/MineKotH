package com.arkflame.minekoth.koth.events;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class KothEventStats {
    private Map<UUID, KothEventPlayerStats> playerStatsMap;

    public KothEventStats() {
        this.playerStatsMap = new HashMap<>();
    }

    public KothEventPlayerStats getPlayerStats(UUID playerId) {
        return playerStatsMap.computeIfAbsent(playerId, id -> new KothEventPlayerStats());
    }

    // Method to add a player kill
    public void addPlayerKill(UUID playerId) {
        KothEventPlayerStats stats = getPlayerStats(playerId);
        stats.addPlayerKill();
    }

    // Method to add a mob kill
    public void addMobKill(UUID playerId) {
        KothEventPlayerStats stats = getPlayerStats(playerId);
        stats.addMobKill();
    }

    // Method to update capture time
    public void updateCapture(UUID playerId) {
        KothEventPlayerStats stats = getPlayerStats(playerId);
        stats.updateCapture();
    }

    // Method to add damage received
    public void addDamageReceived(UUID playerId, int damage) {
        KothEventPlayerStats stats = getPlayerStats(playerId);
        stats.addDamageReceived(damage);
    }

    // Method to add damage done
    public void addDamageDone(UUID playerId, int damage) {
        KothEventPlayerStats stats = getPlayerStats(playerId);
        stats.addDamageDone(damage);
    }

    public void addDeath(UUID uniqueId) {
        KothEventPlayerStats stats = getPlayerStats(uniqueId);
        stats.addDeath();
    }
}
