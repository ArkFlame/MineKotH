package com.arkflame.minekoth.particles;

import com.arkflame.minekoth.utils.FoliaAPI;

import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.*;

public class ParticleScheduler {
    private long currentTick = 0;
    private final Map<Long, List<ScheduledParticle>> scheduledParticles = new HashMap<>();
    private final Map<Player, PlayerParticleTrail> playerTrails = new HashMap<>();

    public ParticleScheduler() {
        startScheduler();
    }

    private void startScheduler() {
        FoliaAPI.runTaskTimer(task -> {
            currentTick++;
            runScheduledParticles();
            updatePlayerTrails();
        }, 1L, 1L);
    }

    private void runScheduledParticles() {
        List<ScheduledParticle> particles = scheduledParticles.remove(currentTick);
        if (particles != null) {
            for (ScheduledParticle sp : particles) {
                sp.run();
            }
        }
    }

    private void updatePlayerTrails() {
        for (PlayerParticleTrail trail : playerTrails.values()) {
            if (currentTick % trail.getDelay() == 0) {
                trail.run();
            }
        }
    }

    public void schedule(Location location, String particleName, long ticks) {
        long targetTick = currentTick + ticks;
        ScheduledParticle particle = new ScheduledParticle(location, particleName);
        scheduledParticles.computeIfAbsent(targetTick, k -> new ArrayList<>()).add(particle);
    }

    public void schedule(Player player, String particleName, long ticks) {
        long targetTick = currentTick + ticks;
        ScheduledParticle particle = new ScheduledParticle(player, particleName);
        scheduledParticles.computeIfAbsent(targetTick, k -> new ArrayList<>()).add(particle);
    }

    public void trail(Player player, String particleName) {
        playerTrails.put(player, new PlayerParticleTrail(this, player, particleName, 1, 0));
    }

    public void trail(Player player, String particleName, int delay) {
        playerTrails.put(player, new PlayerParticleTrail(this, player, particleName, delay, 0));
    }

    public void removeTrail(Player player) {
        playerTrails.remove(player);
    }

    // ScheduledParticle Class
    private static class ScheduledParticle {
        private final Location location;
        private final String particleName;
        private final Player player;

        public ScheduledParticle(Location location, String particleName) {
            this.location = location;
            this.particleName = particleName;
            this.player = null;
        }

        public ScheduledParticle(Player player, String particleName) {
            this.player = player;
            this.particleName = particleName;
            this.location = null;
        }

        public void run() {
            if (location != null) {
                ParticleUtil.spawnParticle(location, particleName, 1, 0, 0, 0, 0);
            } else if (player != null && player.isOnline()) {
                ParticleUtil.spawnParticle(player.getLocation(), particleName, 1, 0, 0, 0, 0);
            }
        }
    }

    // PlayerParticleTrail Class
    private static class PlayerParticleTrail {
        private final ParticleScheduler particleScheduler;
        private final Player player;
        private final String particleName;
        private final int delay;
        private final int times;
        private int timesRan = 0;

        public PlayerParticleTrail(ParticleScheduler particleScheduler, Player player, String particleName, int delay, int times) {
            this.particleScheduler = particleScheduler;
            this.player = player;
            this.particleName = particleName;
            this.delay = delay;
            this.times = times;
        }

        public int getDelay() {
            return delay;
        }

        public boolean updateTimesRan() {
            if (timesRan < times || times <= 0) {
                timesRan++;
                return true;
            } else {
                return false;
            }
        }

        public void run() {
            if (player.isOnline() && updateTimesRan()) {
                ParticleUtil.spawnParticle(player.getLocation(), particleName, 1, 0, 0, 0, 0);
            } else {
                particleScheduler.getPlayerTrails().remove(player);
            }
        }
    }

    public void spiralTrail(Player player, String particleName, double radius, double height, int loops, int points, int delay, int times) {
        PlayerParticleTrail spiralTrail = new PlayerParticleTrail(this, player, particleName, delay, times) {
            @Override
            public void run() {
                if (player.isOnline() && updateTimesRan()) {
                    ParticleUtil.generateSpiral(player.getLocation(), particleName, radius, height, loops, points);
                } else {
                    playerTrails.remove(player);
                }
            }
        };
        playerTrails.put(player, spiralTrail);
    }

    public Map<Player, PlayerParticleTrail> getPlayerTrails() {
        return playerTrails;
    }
}
