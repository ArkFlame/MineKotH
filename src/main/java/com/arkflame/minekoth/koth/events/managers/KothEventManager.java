package com.arkflame.minekoth.koth.events.managers;

import com.arkflame.minekoth.MineKoth;
import com.arkflame.minekoth.koth.Koth;
import com.arkflame.minekoth.koth.events.KothEvent;
import com.arkflame.minekoth.koth.events.KothEvent.KothEventState;
import com.arkflame.minekoth.utils.DiscordHook;
import com.arkflame.minekoth.utils.FoliaAPI;
import com.arkflame.minekoth.utils.Sounds;
import com.arkflame.minekoth.utils.Titles;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.FireworkEffect;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffectType;

public class KothEventManager {

    private List<KothEvent> events = new ArrayList<>();

    private KothEvent runNewEvent(Koth koth) {
        KothEvent event = new KothEvent(koth);
        events.add(event);
        return event;
    }

    /**
     * Starts a new kothEvent if one is not already active.
     * 
     * @param koth The koth instance for the event.
     * @throws IllegalStateException if an event is already active.
     */
    public void start(Koth koth) {
        KothEvent currentEvent = runNewEvent(koth);
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

    private void end(KothEvent currentEvent) {
        currentEvent.clearPlayers();
        currentEvent.end();
        if (events.contains(currentEvent)) {
            events.remove(currentEvent);
        }
    }

    /**
     * Ends the currently active kothEvent.
     * 
     * @throws IllegalStateException if no event is active.
     */
    public void end() {
        if (!isEventActive()) {
            return;
        }
        Iterator<KothEvent> iterator = events.iterator();
        while (iterator.hasNext()) {
            KothEvent currentEvent = iterator.next();
            iterator.remove();
            end(currentEvent);
        }
        MineKoth.getInstance().getScheduleManager().calculateNextKoth();
    }

    /**
     * Retrieves the currently active kothEvent.
     * 
     * @return The active kothEvent, or null if none is active.
     */
    public KothEvent getKothEvent() {
        return events.size() > 0 ? events.get(0) : null;
    }

    /**
     * Checks if a kothEvent is currently active.
     * 
     * @return True if an event is active, false otherwise.
     */
    public boolean isEventActive() {
        return !events.isEmpty();
    }

    /**
     * Ticks the active event, if any.
     * This should be called periodically (e.g., in a scheduled task).
     */
    public void tick() {
        for (KothEvent currentEvent : getRunningKoths()) {
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
                            end(currentEvent);
                        }
                    } else {
                        for (Player player : currentEvent.getPlayersInZone()) {
                            if (!player.isDead() && player.hasPotionEffect(PotionEffectType.INVISIBILITY)) {
                                FoliaAPI.runTaskForRegion(player.getLocation(), () -> {
                                    player.removePotionEffect(PotionEffectType.INVISIBILITY);
                                    player.sendMessage(ChatColor.RED + "You have been revealed!");
                                });
                            }
                        }
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        }
    }

    public void updatePlayerState(Player player, Location to) {
        updatePlayerState(player, to, false);
    }

    public void updatePlayerState(Player player, Location to, boolean dead) {
        for (KothEvent currentEvent : events) {
            if (currentEvent != null) {
                if (currentEvent.getState() == KothEventState.CAPTURED) {
                    return;
                }
                currentEvent.updatePlayerState(player, to, dead);
            }
        }
    }

    public KothEvent[] getRunningKoths() {
        return events.toArray(new KothEvent[0]);
    }
}
