package com.arkflame.minekoth.koth.events;

import com.arkflame.minekoth.MineKoth;
import com.arkflame.minekoth.koth.Koth;
import com.arkflame.minekoth.koth.events.bets.KothEventBets;
import com.arkflame.minekoth.koth.rewards.Rewards;
import com.arkflame.minekoth.koth.rewards.Rewards.LootType;
import com.arkflame.minekoth.lang.Lang;
import com.arkflame.minekoth.playerdata.PlayerData;
import com.arkflame.minekoth.utils.DiscordHook;
import com.arkflame.minekoth.utils.FoliaAPI;
import com.arkflame.minekoth.utils.GlowingUtility;
import com.arkflame.minekoth.utils.Sounds;
import com.arkflame.minekoth.utils.Times;
import com.arkflame.minekoth.utils.Titles;

import org.bukkit.entity.Player;
import org.bukkit.Location;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.FireworkEffect;
import org.bukkit.inventory.meta.FireworkMeta;
import org.bukkit.entity.Firework;

import java.util.*;

public class KothEvent {
    private static final List<Integer> COUNTDOWN_INTERVALS = Arrays.asList(60, 30, 15, 10, 5, 4, 3, 2, 1);

    public enum KothEventState {
        UNCAPTURED,
        CAPTURING,
        STALEMATE,
        CAPTURED;

        public String getFancyName() {
            return MineKoth.getInstance().getConfig().getString("messages.koth-states." + this.name());
        }
    }

    private final Koth koth;
    private KothEventState state;
    private boolean stalemateEnabled;
    private long startTime;
    private long endTime;
    private KothEventStats stats;
    private KothEventBets bets;
    private KothEventCaptureState captureState;
    private Collection<UUID> participated;

    public KothEvent(Koth koth) {
        this.koth = koth;
        this.state = KothEventState.UNCAPTURED;
        this.stalemateEnabled = false;
        this.startTime = System.currentTimeMillis();
        this.stats = new KothEventStats();
        this.bets = new KothEventBets();
        this.captureState = new KothEventCaptureState(koth.getTimeToCapture());
        this.participated = new HashSet<>();
    }

    public KothEventStats getStats() {
        return stats;
    }

    public KothEventState getState() {
        return state;
    }

    public boolean isStalemateEnabled() {
        return stalemateEnabled;
    }

    public void setStalemateEnabled(boolean stalemateEnabled) {
        this.stalemateEnabled = stalemateEnabled;
    }

    public void updateCapturingParticles(Player player) {
        if (player == null)
            return;

        if (captureState.isTopPlayer(player) && captureState.isInZone(player)) {
            applyCapturingParticles(player);
        } else {
            clearParticles(player);
        }
    }

    public void enterKoth(Player player) {
        CapturingPlayers oldTopGroup = captureState.getTopGroup();
        if (!captureState.isInZone(player)) {
            Player oldTopPlayer = captureState.getTopPlayer();

            // Update capturing players
            captureState.addToCapturingPlayers(player);
            updateCapturingGroup(oldTopGroup);

            // Update effects
            if (MineKoth.getInstance().getConfig().getBoolean("capturing-particles.enabled")) {
                updateCapturingParticles(player);
                updateCapturingParticles(oldTopPlayer);
            }

            Player topPlayer = captureState.getTopPlayer();
            if (player == topPlayer) {
                Lang lang = MineKoth.getInstance().getLangManager().getLang(player);
                Titles.sendTitle(player,
                        lang.getMessage("messages.capturing-title"),
                        lang.getMessage("messages.capturing-subtitle"),
                        10, 20, 10);
                // Broadcast player capturing now
                if (MineKoth.getInstance().getConfig().getBoolean("capturing-broadcast.enabled")) {
                    for (Player p1 : Bukkit.getOnlinePlayers()) {
                        String message = MineKoth.getInstance().getLangManager().getLang(p1).getMessage(
                                "messages.capturing-broadcast",
                                "%player%", player.getName());
                        p1.sendMessage(message);
                    }
                }
            } else {
                Lang lang = MineKoth.getInstance().getLangManager().getLang(player);
                Titles.sendTitle(player,
                        lang.getMessage("messages.entering-zone-title"),
                        (oldTopGroup.containsPlayer(player)
                                ? lang.getMessage("messages.top-player-capturing-prefix")
                                : lang.getMessage("messages.not-top-player-capturing-prefix")) + getCapturerName()
                                + " " + lang.getMessage("messages.is-capturing"),
                        10, 20, 10);
            }
            Sounds.play(player, 1.0f, 1.0f, "NOTE_PLING", "BLOCK_NOTE_BLOCK_PLING");
            participated.add(player.getUniqueId());
        }
    }

