package com.arkflame.minekoth.placeholders;

import org.bukkit.entity.Player;
import org.bukkit.Location;

import com.arkflame.minekoth.MineKoth;
import com.arkflame.minekoth.koth.events.CapturingPlayers;
import com.arkflame.minekoth.koth.events.KothEvent;
import com.arkflame.minekoth.koth.events.KothEvent.KothEventState;
import com.arkflame.minekoth.schedule.Schedule;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;

import java.util.stream.Collectors;

public class MineKothPlaceholderExtension extends PlaceholderExpansion {
    private static final String NONE = "";
    private static final String FALSE = "False";
    
    private final MineKoth plugin;

    public MineKothPlaceholderExtension() {
        this.plugin = MineKoth.getInstance();
    }

    @Override
    public String getIdentifier() {
        return "minekoth";
    }

    @Override
    public String getAuthor() {
        return "ArkFlame";
    }

    @Override
    public String getVersion() {
        return "1.1.1";
    }

    @Override
    public String onPlaceholderRequest(Player player, String identifier) {
        switch (identifier) {
            case "koth_name": return getKothName();
            case "koth_state": return getKothState();
            case "koth_time": return getKothTime();
            case "koth_location": return getKothLocation();
            case "koth_top_player": return getKothTopPlayer();
            case "koth_capturing_players": return getKothCapturingPlayers();
            case "koth_winner": return getKothWinner();
            case "koth_time_since_end": return getKothTimeSinceEnd();
            case "koth_is_stalemate": return getKothIsStalemate();
            default: return null;
        }
    }

    private KothEvent getCurrentKothEvent() {
        return plugin.getKothEventManager().getKothEvent();
    }

    private Schedule getNextSchedule() {
        return plugin.getScheduleManager().getNextSchedule();
    }

    private String getKothName() {
        KothEvent event = getCurrentKothEvent();
        if (event != null) {
            return event.getKoth().getName();
        }
        
        Schedule schedule = getNextSchedule();
        return schedule != null ? schedule.getKoth().getName() : NONE;
    }

    private String getKothState() {
        KothEvent event = getCurrentKothEvent();
        if (event != null) {
            switch (event.getState()) {
                case CAPTURING: return "Capturing";
                case STALEMATE: return "Stalemate";
                case CAPTURED: return "Finished";
                default: return "Running";
            }
        }
        
        return getNextSchedule() != null ? "Starting" : NONE;
    }

    private String getKothTime() {
        KothEvent event = getCurrentKothEvent();
        if (event != null) {
            switch (event.getState()) {
                case CAPTURING: return event.getTimeLeftToCaptureFormatted();
                case STALEMATE: return "Stalemate";
                default: return event.getTimeLeftToFinishFormatted();
            }
        }
        
        Schedule schedule = getNextSchedule();
        return schedule != null ? schedule.getTimeLeftFormatted() : "";
    }

    private String getKothLocation() {
        Location loc = null;
        KothEvent event = getCurrentKothEvent();
        if (event != null) {
            loc = event.getKoth().getCenter();
        } else {
            Schedule schedule = getNextSchedule();
            if (schedule != null) {
                loc = schedule.getKoth().getCenter();
            }
        }
        
        return formatLocation(loc);
    }

    private String formatLocation(Location loc) {
        return loc == null ? NONE : 
            String.format("%d, %d, %d", loc.getBlockX(), loc.getBlockY(), loc.getBlockZ());
    }

    private String getKothTopPlayer() {
        KothEvent event = getCurrentKothEvent();
        if (event != null) {
            Player topPlayer = event.getTopPlayer();
            return topPlayer != null ? topPlayer.getName() : NONE;
        }
        return NONE;
    }

    private String getKothCapturingPlayers() {
        KothEvent event = getCurrentKothEvent();
        if (event != null && !event.getPlayersInZone().isEmpty()) {
            return event.getPlayersInZone().stream()
                .map(Player::getName)
                .collect(Collectors.joining(", "));
        }
        return NONE;
    }

    private String getKothWinner() {
        KothEvent event = getCurrentKothEvent();
        if (event != null) {
            CapturingPlayers winners = event.getTopGroup();
            if (winners != null && !winners.getPlayers().isEmpty()) {
                return winners.getPlayers().stream()
                    .map(Player::getName)
                    .collect(Collectors.joining(", "));
            }
        }
        return NONE;
    }

    private String getKothTimeSinceEnd() {
        KothEvent event = getCurrentKothEvent();
        if (event != null && event.getState() == KothEventState.CAPTURED) {
            return formatTime(event.getTimeSinceEnd());
        }
        return "0";
    }

    private String getKothIsStalemate() {
        KothEvent event = getCurrentKothEvent();
        return event != null ? 
            Boolean.toString(event.isStalemateEnabled()) : 
            FALSE;
    }

    private String formatTime(long timeInMillis) {
        long minutes = timeInMillis / 60000;
        long seconds = (timeInMillis % 60000) / 1000;
        return String.format("%02d:%02d", minutes, seconds);
    }
}