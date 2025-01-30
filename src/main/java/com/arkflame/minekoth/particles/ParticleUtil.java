package com.arkflame.minekoth.particles;

import org.bukkit.Bukkit;
import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import java.util.HashMap;
import java.util.Map;

public class ParticleUtil {

    private static final Map<String, Boolean> classExistsCache = new HashMap<>();

    private static boolean doesClassExist(String className) {
        return classExistsCache.computeIfAbsent(className, key -> {
            try {
                Class.forName(key);
                return true;
            } catch (ClassNotFoundException e) {
                return false;
            }
        });
    }

    public static void spawnParticle(Location location, String name, int count, double offsetX, double offsetY,
            double offsetZ, int extra) {
        Object particleOrEffect = getParticleOrEffect(name);

        if (particleOrEffect == null)
            return;

        if (particleOrEffect.getClass().getSimpleName().equals("Particle")) {
            try {
                location.getWorld().spawnParticle((Particle) particleOrEffect, location, count, offsetX, offsetY,
                        offsetZ, extra);
            } catch (Exception e) {
                for (Player player : Bukkit.getOnlinePlayers()) {
                    if (player.getWorld().equals(location.getWorld())) {
                        player.spawnParticle((Particle) particleOrEffect, location, count, offsetX, offsetY,
                                offsetZ, extra);
                    }
                }
            }
        } else if (particleOrEffect.getClass().getSimpleName().equals("Effect")) {
            try {
                for (int i = 0; i < count; i++) {
                    location.getWorld().playEffect(location, (Effect) particleOrEffect, extra);
                }
            } catch (Exception e) {
                // Ignore
            }
        }
    }

    public static void generateSpiral(Location center, String name, double radius, double height, int loops,
            int points) {
        for (int i = 0; i < loops * points; i++) {
            double angle = 2 * Math.PI * i / points;
            double x = radius * Math.cos(angle);
            double z = radius * Math.sin(angle);
            double y = height * i / (loops * points);

            Location particleLocation = center.clone().add(x, y, z);
            spawnParticle(particleLocation, name, 1, 0, 0, 0, 0);
        }
    }

    public static void generatePerimeter(Location loc1, Location loc2, String name, int count) {
        // Ensure loc1 is the top-left and loc2 is the bottom-right
        double minX = Math.min(loc1.getX(), loc2.getX());
        double maxX = Math.max(loc1.getX(), loc2.getX());
        double minZ = Math.min(loc1.getZ(), loc2.getZ());
        double maxZ = Math.max(loc1.getZ(), loc2.getZ());
    
        // Calculate step size for x and z directions
        double stepX = (maxX - minX) / count;
        double stepZ = (maxZ - minZ) / count;
    
        // Generate perimeter by creating four edges
        // Top edge
        for (int i = 0; i <= count; i++) {
            Location particleLocation = new Location(loc1.getWorld(), minX + (stepX * i), loc1.getY(), minZ);
            spawnParticle(particleLocation, name, 1, 0, 0, 0, 0);
        }
    
        // Right edge
        for (int i = 0; i <= count; i++) {
            Location particleLocation = new Location(loc1.getWorld(), maxX + 1, loc1.getY(), minZ + (stepZ * i));
            spawnParticle(particleLocation, name, 1, 0, 0, 0, 0);
        }
    
        // Bottom edge
        for (int i = 0; i <= count; i++) {
            Location particleLocation = new Location(loc1.getWorld(), maxX - (stepX * i), loc1.getY(), maxZ + 1);
            spawnParticle(particleLocation, name, 1, 0, 0, 0, 0);
        }
    
        // Left edge
        for (int i = 0; i <= count; i++) {
            Location particleLocation = new Location(loc1.getWorld(), minX, loc1.getY(), maxZ - (stepZ * i));
            spawnParticle(particleLocation, name, 1, 0, 0, 0, 0);
        }
    }

    public static void generateCircle(Location center, String name, double radius, int points) {
        for (int i = 0; i < points; i++) {
            double angle = 2 * Math.PI * i / points;
            double x = radius * Math.cos(angle);
            double z = radius * Math.sin(angle);

            Location particleLocation = center.clone().add(x, 0, z);
            spawnParticle(particleLocation, name, 1, 0, 0, 0, 0);
        }
    }

    public static void generateLine(Location loc1, Location loc2, String name, int count) {
        Vector direction = loc2.toVector().subtract(loc1.toVector());
        double distance = loc1.distance(loc2);
        Vector step = direction.normalize().multiply(distance / count);

        for (int i = 0; i <= count; i++) {
            Location particleLocation = loc1.clone().add(step.clone().multiply(i));
            spawnParticle(particleLocation, name, 1, 0, 0, 0, 0);
        }
    }

    public static Object getParticleOrEffect(String... names) {
        for (String name : names) {
            if (name == null)
                continue; // Skip null names

            if (doesClassExist("org.bukkit.Particle")) {
                try {
                    return Particle.valueOf(name); // Attempt to get Particle
                } catch (IllegalArgumentException ignored) {
                }
            }

            try {
                return Effect.valueOf(name); // Attempt to get Effect
            } catch (IllegalArgumentException ignored) {
                // Continue to the next name if current one is invalid
            }
        }

        return null; // Return null if no valid name is found
    }
}