    public void leaveKoth(Player player) {
        CapturingPlayers oldTopGroup = captureState.getTopGroup();
        if (captureState.isCapturing(player)) {
            Player oldTopPlayer = captureState.getTopPlayer();

            // Update capturing players
            captureState.removeFromCapturingPlayers(player);
            updateCapturingGroup(oldTopGroup);

            // Update effects
            Player topPlayer = captureState.getTopPlayer();
            if (oldTopPlayer != topPlayer) {
                if (MineKoth.getInstance().getConfig().getBoolean("capturing-effects.enabled")) {
                    updateCapturingParticles(oldTopPlayer);
                    updateCapturingParticles(topPlayer);
                }
            }
            if (player == oldTopPlayer) {
                Lang lang = MineKoth.getInstance().getLangManager().getLang(player);
                Titles.sendTitle(player,
                        lang.getMessage("messages.leaving-koth-title"),
                        lang.getMessage("messages.no-longer-capturing-subtitle"),
                        10, 20, 10);
            } else {
                Lang lang = MineKoth.getInstance().getLangManager().getLang(player);
                Titles.sendTitle(player,
                        lang.getMessage("messages.leaving-koth-title"),
                        lang.getMessage("messages.left-zone-subtitle"),
                        10, 20, 10);
            }
            Sounds.play(player, 1.0f, 1.0f, "NOTE_BASS", "BLOCK_NOTE_BLOCK_BASS");
            clearParticles(player);
        }
    }

    private void updateCapturingGroup(CapturingPlayers oldTopGroup) {
        if (state == KothEventState.CAPTURED) {
            return;
        }
        CapturingPlayers newTopGroup = captureState.getTopGroup();
        if (oldTopGroup == newTopGroup) {
            return;
        }

        if (newTopGroup == null) {
            state = KothEventState.UNCAPTURED;
        } else if (stalemateEnabled && !captureState.hasMostPlayers(newTopGroup)) {
            state = KothEventState.STALEMATE;
        } else {
            captureState.updateCapturingGroup();
            state = KothEventState.CAPTURING;
        }
    }

    public void setCaptured(CapturingPlayers winners) {
        if (this.state == KothEventState.CAPTURED) {
            return;
        }
        this.state = KothEventState.CAPTURED;
        this.endTime = System.currentTimeMillis();

        Player topPlayer = winners.getPlayers().get(0);
        giveRewards(winners);
        if (topPlayer != null) {
            bets.giveRewards(topPlayer.getName());
        }

        // Notify Discord
        DiscordHook.sendKothCaptured(koth.getName(), topPlayer == null ? "No Winner" : topPlayer.getName());

        clearAllParticles();

        // Show win/lose effects titles and subtitles
        for (Player player : Bukkit.getOnlinePlayers()) {
            displayWinLoseEffects(player, player == topPlayer, topPlayer);
        }

        stats.clearStats();
        captureState.clearPlayers();
    }

    private void clearAllParticles() {
        for (Player player : captureState.getPlayersInZone()) {
            clearParticles(player);
        }
    }

    public int getTimeLeftToFinish() {
        int secondsLeft = (int) Math
                .ceil(((startTime + koth.getTimeLimit() * 1000) - System.currentTimeMillis()) / 1000);
        return secondsLeft;
    }

    public int getTimeLeftToCapture() {
        int secondsLeft = (int) Math.ceil(captureState.getTimeLeftToCapture());
        return secondsLeft;
    }

    public boolean hasReachedTimeLimit() {
        return getTimeLeftToFinish() <= 0;
    }

