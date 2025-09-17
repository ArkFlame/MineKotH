package com.arkflame.minekoth.koth.events.managers;

// FlameSDK BossBar Imports
import com.arkflame.minekoth.bossbarapi.BossBarAPI;
import com.arkflame.minekoth.bossbarapi.enums.BarColor;
import com.arkflame.minekoth.bossbarapi.enums.BarStyle;

// MineKoth Imports
import com.arkflame.minekoth.MineKoth;
import com.arkflame.minekoth.koth.Koth;
import com.arkflame.minekoth.koth.events.CapturingPlayers;
import com.arkflame.minekoth.koth.events.KothEvent;
import com.arkflame.minekoth.koth.events.KothEvent.KothEventState;
import com.arkflame.minekoth.koth.events.config.KothEventsCaptureRewardsConfig;
import com.arkflame.minekoth.lang.Lang;
import com.arkflame.minekoth.lang.LangManager;
import com.arkflame.minekoth.particles.ParticleUtil;
import com.arkflame.minekoth.playerdata.PlayerData;
import com.arkflame.minekoth.utils.*;

// Java Util Imports
import java.util.*;

// Bukkit Imports
import org.bukkit.FireworkEffect;
import org.bukkit.Location;
import org.bukkit.configuration.Configuration;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class KothEventManager {

    private final List<KothEvent> events = new ArrayList<>();
    private final KothEventsCaptureRewardsConfig config;
    private final Map<UUID, BossBarAPI> playerBossBars = new HashMap<>();

    public KothEventManager(Plugin plugin) {
        this.config = new KothEventsCaptureRewardsConfig(plugin.getConfig());
    }

    private KothEvent runNewEvent(Koth koth) {
        KothEvent event = new KothEvent(koth);
        events.add(event);
        return event;
    }

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
        Worlds.strikeLightningEffect(center);
        DiscordHook.sendKothStart(koth.getName());
    }

    public void end(KothEvent currentEvent) {
        if (events.remove(currentEvent)) {
            currentEvent.clear();
        }
        clearAllBossBars();
        MineKoth.getInstance().getScheduleManager().calculateNextKoth();
    }

    public void end() {
        if (!isEventActive()) {
            return;
        }
        for (KothEvent currentEvent : getRunningKoths()) {
            end(currentEvent);
        }
    }

    public void clearAllBossBars() {
        for (BossBarAPI bossBar : playerBossBars.values()) {
            bossBar.destroy();
        }
        playerBossBars.clear();
    }

    public KothEvent getKothEvent() {
        return events.size() > 0 ? events.get(0) : null;
    }

    public boolean isEventActive() {
        return !events.isEmpty();
    }

    public void tick() {
        Set<UUID> playersInAnyZone = new HashSet<>();
        for (KothEvent event : events) {
            if (event.getState() != KothEventState.CAPTURED) {
                for (Player player : event.getPlayersInZone()) {
                    playersInAnyZone.add(player.getUniqueId());
                }
            }
        }

        Iterator<Map.Entry<UUID, BossBarAPI>> iterator = playerBossBars.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<UUID, BossBarAPI> entry = iterator.next();
            if (!playersInAnyZone.contains(entry.getKey())) {
                entry.getValue().destroy();
                iterator.remove();
            }
        }

        for (KothEvent currentEvent : getRunningKoths()) {
            try {
                currentEvent.tick();

                if (currentEvent.getState() == KothEvent.KothEventState.CAPTURED) {
                    handleCapturedState(currentEvent);
                } else {
                    handleActiveState(currentEvent);
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }

    private void handleCapturedState(KothEvent currentEvent) {
        Location location = currentEvent.getKoth().getSafeCenter();
        FoliaAPI.runTaskForRegion(location, () -> {
            currentEvent.createFirework(location, FireworkEffect.Type.BALL);
        });
        if (currentEvent.getTimeSinceEnd() > 3000L) {
            end(currentEvent);
        }
    }

    private void handleActiveState(KothEvent currentEvent) {
        MineKoth.getInstance().getRandomEventsManager().tick(currentEvent);

        CapturingPlayers topGroup = currentEvent.getTopGroup();
        Player topPlayer = (topGroup != null) ? topGroup.getTopPlayer() : null;
        String capturer = (topPlayer != null) ? topPlayer.getName() : "No one";
        int score = (topGroup != null) ? topGroup.getScore() : 0;
        int timeToCapture = currentEvent.getKoth().getTimeToCapture();
        
        double progress = (timeToCapture > 0) ? (double) score / timeToCapture : 0.0;
        progress = Math.max(0.0, Math.min(1.0, progress));

        String timeLeftFormatted = currentEvent.getTimeLeftFormatted();
        boolean sendTimeLeftTitle = MineKoth.getInstance().getConfig().getBoolean("capturing-options.capture-time-goal", true);
        LangManager langManager = MineKoth.getInstance().getLangManager();

        for (Player player : currentEvent.getPlayersInZone()) {
            boolean isTopPlayer = player.equals(topPlayer);
            boolean isTopGroup = topGroup != null && topGroup.containsPlayer(player);
            
            updatePlayerBossBar(player, isTopPlayer, isTopGroup, capturer, timeLeftFormatted, progress, langManager);
            
            // FIX: Pass the 'currentEvent' to the method so it can check the time remaining
            updatePlayerActionAndTitle(player, isTopPlayer, isTopGroup, capturer, timeLeftFormatted, sendTimeLeftTitle, langManager, currentEvent);
            
            updatePlayerStatsAndRewards(player, currentEvent, topGroup);
        }

        double safeY = currentEvent.getKoth().getSafeCenter().getY() + 0.5;
        Location firstLocation = currentEvent.getKoth().getFirstLocation().clone();
        Location secondLocation = currentEvent.getKoth().getSecondLocation().clone();
        firstLocation.setY(safeY);
        secondLocation.setY(safeY);
        ParticleUtil.generatePerimeter(firstLocation, secondLocation, "COLOURED_DUST", 100);
    }

    private void updatePlayerBossBar(Player player, boolean isTopPlayer, boolean isTopGroup, String capturer,
            String timeLeft, double progress, LangManager langManager) {
        BossBarAPI bossBar = playerBossBars.computeIfAbsent(player.getUniqueId(), k -> BossBarAPI.create()
                .style(BarStyle.SEGMENTED_20)
                .addPlayer(player));

        String bossBarText;
        BarColor bossBarColor;
        Lang lang = langManager.getLang(player);

        if (isTopPlayer) {
            bossBarText = lang.getMessage("messages.bossbar-capturing-self");
            bossBarColor = BarColor.GREEN;
        } else if (isTopGroup) {
            bossBarText = lang.getMessage("messages.bossbar-capturing-team").replace("<player>", capturer);
            bossBarColor = BarColor.BLUE;
        } else {
            bossBarText = lang.getMessage("messages.bossbar-capturing-enemy").replace("<player>", capturer);
            bossBarColor = BarColor.RED;
        }

        bossBar.text(bossBarText.replace("<time-left>", timeLeft))
                .color(bossBarColor)
                .progress(progress);
    }

    /**
     * Sends ActionBars and Titles to a specific player.
     * FIX: This method now includes a timer to prevent spamming the title every tick.
     */
    private void updatePlayerActionAndTitle(Player player, boolean isTopPlayer, boolean isTopGroup, String capturer,
            String timeLeft, boolean sendTitleFromConfig, LangManager langManager, KothEvent currentEvent) {
        // ActionBar (sent every tick)
        if (isTopPlayer) {
            langManager.sendAction(player, "messages.you-are-capturing-action", "<time-left>", timeLeft);
        } else if (isTopGroup) {
            langManager.sendAction(player, "messages.capturing-action-team", "<player>", capturer, "<time-left>", timeLeft);
        } else {
            langManager.sendAction(player, "messages.capturing-action-enemy", "<player>", capturer, "<time-left>", timeLeft);
        }

        // Title (sent only at specific intervals)
        if (sendTitleFromConfig) {
            // Get the remaining time in seconds to perform a check
            int timeLeftSeconds = currentEvent.getTimeLeftToCapture();

            // Condition to show title:
            // 1. Every second if time left is 10 seconds or less.
            // 2. Every 15 seconds otherwise (e.g., at 45, 30, 15 seconds).
            // 3. Does not show at 0 to avoid overlapping with the capture message.
            boolean shouldShowTitle = (timeLeftSeconds > 0) &&
                                      ((timeLeftSeconds <= 10) || (timeLeftSeconds % 15 == 0));

            if (shouldShowTitle) {
                Lang lang = langManager.getLang(player);
                String subtitle = isTopPlayer
                        ? lang.getMessage("messages.you-are-capturing-subtitle")
                        : isTopGroup
                                ? lang.getMessage("messages.top-player-name-capturing-subtitle").replace("<topPlayerName>", capturer)
                                : lang.getMessage("messages.not-top-player-name-capturing-subtitle").replace("<topPlayerName>", capturer);

                Titles.sendTitle(player,
                        lang.getMessage("messages.seconds-left").replace("<seconds>", timeLeft),
                        subtitle,
                        10, 20, 10);
                Sounds.play(player, 1.0f, 1.0f, "CLICK");
            }
        }
    }

    private void updatePlayerStatsAndRewards(Player player, KothEvent currentEvent, CapturingPlayers topGroup) {
        if (PotionEffectUtil.removeEffect(player, PotionEffectType.INVISIBILITY)) {
            Titles.sendTitle(player, "", MineKoth.getInstance().getLangManager().getLang(player).getMessage("messages.revealed"), 10, 20, 10);
        }

        if (topGroup == null || topGroup.containsPlayer(player)) {
            Configuration config = MineKoth.getInstance().getConfig();
            if (config.getBoolean("capturing-effects.enabled")) {
                ConfigurationSection effectsSection = config.getConfigurationSection("capturing-effects.effects");
                List<PotionEffect> effectsToApply = PotionEffectUtil.readEffectsFromConfig(effectsSection);
                if (effectsToApply != null && !effectsToApply.isEmpty()) {
                    MineKoth.getInstance().getServer().getScheduler().runTask(MineKoth.getInstance(), () -> {
                        for (PotionEffect effect : effectsToApply) {
                            player.addPotionEffect(effect);
                        }
                    });
                }
            }
        }

        PlayerData playerData = MineKoth.getInstance().getPlayerDataManager().getIfLoaded(player.getUniqueId().toString());
        if (playerData != null) {
            playerData.addCaptureTime(currentEvent.getKoth().getId(), 1);
        }
        currentEvent.getStats().updateCapture(player.getUniqueId());
        long timeCaptured = currentEvent.getStats().getPlayerStats(player.getUniqueId()).getTotalTimeCaptured();
        if (timeCaptured == 1) {
            if (playerData != null) {
                playerData.incrementParticipationCount(currentEvent.getKoth().getId());
            }
            player.sendMessage(MineKoth.getInstance().getLangManager().getLang(player).getMessage("messages.stay-to-earn-loot"));
        }
        config.giveRewards(player, timeCaptured);
    }

    public void updatePlayerState(Player player, Location to) {
        updatePlayerState(player, to, false);
    }

    public void updatePlayerState(Player player, Location to, boolean dead) {
        for (KothEvent currentEvent : events) {
            if (currentEvent.getState() == KothEventState.CAPTURED) {
                return;
            }
            currentEvent.updatePlayerState(player, to, dead);
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