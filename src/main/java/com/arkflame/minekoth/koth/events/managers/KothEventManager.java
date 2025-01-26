package com.arkflame.minekoth.koth.events.managers;

import com.arkflame.minekoth.MineKoth;
import com.arkflame.minekoth.koth.Koth;
import com.arkflame.minekoth.koth.events.KothEvent;
import com.arkflame.minekoth.koth.events.KothEvent.KothEventState;
import com.arkflame.minekoth.utils.DiscordHook;
import com.arkflame.minekoth.utils.FoliaAPI;
import com.arkflame.minekoth.utils.Sounds;
import com.arkflame.minekoth.utils.Titles;

import org.bukkit.FireworkEffect;
import org.bukkit.Location;
import org.bukkit.World;
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
            return;
        }
        currentEvent = new KothEvent(koth);
        Titles.sendTitle(
                "&a" + koth.getName(),
                "&e" + "Capture the Hill!",
                10, 20, 10);
        Sounds.play(1.0f, 1.0f, "BLOCK_NOTE_BLOCK_PLING", "NOTE_PLING");
        for (Player player : MineKoth.getInstance().getServer().getOnlinePlayers()) {
            currentEvent.updatePlayerState(player, player.getLocation(), player.isDead());
        }
        Location center = koth.getCenter();
        if (center != null) {
            World world = center.getWorld();
            if (world != null) {
                world.strikeLightning(center);
            }
        }

        // Notify Discord
        DiscordHook.sendKothStart(koth.getName());
    }

    /**
     * Ends the currently active kothEvent.
     * 
     * @throws IllegalStateException if no event is active.
     */
    public void end() {
        if (currentEvent == null) {
            return;
        }
        currentEvent.clearPlayers();
        currentEvent.end();
        currentEvent = null;
        MineKoth.getInstance().getScheduleManager().calculateNextKoth();
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
            try {
                currentEvent.tick();

                // The koth was captured
                if (currentEvent.getState() == KothEvent.KothEventState.CAPTURED) {
                    Koth koth = currentEvent.getKoth();
                    Location location = koth.getCenter();

                    // Play 3 fireworks in the last 3 seconds
                    FoliaAPI.runTaskForRegion(location, () -> {
                        if (currentEvent != null) {
                            currentEvent.createFirework(location, FireworkEffect.Type.BALL);
                        }
                    });

                    // Check if 3 seconds passed since end
                    if (currentEvent.getTimeSinceEnd() > 3000L) {
                        end();
                    }
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }

    public void updatePlayerState(Player player, Location to) {
        updatePlayerState(player, to, false);
    }

    public void updatePlayerState(Player player, Location to, boolean dead) {
        if (currentEvent != null) {
            if (currentEvent.getState() == KothEventState.CAPTURED) {
                return;
            }
            currentEvent.updatePlayerState(player, to, dead);
        }
    }
}
