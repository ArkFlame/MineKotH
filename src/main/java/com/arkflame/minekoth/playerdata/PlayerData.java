package com.arkflame.minekoth.playerdata;

import java.util.HashMap;
import java.util.Map;

/**
 * Represents a player's data in Koth events.
 */
public class PlayerData {

    // Static constants for statistic keys
    public static final String KILLS = "kills";
    public static final String DEATHS = "deaths";
    public static final String WINS = "wins";
    public static final String PARTICIPATIONS = "participations";
    public static final String CAPTURE_TIME = "capture_time";
    public static final String KDR = "kdr";
    public static final String DAMAGE_DEALT = "damage_dealt";
    public static final String DAMAGE_RECEIVED = "damage_received";
    public static final String REWARDS_RECEIVED = "rewards_received";

    // Map for total statistics
    private final Map<String, StatValue> totalStats = new HashMap<>();

    // Map for statistics by Koth ID
    private final Map<String, Map<Integer, StatValue>> statsByKoth = new HashMap<>();

    /**
     * Loads the player data.
     * Can be overridden by subclasses to implement custom loading logic.
     */
    public void load() {
        // Default implementation (empty)
    }

    /**
     * Saves the player data.
     * Can be overridden by subclasses to implement custom saving logic.
     */
    public void save() {
        // Default implementation (empty)
    }

    // Generic methods to set and add total values
    public void setTotal(String key, Number value) {
        totalStats.put(key, new StatValue(value));
    }

    public void addTotal(String key, Number value) {
        totalStats.computeIfAbsent(key, k -> new StatValue(0));
        totalStats.get(key).add(value);
    }

    public Number getTotal(String key) {
        StatValue statValue = totalStats.get(key);
        return statValue != null ? statValue.getValue() : 0;
    }

    // Generic methods to set and add values by Koth ID
    public void setByKoth(String key, int kothId, Number value) {
        statsByKoth.computeIfAbsent(key, k -> new HashMap<>());
        statsByKoth.get(key).put(kothId, new StatValue(value));
    }

    public void addByKoth(String key, int kothId, Number value) {
        statsByKoth.computeIfAbsent(key, k -> new HashMap<>());
        Map<Integer, StatValue> kothStats = statsByKoth.get(key);
        kothStats.computeIfAbsent(kothId, id -> new StatValue(0));
        kothStats.get(kothId).add(value);
    }

    public Number getByKoth(String key, int kothId) {
        Map<Integer, StatValue> kothStats = statsByKoth.get(key);
        if (kothStats != null) {
            StatValue statValue = kothStats.get(kothId);
            return statValue != null ? statValue.getValue() : 0;
        }
        return 0;
    }

    // Specific methods for incrementing statistics
    public void incrementKillCount(int kothId) {
        addByKoth(KILLS, kothId, 1);
        addTotal(KILLS, 1);
        updateKdr(kothId);
    }

    public void incrementDeathCount(int kothId) {
        addByKoth(DEATHS, kothId, 1);
        addTotal(DEATHS, 1);
        updateKdr(kothId);
    }

    public void incrementWinCount(int kothId) {
        addByKoth(WINS, kothId, 1);
        addTotal(WINS, 1);
    }

    public void incrementParticipationCount(int kothId) {
        addByKoth(PARTICIPATIONS, kothId, 1);
        addTotal(PARTICIPATIONS, 1);
    }

    public void addCaptureTime(int kothId, long time) {
        addByKoth(CAPTURE_TIME, kothId, time);
        addTotal(CAPTURE_TIME, time);
    }

    public void addDamageDealt(int kothId, double damage) {
        addByKoth(DAMAGE_DEALT, kothId, damage);
        addTotal(DAMAGE_DEALT, damage);
    }

    public void addDamageReceived(int kothId, double damage) {
        addByKoth(DAMAGE_RECEIVED, kothId, damage);
        addTotal(DAMAGE_RECEIVED, damage);
    }

    public void incrementRewardsReceived(int kothId, int rewards) {
        addByKoth(REWARDS_RECEIVED, kothId, rewards);
        addTotal(REWARDS_RECEIVED, rewards);
    }

    // Methods to calculate and retrieve KDR
    private void updateKdr(int kothId) {
        int kills = getByKoth(KILLS, kothId).intValue();
        int deaths = getByKoth(DEATHS, kothId).intValue();
        double kdr = deaths == 0 ? kills : (double) kills / deaths;
        setByKoth(KDR, kothId, kdr);

        int totalKills = getTotal(KILLS).intValue();
        int totalDeaths = getTotal(DEATHS).intValue();
        double totalKdr = totalDeaths == 0 ? totalKills : (double) totalKills / totalDeaths;
        setTotal(KDR, totalKdr);
    }

    // Specific methods for accessing statistics
    public double getKdr(int kothId) {
        return getByKoth(KDR, kothId).doubleValue();
    }

    public double getTotalKdr() {
        return getTotal(KDR).doubleValue();
    }

    // Getters for other statistics
    public int getKillCount(int kothId) {
        return getByKoth(KILLS, kothId).intValue();
    }

    public int getTotalKills() {
        return getTotal(KILLS).intValue();
    }

    public int getDeathCount(int kothId) {
        return getByKoth(DEATHS, kothId).intValue();
    }

    public int getTotalDeaths() {
        return getTotal(DEATHS).intValue();
    }

    public int getWinCount(int kothId) {
        return getByKoth(WINS, kothId).intValue();
    }

    public int getTotalWins() {
        return getTotal(WINS).intValue();
    }

    public int getParticipationCount(int kothId) {
        return getByKoth(PARTICIPATIONS, kothId).intValue();
    }

    public int getTotalParticipations() {
        return getTotal(PARTICIPATIONS).intValue();
    }

    public long getCaptureTime(int kothId) {
        return getByKoth(CAPTURE_TIME, kothId).longValue();
    }

    public long getTotalCaptureTime() {
        return getTotal(CAPTURE_TIME).longValue();
    }

    public double getDamageDealt(int kothId) {
        return getByKoth(DAMAGE_DEALT, kothId).doubleValue();
    }

    public double getTotalDamageDealt() {
        return getTotal(DAMAGE_DEALT).doubleValue();
    }

    public double getDamageReceived(int kothId) {
        return getByKoth(DAMAGE_RECEIVED, kothId).doubleValue();
    }

    public double getTotalDamageReceived() {
        return getTotal(DAMAGE_RECEIVED).doubleValue();
    }

    public int getRewardsReceived(int kothId) {
        return getByKoth(REWARDS_RECEIVED, kothId).intValue();
    }

    public int getTotalRewardsReceived() {
        return getTotal(REWARDS_RECEIVED).intValue();
    }

    /**
     * Protected getters for persistence implementations.
     */
    protected Map<String, StatValue> getTotalStats() {
        return totalStats;
    }

    protected Map<String, Map<Integer, StatValue>> getStatsByKoth() {
        return statsByKoth;
    }
}
