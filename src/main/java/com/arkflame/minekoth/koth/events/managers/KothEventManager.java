package com.arkflame.minekoth.koth.events.managers;

import com.arkflame.minekoth.MineKoth;
import com.arkflame.minekoth.koth.Koth;
import com.arkflame.minekoth.koth.events.CapturingPlayers;
import com.arkflame.minekoth.koth.events.KothEvent;
import com.arkflame.minekoth.koth.events.KothEvent.KothEventState;
import com.arkflame.minekoth.koth.events.config.KothEventsCaptureRewardsConfig;
import com.arkflame.minekoth.particles.ParticleUtil;
import com.arkflame.minekoth.playerdata.PlayerData;
import com.arkflame.minekoth.utils.ChatColors;
import com.arkflame.minekoth.utils.DiscordHook;
import com.arkflame.minekoth.utils.FoliaAPI;
import com.arkflame.minekoth.utils.PotionEffectUtil;
import com.arkflame.minekoth.utils.Sounds;
import com.arkflame.minekoth.utils.Titles;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.bukkit.FireworkEffect;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.Configuration;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class KothEventManager {

    private List<KothEvent> events = new ArrayList<>();
    private KothEventsCaptureRewardsConfig config;

    public KothEventManager(Plugin plugin) {
        config = new KothEventsCaptureRewardsConfig(plugin.getConfig());
    }

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
        Sounds.play(1.0f, 1.0f, "BLOCK_NOTE_BLOCK_PLING", "NOTE_PLING");
        for (Player player : MineKoth.getInstance().getServer().getOnlinePlayers()) {
            currentEvent.updatePlayerState(player, player.getLocation(), player.isDead());
            ChatColors.sendMessage(player,
                    MineKoth.getInstance().getLangManager().getLang(player).getMessage("messages.place-bets"));
            Titles.sendTitle(player,
                    MineKoth.getInstance().getLangManager().getLang(player)
                            .getMessage("messages.capture-the-hill-title").replace("<kothName>", koth.getName()),
                    MineKoth.getInstance().getLangManager().getLang(player)
                            .getMessage("messages.capture-the-hill-subtitle").replace("<kothName>", koth.getName()),
                    10, 20, 10);
        }
        Location center = koth.getCenter();
        if (center != null) {
            World world = center.getWorld();
            if (world != null) {
                world.strikeLightningEffect(center);
            }
        }

        // Notify Discord
        DiscordHook.sendKothStart(koth.getName());
    }

    private void end(KothEvent currentEvent) {
        currentEvent.clearPlayers();
        currentEvent.setCaptured();
        if (events.contains(currentEvent)) {
            events.remove(currentEvent);
        }
        MineKoth.getInstance().getScheduleManager().calculateNextKoth();
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
                        MineKoth.getInstance().getRandomEventsManager().tick(currentEvent);

                        for (Player player : currentEvent.getPlayersInZone()) {
                            if (PotionEffectUtil.removeEffect(player, PotionEffectType.INVISIBILITY)) {
                                Titles.sendTitle(player, "", MineKoth.getInstance().getLangManager().getLang(player)
                                        .getMessage("messages.revealed"), 10, 20, 10);
                            }

                            CapturingPlayers topGroup = currentEvent.getTopGroup();
                            if (topGroup == null || topGroup.containsPlayer(player)) {
                                Configuration config = MineKoth.getInstance().getConfig();
                                if (config.getBoolean("capturing-effects.enabled")) {
                                    ConfigurationSection effectsSection = config
                                            .getConfigurationSection("capturing-effects.effects");
                                    List<PotionEffect> effectsToApply = PotionEffectUtil
                                            .readEffectsFromConfig(effectsSection);
                                    if (effectsToApply != null) {
                                        MineKoth.getInstance().getServer().getScheduler()
                                                .runTask(MineKoth.getInstance(), () -> {
                                                    for (PotionEffect effect : effectsToApply) {
                                                        player.addPotionEffect(effect);
                                                    }
                                                });
                                    }
                                }
                            }

                            PlayerData playerData = MineKoth.getInstance().getPlayerDataManager()
                                    .getIfLoaded(player.getUniqueId().toString());
                            if (playerData != null) {
                                playerData.addCaptureTime(currentEvent.getKoth().getId(), 1);
                            }
                            currentEvent.getStats().updateCapture(player.getUniqueId());
                            long timeCaptured = currentEvent.getStats().getPlayerStats(player.getUniqueId())
                                    .getTotalTimeCaptured();
                            if (timeCaptured == 1) {
                                if (playerData != null) {
                                    playerData.incrementParticipationCount(currentEvent.getKoth().getId());
                                }
                                player.sendMessage(MineKoth.getInstance().getLangManager().getLang(player)
                                        .getMessage("messages.stay-to-earn-loot"));
                            }

                            config.giveRewards(player, timeCaptured);
                        }

                        ParticleUtil.generatePerimeter(currentEvent.getKoth().getFirstLocation().add(0, 0.5, 0),
                                currentEvent.getKoth().getSecondLocation().add(0, 0.5, 0), "COLOURED_DUST", 100);
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

    public KothEvent getKothEvent(Koth betKoth) {
        for (KothEvent currentEvent : events) {
            if (currentEvent.getKoth() == betKoth) {
                return currentEvent;
            }
        }
        return null;
    }
}
