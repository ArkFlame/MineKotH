package com.arkflame.minekoth.koth.events.managers;

import com.arkflame.minekoth.koth.Koth;
import com.arkflame.minekoth.koth.events.KothEvent;
import com.arkflame.minekoth.utils.Sounds;
import com.arkflame.minekoth.utils.Titles;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Player;

public class KothEventManager {

    private KothEvent currentEvent;

    /**
     * Starts a new kothEvent if one is not already active.
     * 
     * @param koth The koth instance for the event.
     * @throws IllegalStateException if an event is already active.
     */
    public void start(Koth koth) {
        if (currentEvent != null) {
            throw new IllegalStateException("A koth event is already active.");
        }
        currentEvent = new KothEvent(koth);
        Titles.sendTitle(
                ChatColor.GREEN + koth.getName(),
                ChatColor.YELLOW + "Capture the Hill!",
                10, 20, 10);
        Sounds.play(1.0f, 1.0f, "BLOCK_NOTE_BLOCK_PLING", "PLING");
    }

    /**
     * Ends the currently active kothEvent.
     * 
     * @throws IllegalStateException if no event is active.
     */
    public void end() {
        if (currentEvent == null) {
            throw new IllegalStateException("No active koth event to end.");
        }
        currentEvent.end();
        Bukkit.getLogger().info("koth event ended for: " + currentEvent.getKoth().getName());
        currentEvent = null;
    }

    /**
     * Retrieves the currently active kothEvent.
     * 
     * @return The active kothEvent, or null if none is active.
     */
    public KothEvent getKothEvent() {
        return currentEvent;
    }

    /**
     * Checks if a kothEvent is currently active.
     * 
     * @return True if an event is active, false otherwise.
     */
    public boolean isEventActive() {
        return currentEvent != null;
    }

    /**
     * Ticks the active event, if any.
     * This should be called periodically (e.g., in a scheduled task).
     */
    public void tick() {
        if (currentEvent != null) {
            currentEvent.tick();
        }
    }

    public void updatePlayerState(Player player, Location to) {
        if (currentEvent != null) {
            currentEvent.updatePlayerState(player, to);
        }
    }
}
