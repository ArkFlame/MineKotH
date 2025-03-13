package com.arkflame.minekoth.placeholders;

import org.bukkit.entity.Player;
import org.bukkit.Location;
import com.arkflame.minekoth.MineKoth;
import com.arkflame.minekoth.koth.Koth;
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
        if (identifier.startsWith("koth_")) {
            String[] parts = identifier.split("_");
            String kothId = parts.length > 2 ? parts[2] : null;
            switch (parts[1]) {
                case "name":
                    return kothId == null ? getKothName() : getKothNameById(kothId);
                case "state":
                    return kothId == null ? getKothState() : getKothStateById(kothId);
                case "capturer":
                    return kothId == null ? getKothCapturer() : getKothCapturerById(kothId);
                case "time":
                    return kothId == null ? getKothTime() : getKothTimeById(kothId);
                case "location":
                    return kothId == null ? getKothLocation() : getKothLocationById(kothId);
                case "top_player":
                    return getKothTopPlayer();
                case "capturing_players":
                    return getKothCapturingPlayers();
                case "winner":
                    return getKothWinner();
                case "time_since_end":
                    return getKothTimeSinceEnd();
                case "is_stalemate":
                    return getKothIsStalemate();
                default:
                    break;
            }
        }
        return NONE;
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

    private String getKothNameById(String kothId) {
        Koth koth = plugin.getKothManager().getKothById(Integer.parseInt(kothId));
        return koth != null ? koth.getName() : NONE;
    }

    private String getKothState() {
        KothEvent event = getCurrentKothEvent();
        if (event != null) {
            return event.getState().getFancyName();
        }
        return getNextSchedule() != null ? "Starting" : NONE;
    }

    private String getKothStateById(String kothId) {
        Koth koth = plugin.getKothManager().getKothById(Integer.parseInt(kothId));
        for (KothEvent event : plugin.getKothEventManager().getRunningKoths()) {
            if (event.getKoth().getId() == koth.getId()) {
                return event.getState().getFancyName();
            }
        }
        return getNextSchedule() != null ? "Starting" : NONE;
    }

    private String getKothTime() {
        KothEvent event = getCurrentKothEvent();
        if (event != null) {
            if (event != null) {
                switch (event.getState()) {
                    case CAPTURING:
                        return event.getTimeLeftToCaptureFormatted();
                    case STALEMATE:
                        return "Stalemate";
                    default:
                        return event.getTimeLeftToFinishFormatted();
                }
            }
        }
        Schedule schedule = getNextSchedule();
        return schedule != null ? schedule.getTimeLeftFormatted() : NONE;
    }

    private String getKothTimeById(String kothId) {
        Koth koth = plugin.getKothManager().getKothById(Integer.parseInt(kothId));
        for (KothEvent event : plugin.getKothEventManager().getRunningKoths()) {
            if (event.getKoth().getId() == koth.getId()) {
                if (event != null) {
                    switch (event.getState()) {
                        case CAPTURING:
                            return event.getTimeLeftToCaptureFormatted();
                        case STALEMATE:
                            return "Stalemate";
                        default:
                            return event.getTimeLeftToFinishFormatted();
                    }
                }
            }
        }

        for (Schedule schedule : plugin.getScheduleManager().getSchedulesByKoth(koth.getId())) {
            return schedule.getTimeLeftFormatted();
        }
        return NONE;
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

    private String getKothLocationById(String kothId) {
        Koth koth = plugin.getKothManager().getKothById(Integer.parseInt(kothId));
        if (koth == null) {
            return NONE;
        }
        return formatLocation(koth.getCenter());
    }

    private String formatLocation(Location loc) {
        return loc == null ? NONE : String.format("%d, %d, %d", loc.getBlockX(), loc.getBlockY(), loc.getBlockZ());
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
        return event != null ? Boolean.toString(event.isStalemateEnabled()) : FALSE;
    }

    private String formatTime(long timeInMillis) {
        long minutes = timeInMillis / 60000;
        long seconds = (timeInMillis % 60000) / 1000;
        return String.format("%02d:%02d", minutes, seconds);
    }

    public String getKothCapturer() {
        KothEvent event = getCurrentKothEvent();
        if (event != null) {
            Player player = event.getTopPlayer();
            return player != null ? player.getName() : NONE;
        }
        return NONE;
    }

    public String getKothCapturerById(String kothId) {
        Koth koth = plugin.getKothManager().getKothById(Integer.parseInt(kothId));
        for (KothEvent event : plugin.getKothEventManager().getRunningKoths()) {
            if (event != null && event.getKoth().getId() == koth.getId()) {
                Player player = event.getTopPlayer();
                return player != null ? player.getName() : NONE;
            }
        }
        return NONE;
    }
}