    public void tick() {
        boolean isCaptureTimeGoal = MineKoth.getInstance().getConfig()
                .getBoolean("capturing-options.capture-time-goal", true);
        if (state == KothEventState.CAPTURING) {
            CapturingPlayers topGroup = captureState.getTopGroup();
            long captureSecondsLeft = getTimeLeftToCapture();
            if (hasReachedTimeLimit()) {
                setCaptured(topGroup);
            } else if ((captureSecondsLeft <= 0 &&
                    isCaptureTimeGoal)) {
                setCaptured(topGroup);
            } else {
                Player topPlayer = captureState.getTopPlayer();
                String capturer = getCapturerName();
                boolean sendTimeLeftTitle = COUNTDOWN_INTERVALS.contains((int) captureSecondsLeft);
                captureState.tick();
                for (Player player : captureState.getPlayersInZone()) {
                    boolean isTopPlayer = player == topPlayer;
                    boolean isTopGroup = topGroup != null && topGroup.containsPlayer(player);
                    if (isTopPlayer) {
                        String timeLeftToCapture = getTimeLeftFormatted();
                        MineKoth.getInstance().getLangManager().sendAction(player, "messages.you-are-capturing-action",
                                "<time-left>", timeLeftToCapture);
                    } else if (isTopGroup) {
                        MineKoth.getInstance().getLangManager().sendAction(player, "messages.capturing-action-team",
                                "<player>", capturer,
                                "<time-left>", getTimeLeftFormatted());
                    } else {
                        MineKoth.getInstance().getLangManager().sendAction(player, "messages.capturing-action-enemy",
                                "<player>", capturer,
                                "<time-left>", getTimeLeftFormatted());
                    }
                    if (!MineKoth.getInstance().getConfig().getBoolean("capturing-options.capture-time-goal", true)) {
                        sendTimeLeftTitle = false;
                    }
                    if (sendTimeLeftTitle) {
                        Lang lang = MineKoth.getInstance().getLangManager().getLang(player);
                        String timeLeftToCapture = getTimeLeftFormatted();
                        Titles.sendTitle(player,
                                lang.getMessage("messages.seconds-left").replace("<seconds>", timeLeftToCapture),
                                isTopPlayer
                                        ? lang.getMessage("messages.you-are-capturing-subtitle")
                                        : isTopGroup
                                                ? lang.getMessage("messages.top-player-name-capturing-subtitle")
                                                        .replace("<topPlayerName>", capturer)
                                                : lang.getMessage("messages.not-top-player-name-capturing-subtitle")
                                                        .replace("<topPlayerName>", capturer),
                                10, 20, 10);
                        Sounds.play(player, 1.0f, 1.0f, "CLICK");
                    }
                }
            }
        } else if (state == KothEventState.UNCAPTURED) {
            if (hasReachedTimeLimit()) {
                if (!isCaptureTimeGoal) {
                    setCaptured(captureState.getTopGroup());
                } else {
                    MineKoth.getInstance().getKothEventManager().end(this);

                    // Notify Discord
                    DiscordHook.sendKothTimeLimit(koth.getName());

                    for (Player player : Bukkit.getOnlinePlayers()) {
                        Lang lang = MineKoth.getInstance().getLangManager().getLang(player);
                        Titles.sendTitle(player,
                                lang.getMessage("messages.time-limit-title"),
                                lang.getMessage("messages.time-limit-subtitle"),
                                10, 60, 10);
                    }
                }
            }
        }
    }

    public boolean isWinner(Player player) {
        CapturingPlayers winners = captureState.getTopGroup();
        return winners == null ? false : winners.containsPlayer(player);
    }

    private void giveRewards(CapturingPlayers winners) {
        if (winners == null) {
            return;
        }
        Rewards rewards = koth.getRewards();
        LootType lootType = rewards.getLootType();
        Player topPlayer = winners.getPlayers().get(0); // Assuming the first player is the winner
        PlayerData winnerData = MineKoth.getInstance().getPlayerDataManager()
                .getIfLoaded(topPlayer.getUniqueId().toString());

        // Iterate through all players in the winners
        for (Player player : winners.getPlayers()) {
            int rewardCount = 0;
            PlayerData playerData = MineKoth.getInstance().getPlayerDataManager()
                    .getIfLoaded(player.getUniqueId().toString());

            switch (lootType) {
                case RANDOM:
                case DEFAULT:
                    if (player.equals(topPlayer)) {
                        rewardCount = rewards.giveRewards(player);
                    }
                    break;
                case MINECLANS_RANDOM:
                case MINECLANS_DEFAULT:
                    rewardCount = rewards.giveRewards(player);
                    break;
                default:
                    break;
            }

            if (rewardCount > 0 && playerData != null) {
                playerData.incrementRewardsReceived(koth.getId(), rewardCount);
            }

            // Increment win count only for the top player
            if (player.equals(topPlayer) && winnerData != null) {
                winnerData.incrementWinCount(koth.getId());
            }
        }
    }

