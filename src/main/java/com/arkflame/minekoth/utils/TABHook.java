package com.arkflame.minekoth.utils;

import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

import com.arkflame.minekoth.MineKoth;

import me.neznamy.tab.api.TabAPI;
import me.neznamy.tab.api.TabPlayer;
import me.neznamy.tab.api.scoreboard.Scoreboard;
import me.neznamy.tab.api.scoreboard.ScoreboardManager;

public class TABHook {

    private static Scoreboard kothScoreboard;
    private static boolean hookActive = false;
    private static final String SCOREBOARD_NAME = "MineKothSB";

    /**
     * Checks if the hook should be active (Plugin enabled + Config enabled)
     */
    public static boolean isAvailable() {
        return Bukkit.getPluginManager().isPluginEnabled("TAB") &&
               MineKoth.getInstance().getConfig().getBoolean("tab-hook.enabled");
    }

    /**
     * Called when a KOTH starts. Forces the scoreboard if it's not already active.
     */
    public static void updateKothStart() {
        if (!isAvailable()) return;

        // If the scoreboard is already active, we don't need to recreate it,
        // just ensure all players see it (in case of reloads or joins).
        if (hookActive && kothScoreboard != null) {
            showToAll();
            return;
        }

        // Initialize and show
        createScoreboard();
        if (kothScoreboard != null) {
            hookActive = true;
            showToAll();
        }
    }

    /**
     * Called when a KOTH ends. 
     * @param areOtherKothsRunning If true, we keep the scoreboard.
     */
    public static void updateKothEnd(boolean areOtherKothsRunning) {
        if (!isAvailable()) return;

        // If there are still events running, do not unforce the scoreboard.
        if (areOtherKothsRunning) {
            return;
        }

        // No events running, disable the scoreboard
        disable();
    }

    private static void createScoreboard() {
        try {
            TabAPI api = TabAPI.getInstance();
            ScoreboardManager manager = api.getScoreboardManager();
            
            if (manager == null) return;

            ConfigurationSection section = MineKoth.getInstance().getConfig().getConfigurationSection("tab-hook.scoreboard");
            if (section == null) return;

            // Clean up existing scoreboard with the same name if it exists (prevents duplicates on reload)
            if (manager.getRegisteredScoreboards().containsKey(SCOREBOARD_NAME)) {
                manager.removeScoreboard(SCOREBOARD_NAME);
            }

            String title = section.getString("title", "&6&lMINEKOTH");
            List<String> lines = section.getStringList("lines");

            // Register the scoreboard within TAB
            kothScoreboard = manager.createScoreboard(SCOREBOARD_NAME, title, lines);
            
        } catch (Exception e) {
            MineKoth.getInstance().getLogger().warning("Failed to create TAB scoreboard: " + e.getMessage());
        }
    }

    private static void showToAll() {
        if (kothScoreboard == null) return;
        
        TabAPI api = TabAPI.getInstance();
        ScoreboardManager manager = api.getScoreboardManager();
        
        if (manager == null) return;
        
        // Iterate over TAB players directly
        for (TabPlayer tabPlayer : api.getOnlinePlayers()) {
            if (tabPlayer.isLoaded()) {
                manager.showScoreboard(tabPlayer, kothScoreboard);
            }
        }
    }

    private static void disable() {
        if (kothScoreboard == null) return;

        TabAPI api = TabAPI.getInstance();
        ScoreboardManager manager = api.getScoreboardManager();

        if (manager != null) {
            // Reset players who are currently viewing our scoreboard back to default
            for (TabPlayer tabPlayer : api.getOnlinePlayers()) {
                Scoreboard active = manager.getActiveScoreboard(tabPlayer);
                if (active != null && active.getName().equals(SCOREBOARD_NAME)) {
                    manager.resetScoreboard(tabPlayer);
                }
            }
        }

        // Unregister the scoreboard from the manager
        kothScoreboard.unregister();
        kothScoreboard = null;
        hookActive = false;
    }
    
    /**
     * Helper to show scoreboard to a single player (e.g. on Join)
     */
    public static void showToPlayer(Player player) {
        if (hookActive && kothScoreboard != null) {
            TabAPI api = TabAPI.getInstance();
            ScoreboardManager manager = api.getScoreboardManager();
            
            if (manager == null) return;

            TabPlayer tabPlayer = api.getPlayer(player.getUniqueId());
            if (tabPlayer != null && tabPlayer.isLoaded()) {
                manager.showScoreboard(tabPlayer, kothScoreboard);
            }
        }
    }
}