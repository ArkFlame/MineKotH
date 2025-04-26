package com.arkflame.minekoth.utils;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

public class GlowingUtility {

    private static final boolean IS_GLOWING_SUPPORTED;
    private static final Map<String, Team> TEAM_CACHE = new HashMap<>();
    private static final Map<String, ChatColor> PLAYER_TO_COLOR_CACHE = new HashMap<>();

    static {
        boolean teamsSupported = false;
        boolean colorSupport = false;
        boolean nameTagVisibilitySupport = false;
        boolean collisionRuleSupport = false;

        try {
            Class<?> teamClass = Class.forName("org.bukkit.scoreboard.Team");
            teamsSupported = true;

            // Check for setColor method
            Method setColorMethod = teamClass.getMethod("setColor", ChatColor.class);
            colorSupport = setColorMethod != null;

            // Check for NAME_TAG_VISIBILITY option
            Method setOptionMethod = teamClass.getMethod("setOption", Team.Option.class, Team.OptionStatus.class);
            if (setOptionMethod != null) {
                nameTagVisibilitySupport = true;
                collisionRuleSupport = true;
            }
        } catch (Exception e) {
            // If any exception occurs, these features are unsupported
        }

        IS_GLOWING_SUPPORTED = teamsSupported && colorSupport && nameTagVisibilitySupport && collisionRuleSupport;
    }

    /**
     * Checks if glowing is supported on the server.
     *
     * @return true if glowing is supported, false otherwise
     */
    public static boolean isGlowingSupported() {
        return IS_GLOWING_SUPPORTED;
    }

    /**
     * Sets a glowing outline on the player with the specified color.
     *
     * @param player The player to glow.
     * @param color  The color of the glowing effect (use ChatColor).
     */
    public static void setGlowing(Player player, ChatColor color) {
        if (!IS_GLOWING_SUPPORTED || player == null || color == null) {
            return; // Glowing not supported or invalid parameters
        }

        Scoreboard scoreboard = Bukkit.getScoreboardManager().getMainScoreboard();
        String teamName = "Glow_" + color.name();
        Team team = TEAM_CACHE.get(teamName);

        try {
            if (team == null) {
                team = scoreboard.getTeam(teamName);
                if (team == null) {
                    team = scoreboard.registerNewTeam(teamName);
                    team.setColor(color);
                    team.setOption(Team.Option.NAME_TAG_VISIBILITY, Team.OptionStatus.ALWAYS);
                    team.setOption(Team.Option.COLLISION_RULE, Team.OptionStatus.NEVER);
                }
                TEAM_CACHE.put(teamName, team);
            }

            if (!team.hasEntry(player.getName())) {
                team.addEntry(player.getName());
                PLAYER_TO_COLOR_CACHE.put(player.getName(), color);
            }
        } catch (UnsupportedOperationException ex) {
            // Folia does not support this
        }

        PotionEffectUtil.applyAllValidEffects(player, 0, Integer.MAX_VALUE, "GLOWING");
    }

    /**
     * Removes the glowing effect from the player.
     *
     * @param player The player to remove glowing from.
     */
    public static void unsetGlowing(Player player) {
        if (!IS_GLOWING_SUPPORTED || player == null) {
            return; // Glowing not supported or invalid parameters
        }

        PotionEffectUtil.removeAllValidEffects(player, "GLOWING");
        
        String playerName = player.getName();
        ChatColor color = PLAYER_TO_COLOR_CACHE.remove(playerName);

        if (color != null) {
            String teamName = "Glow_" + color.name();
            Team team = TEAM_CACHE.get(teamName);
            if (team != null) {
                team.removeEntry(playerName);
            }
        }
    }

    /**
     * Clears all cached teams and player-to-color mappings. Useful for ensuring
     * cache consistency.
     */
    public static void clearCache() {
        TEAM_CACHE.clear();
        PLAYER_TO_COLOR_CACHE.clear();
    }

    /**
     * Removes a specific team from the cache.
     *
     * @param color The color of the team to remove.
     */
    public static void removeCachedTeam(ChatColor color) {
        if (color == null) {
            return;
        }

        String teamName = "Glow_" + color.name();
        TEAM_CACHE.remove(teamName);
    }

    /**
     * Checks if a player is currently glowing.
     *
     * @param player The player to check.
     * @return true if the player is glowing, false otherwise.
     */
    public static boolean isGlowing(Player player) {
        if (!IS_GLOWING_SUPPORTED || player == null) {
            return false;
        }

        return PLAYER_TO_COLOR_CACHE.containsKey(player.getName());
    }

    /**
     * Gets the color assigned to a glowing player.
     *
     * @param player The player to check.
     * @return The color of the glowing effect, or null if the player is not
     *         glowing.
     */
    public static ChatColor getPlayerColor(Player player) {
        if (!IS_GLOWING_SUPPORTED || player == null) {
            return null;
        }

        return PLAYER_TO_COLOR_CACHE.get(player.getName());
    }
}