    public Firework createFirework(Location location, FireworkEffect.Type type) {
        Firework firework = location.getWorld().spawn(location, Firework.class);
        FireworkMeta meta = firework.getFireworkMeta();
        meta.addEffect(
                FireworkEffect.builder().with(type).withColor(org.bukkit.Color.LIME).withFlicker().withTrail().build());
        meta.setPower(1);
        firework.setFireworkMeta(meta);
        return firework;
    }

    private void displayWinLoseEffects(Player player, boolean isWinner, Player winner) {
        FileConfiguration config = MineKoth.getInstance().getConfig();
        if (!config.getBoolean("winner-broadcast.enabled", true)) {
            return;
        }
        boolean showToEveryone = config.getBoolean("winner-broadcast.everyone", false);
        if (!showToEveryone) {
            if (!hasParticipated(player)) {
                return; // Player didn't participate, so we stop here.
            }
        }
        Lang lang = MineKoth.getInstance().getLangManager().getLang(player);
        String title = isWinner ? lang.getMessage("messages.you-won-title")
                : lang.getMessage("messages.you-lose-title");
        String subtitle = lang.getMessage("messages.winner-subtitle").replace("<winner>",
                (winner == null ? lang.getMessage("messages.na") : winner.getName()));

        Titles.sendTitle(player, title, subtitle, 10, 70, 20);
        Sounds.play(1.0f, 1.0f, "ENTITY_PLAYER_LEVELUP", "LEVEL_UP");
        if (isWinner) {
            applyWinnerParticles(player);
        } else {
            clearParticles(player);
        }
    }

    private boolean hasParticipated(Player player) {
        return participated.contains(player.getUniqueId());
    }

    public long getTimeSinceEnd() {
        return System.currentTimeMillis() - endTime;
    }

    public Koth getKoth() {
        return koth;
    }

    public void updatePlayerState(Player player, Location to, boolean dead) {
        if (koth.isInside(to) && !dead && !player.isDead()) {
            enterKoth(player);
        } else {
            leaveKoth(player);
        }
    }

    public String getTimeLeftFormatted() {
        if (getTimeLeftToFinish() < captureState.getTimeLeftToCapture() || getTopGroup() == null) {
            return Times.formatSecondsShort(getTimeLeftToFinish());
        } else {
            return captureState.getTimeLeftToCaptureFormatted();
        }
    }

    private void applyCapturingParticles(Player player) {
        if (player != null) {
            MineKoth.getInstance().getParticleScheduler().spiralTrail(player, "COLOURED_DUST", 0.5, 2, 3, 20,
                    5, 0);
            GlowingUtility.setGlowing(player, ChatColor.RED);
        }
    }

    private void applyWinnerParticles(Player player) {
        if (player != null) {
            MineKoth.getInstance().getParticleScheduler().spiralTrail(player, "HAPPY_VILLAGER", 0.5, 2, 3, 20, 20, 3);
            GlowingUtility.setGlowing(player, ChatColor.GREEN);
            FoliaAPI.runTask(() -> {
                GlowingUtility.unsetGlowing(player);
            }, 60L);
        }
    }

    public void clearParticles(Player player) {
        if (player != null) {
            MineKoth.getInstance().getParticleScheduler().removeTrail(player);
            GlowingUtility.unsetGlowing(player);
        }
    }

    public KothEventBets getKothEventBets() {
        return bets;
    }

    public Player getTopPlayer() {
        return captureState.getTopPlayer();
    }

    public Collection<Player> getPlayersInZone() {
        return captureState.getPlayersInZone();
    }

    public CapturingPlayers getTopGroup() {
        return captureState.getTopGroup();
    }

    public void clear() {
        clearAllParticles();
        captureState.clearPlayers();
        stats.clearStats();
        bets.clearAllBets();
    }

    public CapturingPlayers getGroup(Player player) {
        return captureState.getGroup(player);
    }

    public int getPosition(Player player) {
        return captureState.getPosition(player);
    }

    public CapturingPlayers getGroup(int position) {
        return captureState.getGroup(position);
    }

    public String getCapturerName() {
        return captureState.getCapturerName();
    }
}
