package com.arkflame.minekoth.koth;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.Configuration;
import org.bukkit.entity.Player;

import com.arkflame.minekoth.MineKoth;
import com.arkflame.minekoth.holograms.HologramsAPIUniversal;
import com.arkflame.minekoth.utils.FoliaAPI;

public class Koth {
    private int id;
    private String name;
    private String worldName;
    private Position firstPosition;
    private Position secondPosition;
    private int timeLimit;
    private int timeToCapture;
    private Rewards rewards;
    private String times;
    private String days;
    private boolean hologramSpawned = false;

    private Location center;
    private Location safeCenter;

    public Koth(int id, String name, String worldName, Position firstPosition, Position secondPosition, int timeLimit,
            int timeToCapture, Rewards rewards, String times, String days) {
        this.id = id;
        this.name = name;
        this.worldName = worldName;
        this.firstPosition = firstPosition;
        this.secondPosition = secondPosition;
        this.timeLimit = timeLimit;
        this.timeToCapture = timeToCapture;
        this.rewards = rewards;
        this.times = times;
        this.days = days;
    }

    public World getWorld() {
        return Bukkit.getWorld(worldName);
    }

    public Location getFirstLocation() {
        return new Location(getWorld(), firstPosition.getX(), firstPosition.getY(), firstPosition.getZ());
    }

    public Location getSecondLocation() {
        return new Location(getWorld(), secondPosition.getX(), secondPosition.getY(), secondPosition.getZ());
    }

    public Position getFirstPosition() {
        return firstPosition;
    }

    public Position getSecondPosition() {
        return secondPosition;
    }

    public boolean isInside(Location location) {
        double xMin = Math.min(firstPosition.getX(), secondPosition.getX());
        double xMax = Math.max(firstPosition.getX(), secondPosition.getX());
        double yMin = Math.min(firstPosition.getY(), secondPosition.getY());
        double yMax = Math.max(firstPosition.getY(), secondPosition.getY());
        double zMin = Math.min(firstPosition.getZ(), secondPosition.getZ());
        double zMax = Math.max(firstPosition.getZ(), secondPosition.getZ());

        return location.getWorld().getName().equals(worldName) &&
                location.getX() >= xMin && location.getX() <= xMax + 1 &&
                location.getY() >= yMin && location.getY() <= yMax + 1.5 &&
                location.getZ() >= zMin && location.getZ() <= zMax + 1;
    }

    public boolean isInside(Player player) {
        return isInside(player.getLocation());
    }

    // Getters and Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getWorldName() {
        return worldName;
    }

    public void setWorldName(String worldName) {
        this.worldName = worldName;
    }

    public int getTimeLimit() {
        return timeLimit;
    }

    public void setTimeLimit(int timeLimit) {
        this.timeLimit = timeLimit;
    }

    public int getTimeToCapture() {
        return timeToCapture;
    }

    public void setTimeToCapture(int timeToCapture) {
        this.timeToCapture = timeToCapture;
    }

    public Rewards getRewards() {
        return rewards;
    }

    public void setRewards(Rewards rewards) {
        this.rewards = rewards;
    }

    public String getTimes() {
        return times;
    }

    public void setTimes(String times) {
        this.times = times;
    }

    public String getDays() {
        return days;
    }

    public void setDays(String days) {
        this.days = days;
    }

    public Location getCenter() {
        if (center != null)
            return center;
        if (firstPosition == null || secondPosition == null)
            return null;
        // Generate Center
        double x = (firstPosition.getX() + secondPosition.getX()) / 2;
        double y = (firstPosition.getY() + secondPosition.getY()) / 2;
        double z = (firstPosition.getZ() + secondPosition.getZ()) / 2;
        center = new Location(getWorld(), x, y, z);
        // Generate Safe center
        safeCenter = center.clone();
        FoliaAPI.runTaskForRegion(center, () -> {
            int attempts = 0;
            while (safeCenter.getBlock().getType().isSolid() && ++attempts < 5) {
                safeCenter.add(0, 1, 0);
            }
            safeCenter.add(0.5, 2, 0.5);
        });
        return center;
    }

    public Location getSafeCenter() {
        if (safeCenter != null)
            return safeCenter;
        else
            getCenter();
        return safeCenter;
    }

    public void spawnHologram() {
        if (safeCenter == null) {
            return;
        }
        Configuration config = MineKoth.getInstance().getConfig();
        String[] lines = config.getString("messages.koth-hologram-lines").split("\n");
        for (int i = 0; i < lines.length; i++) {
            if (lines[i] != null) {
                lines[i] = lines[i].replace("<id>", String.valueOf(id)).replace("<name>", name);
            }
        }
        FoliaAPI.runTask(() -> {
            HologramsAPIUniversal.getHologramsAPI().createHologram("koth_" + id, safeCenter, lines);
            hologramSpawned = true;
        });
    }

    public void despawnHologram() {
        HologramsAPIUniversal.getHologramsAPI().deleteHologram("koth_" + id);
        hologramSpawned = false;
    }

    public boolean isHologramSpawned() {
        return hologramSpawned;
    }
}