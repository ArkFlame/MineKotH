package com.arkflame.minekoth.koth.events;

public class KothEventPlayerStats {
    private int totalPlayerKills;
    private int totalMobKills;
    private int totalDeaths;
    private int currentKillStreak;
    private long totalTimeCaptured; // in milliseconds
    private long firstCaptureTime; // timestamp in milliseconds
    private long lastCaptureTime; // timestamp in milliseconds
    private long lastKillTime; // timestamp in milliseconds
    private int totalDamageReceived;
    private int totalDamageDone;

    public KothEventPlayerStats() {
        this.totalPlayerKills = 0;
        this.totalMobKills = 0;
        this.totalTimeCaptured = 0;
        this.firstCaptureTime = 0;
        this.lastCaptureTime = 0;
        this.lastKillTime = 0;
        this.totalDamageReceived = 0;
        this.totalDamageDone = 0;
        this.totalDeaths = 0;
        this.currentKillStreak = 0;
    }

    public void addPlayerKill() {
        totalPlayerKills++;
        currentKillStreak++;
        lastKillTime = System.currentTimeMillis(); // Set last kill time to current time in milliseconds
    }

    public void addMobKill() {
        totalMobKills++;
    }

    public void updateCapture() {
        if (firstCaptureTime == 0) {
            firstCaptureTime = System.currentTimeMillis(); // Set first capture time if not set
        }
        lastCaptureTime = System.currentTimeMillis(); // Update last capture time
        totalTimeCaptured = totalTimeCaptured + 1000; // Add the time captured in milliseconds
    }

    public void addDamageReceived(int damage) {
        totalDamageReceived += damage;
    }

    public void addDamageDone(int damage) {
        totalDamageDone += damage;
    }

    // Getters for the stats
    public int getTotalPlayerKills() {
        return totalPlayerKills;
    }

    public int getTotalMobKills() {
        return totalMobKills;
    }

    public long getTotalTimeCaptured() {
        return totalTimeCaptured;
    }

    public long getFirstCaptureTime() {
        return firstCaptureTime; // Return as timestamp in milliseconds
    }

    public long getLastCaptureTime() {
        return lastCaptureTime; // Return as timestamp in milliseconds
    }

    public long getLastKillTime() {
        return lastKillTime; // Return as timestamp in milliseconds
    }

    public int getTotalDamageReceived() {
        return totalDamageReceived;
    }

    public int getTotalDamageDone() {
        return totalDamageDone;
    }

    public int getCurrentKillStreak() {
        return currentKillStreak;
    }

    public int getDeaths() {
        return totalDeaths;
    }

    public int addDeath() {
        currentKillStreak = 0;
        return totalDeaths++;
    }
}
