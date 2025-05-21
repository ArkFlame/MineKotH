package com.arkflame.minekoth.koth.events;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;

import org.bukkit.entity.Player;

import com.arkflame.minekoth.MineKoth;
import com.arkflame.minekoth.utils.MineClansHook;
import com.arkflame.minekoth.utils.Times;

public class KothEventCaptureState {
    private final int timeToCapture;

    private Collection<Player> playersInZone = new HashSet<>();
    private List<CapturingPlayers> playersCapturing = new ArrayList<>();

    public KothEventCaptureState(int timeToCapture) {
        this.timeToCapture = timeToCapture;
    }

    public int getTimeLeftToCapture() {
        if (!MineKoth.getInstance().getConfig().getBoolean("capturing-options.capture-time-goal", true)) {
            return getTopGroup().getScore();
        }
        int timeLeftToCapture = ((timeToCapture - (getTopGroup() != null ? getTopGroup().getScore() : 0)));
        return timeLeftToCapture;
    }

    public String getTimeLeftToCaptureFormatted() {
        return Times.formatSecondsShort(getTimeLeftToCapture());
    }

    public boolean isInZone(Player player) {
        return playersInZone.contains(player);
    }

    private void sortCapturingPlayers() {
        playersCapturing.sort((group1, group2) -> Integer.compare(group2.getScore(), group1.getScore()));
    }

    public void addToCapturingPlayers(Player player) {
        playersInZone.add(player);
        for (CapturingPlayers group : playersCapturing) {
            Player firstPlayer = group.getPlayers().get(0);
            if (firstPlayer != player && isSameTeam(group.getPlayers().get(0), player)) {
                group.addPlayer(player);
                return;
            }
        }
        playersCapturing.add(new CapturingPlayers(player));
        sortCapturingPlayers();
    }

    public void removeFromCapturingPlayers(Player player) {
        playersInZone.remove(player);
        if (MineKoth.getInstance().getConfig().getBoolean("capturing-options.reset-score", true)) {
            playersCapturing.removeIf(group -> {
                group.removePlayer(player);
                return group.getPlayers().isEmpty();
            });
        }
        sortCapturingPlayers();
    }

    public boolean isCapturing(Player player) {
        return playersInZone.contains(player);
    }

    private boolean isSameTeam(Player p1, Player p2) {
        // Placeholder logic for determining if two players are on the same team.
        if (MineKoth.getInstance().isMineClansEnabled()) { // MineClans plugin enabled
            return MineClansHook.isSameTeam(p1, p2);
        }
        return false;
    }

    public boolean isAnyoneCapturing() {
        return !playersCapturing.isEmpty();
    }

    public boolean hasMostPlayers(CapturingPlayers topGroup) {
        int topGroupSize = topGroup.getPlayers().size();
        for (CapturingPlayers group : playersCapturing) {
            if (group.getPlayers().size() >= topGroupSize) {
                return false;
            }
        }
        return true;
    }

    public Collection<Player> getPlayersInZone() {
        return playersInZone;
    }

    public void clearPlayers() {
        playersCapturing.clear();
        playersInZone.clear();
    }

    public CapturingPlayers getGroup(int index) {
        return playersCapturing != null && !playersCapturing.isEmpty() &&
                playersCapturing.size() > index ? playersCapturing.get(index) : null;
    }

    public CapturingPlayers getTopGroup() {
        return getGroup(0);
    }

    public Player getTopPlayer() {
        CapturingPlayers topGroup = getTopGroup();
        return topGroup != null ? topGroup.getPlayers().get(0) : null;
    }

    public boolean isTopPlayer(Player player) {
        return player == getTopPlayer();
    }

    public void tick() {
        if (!isAnyoneCapturing()) {
            return;
        }
        for (CapturingPlayers group : playersCapturing) {
            for (Player player : group.getPlayers()) {
                if (isCapturing(player)) {
                    group.addScore(1);
                    break;
                }
            }
        }
        sortCapturingPlayers();
    }

    public void updateCapturingGroup() {
        // Reset all scores
        if (MineKoth.getInstance().getConfig().getBoolean("capturing-options.reset-score", true)) {
            for (CapturingPlayers group : playersCapturing) {
                group.setScore(0);
            }
        }
    }

    public CapturingPlayers getGroup(Player player) {
        for (CapturingPlayers group : playersCapturing) {
            if (group.getPlayers().contains(player)) {
                return group;
            }
        }
        return null;
    }

    public int getPosition(Player player) {
        CapturingPlayers group = getGroup(player);
        return group != null ? playersCapturing.indexOf(group) + 1 : -1;
    }
}
