package com.arkflame.minekoth.koth.events;

import com.arkflame.minekoth.koth.KoTH;
import org.bukkit.Bukkit;

public class KoTHEventManager {

    private KoTHEvent currentEvent;

    /**
     * Starts a new KoTHEvent if one is not already active.
     * 
     * @param koth The KoTH instance for the event.
     * @throws IllegalStateException if an event is already active.
     */
    public void start(KoTH koth) {
        if (currentEvent != null) {
            throw new IllegalStateException("A KoTH event is already active.");
        }
        currentEvent = new KoTHEvent(koth);
        Bukkit.getLogger().info("KoTH event started for: " + koth.getName());
    }

    /**
     * Ends the currently active KoTHEvent.
     * 
     * @throws IllegalStateException if no event is active.
     */
    public void end() {
        if (currentEvent == null) {
            throw new IllegalStateException("No active KoTH event to end.");
        }
        currentEvent.end();
        Bukkit.getLogger().info("KoTH event ended for: " + currentEvent.getKoTH().getName());
        currentEvent = null;
    }

    /**
     * Retrieves the currently active KoTHEvent.
     * 
     * @return The active KoTHEvent, or null if none is active.
     */
    public KoTHEvent getKoTHEvent() {
        return currentEvent;
    }

    /**
     * Checks if a KoTHEvent is currently active.
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
}
