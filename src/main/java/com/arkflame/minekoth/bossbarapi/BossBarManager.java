package com.arkflame.minekoth.bossbarapi;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import com.arkflame.minekoth.bossbarapi.bridge.BossBarBridge;
import com.arkflame.minekoth.bossbarapi.bridge.LegacyWitherBossBar;
import com.arkflame.minekoth.bossbarapi.bridge.ModernBossBar;

import java.util.Collections;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Manages all active BossBars, handles version detection, and runs update tasks.
 * This is a static utility class for internal use only.
 */
public final class BossBarManager {
    private static JavaPlugin plugin;
    private static boolean isLegacy;
    private static final Set<BossBarAPI> activeBars = Collections.newSetFromMap(new ConcurrentHashMap<>());

    private BossBarManager() {} // Private constructor for utility class

    public static void init(JavaPlugin pluginInstance) {
        if (plugin != null) {
            return; // Already initialized
        }
        plugin = pluginInstance;

        String version = Bukkit.getServer().getClass().getPackage().getName().split("\\.")[3];
        isLegacy = version.startsWith("v1_8");

        if (isLegacy) {
            Bukkit.getPluginManager().registerEvents(new BossBarListener(), plugin);
            startLegacyUpdateTask();
        }
    }

    static BossBarBridge createBridge() {
        if (plugin == null) {
            throw new IllegalStateException("BossBarManager has not been initialized! Call BossBarManager.init(plugin) in onEnable.");
        }
        // This method now ONLY creates the bridge, not the BossBarAPI instance.
        return isLegacy ? new LegacyWitherBossBar() : new ModernBossBar();
    }
    
    static void addBar(BossBarAPI bar) {
        activeBars.add(bar);
    }

    static void removeBar(BossBarAPI bar) {
        activeBars.remove(bar);
    }
    
    // Package-private for the listener to access
    static Set<BossBarAPI> getActiveBars() {
        return activeBars;
    }
    
    private static void startLegacyUpdateTask() {
        new BukkitRunnable() {
            @Override
            public void run() {
                for (BossBarAPI bar : activeBars) {
                    // Directly iterate over the Player objects.
                    for (Player player : bar.getPlayers()) {
                        if (player.isOnline()) {
                            // Tell the legacy bridge to update the wither position for this player
                            ((LegacyWitherBossBar) bar.getBridge()).updatePosition(player);
                        }
                    }
                }
            }
        }.runTaskTimer(plugin, 40L, 40L); // Update positions every 2 seconds
    }
}