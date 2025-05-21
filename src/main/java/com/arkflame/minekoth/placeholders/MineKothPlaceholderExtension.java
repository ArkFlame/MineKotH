package com.arkflame.minekoth.placeholders;

import org.bukkit.entity.Player;
import org.bukkit.Location;
import com.arkflame.minekoth.MineKoth;
import com.arkflame.minekoth.koth.Koth;
import com.arkflame.minekoth.koth.events.CapturingPlayers;
import com.arkflame.minekoth.koth.events.KothEvent;
import com.arkflame.minekoth.koth.events.KothEvent.KothEventState;
import com.arkflame.minekoth.playerdata.PlayerData;
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
        return MineKoth.getInstance().getDescription().getVersion();
    }

    @Override
    public String onPlaceholderRequest(Player player, String identifier) {
        String[] args = identifier.split("_");
        if (args.length > 0) {
            String arg0 = args[0];
            String arg1 = args.length > 1 ? args[1] : null;
            String arg2 = args.length > 2 ? args[2] : null;
            switch (arg0) {
                case "koth": {
                    KothEvent event = getCurrentKothEvent();
                    switch (args[1]) {
                        case "name":
                            return arg2 == null ? getKothName() : getKothNameById(arg2);
                        case "state":
                            return arg2 == null ? getKothState() : getKothStateById(arg2);
                        case "capturer":
                            return arg2 == null ? getKothCapturer() : getKothCapturerById(arg2);
                        case "time":
                            return arg2 == null ? getKothTime() : getKothTimeById(arg2);
                        case "location":
                            return arg2 == null ? getKothLocation() : getKothLocationById(arg2);
                        case "topplayer":
                            return getKothTopPlayer();
                        case "capturingplayers":
                            return getKothCapturingPlayers();
                        case "winner":
                            return getKothWinner();
                        case "timesinceend":
                            return getKothTimeSinceEnd();
                        case "isstalemate":
                            return getKothIsStalemate();
                        case "capturetime":
                            if (event != null) {
                                if (arg2 != null) {
                                    CapturingPlayers players = event.getGroup(Integer.parseInt(arg2));
                                    if (players != null) {
                                        return players.getCaptureTimeFormatted();
                                    }
                                }
                                int timeLimitLeft = event.getTimeLeftToFinish();
                                int timeToCaptureLeft = event.getTimeLeftToCapture();
                                CapturingPlayers players = event.getGroup(player);
                                if (players != null) {
                                    if (timeToCaptureLeft > timeLimitLeft) {
                                        return players.getCaptureTimeFormatted();
                                    } else {
                                        return event.getTimeLeftFormatted();
                                    }
                                }
                            }
                            break;
                        case "position":
                            if (event != null) {
                                int position = event.getPosition(player);
                                return position == -1 ? NONE : String.valueOf(position);
                            }
                            break;
                        case "playername":
                            if (event != null && arg2 != null) {
                                CapturingPlayers players = event.getGroup(Integer.parseInt(arg2) - 1);
                                if (players != null) {
                                    return players.getPlayers().stream()
                                            .map(Player::getName)
                                            .collect(Collectors.joining(", "));
                                }
                            }
                            break;
                        default:
                            break;
                    }
                    break;
                }
                case "stats": {
                    if (arg1 != null) {
                        PlayerData playerData = plugin.getPlayerDataManager()
                                .getAndLoad(player.getUniqueId().toString());
                        if (playerData != null) {
                            switch (arg1) {
                                case "wins":
                                    return String.valueOf(playerData.getTotal(PlayerData.WINS).intValue());
                            }
                        }
                    }
                }
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
        return MineKoth.getInstance().getConfig().getString("messages.koth-states.NOT_STARTED");
    }

    private String getKothStateById(String kothId) {
        Koth koth = plugin.getKothManager().getKothById(Integer.parseInt(kothId));
        for (KothEvent event : plugin.getKothEventManager().getRunningKoths()) {
            if (event.getKoth().getId() == koth.getId()) {
                return event.getState().getFancyName();
            }
        }
        return MineKoth.getInstance().getConfig().getString("messages.koth-states.NOT_STARTED");
    }

    private String getKothTime() {
        KothEvent event = getCurrentKothEvent();
        if (event != null) {
            if (event != null) {
                switch (event.getState()) {
                    case CAPTURING:
                        return event.getTimeLeftFormatted();
                    case STALEMATE:
                        return "Stalemate";
                    default:
                        return event.getTimeLeftFormatted();
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
                            return event.getTimeLeftFormatted();
                        case STALEMATE:
                            return "Stalemate";
                        default:
                            return event.getTimeLeftFormatted();
                    }
                }
            }
        }

        Schedule schedule = plugin.getScheduleManager().getNextOccurrence(koth);
        if (schedule != null) {
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
        return loc == null ? NONE : String.format("%d, %d", loc.getBlockX(), loc.getBlockZ());
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
            return event.getCapturerName();
        }
        return NONE;
    }

    public String getKothCapturerById(String kothId) {
        Koth koth = plugin.getKothManager().getKothById(Integer.parseInt(kothId));
        for (KothEvent event : plugin.getKothEventManager().getRunningKoths()) {
            if (event != null && event.getKoth().getId() == koth.getId()) {
                return event.getCapturerName();
            }
        }
        return NONE;
    }
}
