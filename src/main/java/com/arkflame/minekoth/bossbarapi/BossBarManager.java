package com.arkflame.minekoth.bossbarapi;

import com.arkflame.minekoth.bossbarapi.bridge.BossBarBridge;
import com.arkflame.minekoth.bossbarapi.bridge.LegacyWitherBossBar;
import com.arkflame.minekoth.bossbarapi.bridge.ModernBossBar;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.Collections;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Manages all active BossBars, handles version detection, and runs update tasks.
 * This class provides multi-version support for Minecraft 1.8 through 1.21+.
 * It must be initialized in your plugin's onEnable method.
 */
public final class BossBarManager {

    /**
     * A reference to the plugin instance.
     */
    private static JavaPlugin plugin;

    /**
     * A flag to determine if the server is running on a modern version (1.9+)
     * that has the native BossBar API.
     * This is determined by checking for the existence of the `org.bukkit.boss.BossBar` class.
     * This is the most reliable method for multi-version support.
     */
    private static final boolean IS_MODERN_VERSION;

    /**
     * A thread-safe set of all currently active boss bars.
     * Using a ConcurrentHashMap-backed set ensures safety when iterating
     * in the update task while bars might be added or removed from other threads.
     */
    private static final Set<BossBarAPI> activeBars = Collections.newSetFromMap(new ConcurrentHashMap<BossBarAPI, Boolean>());

    /**
     * The task that periodically updates the position of legacy withers.
     * Stored so it can be cancelled on shutdown.
     */
    private static BukkitTask legacyUpdateTask;

    // Static initializer block to perform version detection once when the class is loaded.
    static {
        boolean modern = false;
        try {
            // This class only exists in Spigot 1.9 and above.
            Class.forName("org.bukkit.boss.BossBar");
            modern = true;
        } catch (ClassNotFoundException e) {
            // Class not found, so we are on a legacy version (1.8.x).
            modern = false;
        }
        IS_MODERN_VERSION = modern;
    }

    /**
     * Private constructor to prevent instantiation of this utility class.
     */
    private BossBarManager() {}

    /**
     * Initializes the BossBarManager. This MUST be called in your plugin's `onEnable` method.
     *
     * @param pluginInstance The instance of your JavaPlugin.
     */
    public static void init(JavaPlugin pluginInstance) {
        if (plugin != null) {
            // Avoid re-initialization
            return;
        }
        plugin = pluginInstance;

        // On legacy versions (1.8), we need to use a fake Wither.
        // This requires a listener to handle players moving and a task to update the Wither's position.
        if (!IS_MODERN_VERSION) {
            Bukkit.getPluginManager().registerEvents(new BossBarListener(), plugin);
            startLegacyWitherUpdateTask();
            plugin.getLogger().info("BossBarAPI running in Legacy Mode (for MC 1.8).");
        } else {
            plugin.getLogger().info("BossBarAPI running in Modern Mode (for MC 1.9+).");
        }
    }

    /**
     * Shuts down the BossBarManager, cancelling tasks and clearing active bars.
     * This should be called in your plugin's `onDisable` method to prevent memory leaks.
     */
    public static void shutdown() {
        // Cancel the legacy update task if it's running
        if (legacyUpdateTask != null) {
            legacyUpdateTask.cancel();
            legacyUpdateTask = null;
        }

        // Destroy all active boss bars to remove them from players' screens
        for (BossBarAPI bar : activeBars) {
            bar.destroy();
        }
        activeBars.clear();

        plugin = null;
    }

    /**
     * Creates a version-specific bridge for a new BossBar.
     * This method is for internal use by the BossBarAPI.
     *
     * @return A {@link ModernBossBar} on 1.9+ or a {@link LegacyWitherBossBar} on 1.8.
     * @throws IllegalStateException if the manager has not been initialized.
     */
    static BossBarBridge createBridge() {
        if (plugin == null) {
            throw new IllegalStateException("BossBarManager has not been initialized! Call BossBarManager.init(plugin) in onEnable.");
        }
        return IS_MODERN_VERSION ? new ModernBossBar() : new LegacyWitherBossBar();
    }
    
    /**
     * Adds a bar to the set of active bars. For internal use.
     * @param bar The boss bar to add.
     */
    static void addBar(BossBarAPI bar) {
        activeBars.add(bar);
    }

    /**
     * Removes a bar from the set of active bars. For internal use.
     * @param bar The boss bar to remove.
     */
    static void removeBar(BossBarAPI bar) {
        activeBars.remove(bar);
    }
    
    /**
     * Returns the set of active bars. For internal use by the listener.
     * @return An unmodifiable set of active boss bars.
     */
    static Set<BossBarAPI> getActiveBars() {
        return Collections.unmodifiableSet(activeBars);
    }
    
    /**
     * Starts a repeating task to update the position of legacy withers.
     * The fake wither needs to be teleported periodically to stay in front of the player
     * and remain visible, as it will despawn if it gets too far away.
     */
    private static void startLegacyWitherUpdateTask() {
        legacyUpdateTask = new BukkitRunnable() {
            @Override
            public void run() {
                // Iterate over a copy to prevent ConcurrentModificationException, though the set is thread-safe
                for (BossBarAPI bar : activeBars) {
                    // This bar uses a legacy wither, so we need to update its position for each player
                    if (bar.getBridge() instanceof LegacyWitherBossBar) {
                        LegacyWitherBossBar legacyBridge = (LegacyWitherBossBar) bar.getBridge();
                        for (Player player : bar.getPlayers()) {
                            // Ensure the player is still online before trying to update
                            if (player.isOnline()) {
                                legacyBridge.updatePosition(player);
                            }
                        }
                    }
                }
            }
        }.runTaskTimer(plugin, 40L, 40L); // Run every 2 seconds (40 ticks)
    }
}