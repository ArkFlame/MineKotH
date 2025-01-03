package com.arkflame.minekoth.koth.events;

import com.arkflame.minekoth.koth.Koth;
import com.arkflame.minekoth.utils.Sounds;
import com.arkflame.minekoth.utils.Titles;

import org.bukkit.entity.Player;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Bukkit;
import org.bukkit.FireworkEffect;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.FireworkMeta;
import org.bukkit.entity.Firework;

import java.util.*;

public class KothEvent {

    public enum KothEventState {
        UNCAPTURED,
        CAPTURING,
        STALEMATE,
        CAPTURED
    }

    private final Koth koth;
    private KothEventState state;
    private Map<Player, Long> playersInZone;
    private List<CapturingPlayers> playersCapturing;
    private int captureTime;
    private boolean stalemateEnabled;

    public KothEvent(Koth koth) {
        this.koth = koth;
        this.state = KothEventState.UNCAPTURED;
        this.playersInZone = new HashMap<>();
        this.playersCapturing = new ArrayList<>();
        this.captureTime = koth.getTimeToCapture();
        this.stalemateEnabled = false;
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

    public void updatePlayerState(Player player, boolean entering) {
        if (entering) {
            if (!playersInZone.containsKey(player)) {
                playersInZone.put(player, System.currentTimeMillis());
                addToCapturingPlayers(player);
            }
            evaluateState();
        } else {
            playersInZone.remove(player);
            removeFromCapturingPlayers(player);
            evaluateState();
        }
    }

    private boolean isSameTeam(Player p1, Player p2) {
        // Placeholder logic for determining if two players are on the same team.
        return false;
    }

    private void addToCapturingPlayers(Player player) {
        for (CapturingPlayers group : playersCapturing) {
            if (isSameTeam(group.getPlayers().get(0), player)) {
                group.addPlayer(player);
                return;
            }
        }
        playersCapturing.add(new CapturingPlayers(player));
    }

    private void removeFromCapturingPlayers(Player player) {
        playersCapturing.removeIf(group -> {
            group.removePlayer(player);
            return group.getPlayers().isEmpty();
        });
    }

    private void evaluateState() {
        if (state == KothEventState.CAPTURED) {
            return;
        }

        if (playersInZone.isEmpty()) {
            state = KothEventState.UNCAPTURED;
            playersCapturing.clear();
            return;
        }

        playersCapturing.sort(Comparator.comparingInt(group -> group.getPlayers().size()));

        if (playersCapturing.size() > 1
                && playersCapturing.get(0).getPlayers().size() == playersCapturing.get(1).getPlayers().size()) {
            if (stalemateEnabled) {
                state = KothEventState.STALEMATE;
            } else {
                state = KothEventState.CAPTURING;
            }
        } else {
            state = KothEventState.CAPTURING;
        }
    }

    public void tick() {
        if (state == KothEventState.CAPTURING) {
            long currentTime = System.currentTimeMillis();
            CapturingPlayers topGroup = playersCapturing.get(0);
            long totalCaptureTime = topGroup.getPlayers().stream()
                    .mapToLong(playersInZone::get)
                    .map(startTime -> currentTime - startTime)
                    .sum();

            if (totalCaptureTime / topGroup.getPlayers().size() >= captureTime * 1000L) {
                setCaptured(topGroup);
            }
        }
    }

    private void setCaptured(CapturingPlayers winners) {
        state = KothEventState.CAPTURED;
        giveRewards(winners);
        playFireworks();
    }

    private void giveRewards(CapturingPlayers winners) {
        Player topPlayer = winners.getPlayers().get(0);

        // Execute reward commands
        for (String command : koth.getRewards().getRewardsCommands()) {
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command.replace("%player%", topPlayer.getName()));
        }

        // Give reward items
        for (ItemStack item : koth.getRewards().getRewardsItems()) {
            if (item != null && item.getType() != Material.AIR) {
                topPlayer.getInventory().addItem(item);
            }
        }
        ;

        // Notify players
        for (Player player : Bukkit.getOnlinePlayers()) {
            sendTitle(player, winners.getPlayers().contains(player), topPlayer.getName());
        }
    }

    private void playFireworks() {
        Location location = koth.getFirstPosition();
        for (int i = 0; i < 3; i++) {
            Firework firework = location.getWorld().spawn(location, Firework.class);
            FireworkMeta meta = firework.getFireworkMeta();
            meta.addEffect(
                    FireworkEffect.builder().with(FireworkEffect.Type.BALL).withColor(org.bukkit.Color.RED).build());
            firework.setFireworkMeta(meta);
        }
    }

    private void sendTitle(Player player, boolean isWinner, String winnerName) {
        String title = isWinner ? "WINNER" : "LOSER";
        String subtitle = "Winner: " + winnerName;
        Titles.sendTitle(player, title, subtitle, 10, 70, 20);
        Sounds.play(1.0f, 1.0f, "ENTITY_PLAYER_LEVELUP", "LEVEL_UP");
    }

    public void end() {
        state = KothEventState.CAPTURED;
        playersInZone.clear();
        playersCapturing.clear();
    }

    private static class CapturingPlayers {
        private final List<Player> players;

        public CapturingPlayers(Player player) {
            this.players = new ArrayList<>();
            this.players.add(player);
        }

        public List<Player> getPlayers() {
            return players;
        }

        public void addPlayer(Player player) {
            players.add(player);
        }

        public void removePlayer(Player player) {
            players.remove(player);
        }
    }

    public Koth getKoth() {
        return koth;
    }

    public void updatePlayerState(Player player, Location to) {
        if (koth.isInside(to)) {
            updatePlayerState(player, true);
        } else {
            updatePlayerState(player, false);
        }
    }

    public boolean isCapturing(Player player) {
        return playersInZone.containsKey(player);
    }

    public long getTimeOfCapture(Player player) {
        return playersInZone.get(player);
    }

    public long getTimeCaptured(Player player) {
        return System.currentTimeMillis() - getTimeOfCapture(player);
    }

    public String getTimeCapturedFormatted(Player player) {
        long time = getTimeCaptured(player);
        long minutes = time / 60000;
        long seconds = (time % 60000) / 1000;
        return String.format("%02d:%02d", minutes, seconds);
    }
}
