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

import java.util.Collection;
import java.util.stream.Collectors;

public class MineKothPlaceholderExtension extends PlaceholderExpansion {
    private static final String ZERO = "0";
    private static final String NONE = "N/A";
    private static final String EMPTY = "";
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
    public boolean persist() {
        return true;
    }

    @Override
    public String onPlaceholderRequest(Player player, String identifier) {
        if (identifier == null) return null;
        
        // Split arguments: koth_name -> [koth, name]
        String[] args = identifier.split("_");
        if (args.length < 1) return null;

        String mainCategory = args[0]; // usually "koth"
        String subCategory = args.length > 1 ? args[1] : ""; 
        String detail = args.length > 2 ? args[2] : null; // e.g., position number or ID

        try {
            if (mainCategory.equals("koth")) {
                KothEvent currentEvent = getCurrentKothEvent();

                switch (subCategory) {
                    case "isrunning":
                        return String.valueOf(currentEvent != null);
                    // --- New: Dynamic Placeholders (Adapt to Event or Schedule) ---
                    case "dynamic":
                        if (detail == null) return NONE;
                        switch (detail) {
                            case "name": return getDynamicName();
                            case "time": return getDynamicTime();
                            case "location": return getDynamicLocation();
                            case "state": return getDynamicState();
                        }
                        return NONE;

                    // --- New: Current Placeholders (Only active, else Empty) ---
                    case "current":
                        if (detail == null) return NONE;
                        if (currentEvent == null) return EMPTY;
                        switch (detail) {
                            case "name": return currentEvent.getKoth().getName();
                            case "time": return currentEvent.getTimeLeftFormatted();
                            case "location": return formatLocation(currentEvent.getKoth().getCenter());
                            case "state": return currentEvent.getState().getFancyName();
                        }
                        return EMPTY;

                    // --- Gameplay & Specific Context ---
                    case "capturer":
                        return detail == null ? getKothCapturer(currentEvent) : getKothCapturerById(detail);
                    case "topplayer":
                        return getKothTopPlayer(currentEvent);
                    case "capturingplayers":
                        return getKothCapturingPlayers(currentEvent);
                    case "winner":
                        return getKothWinner(currentEvent);
                    case "timesinceend":
                        return getKothTimeSinceEnd(currentEvent);
                    case "isstalemate":
                        return getKothIsStalemate(currentEvent);
                    
                    // --- Logic for Capture Time (Complex) ---
                    case "capturetime":
                        // Logic for %koth_capturetime_<position>%
                        if (detail != null && currentEvent != null) {
                            return getCaptureTimeAtPosition(currentEvent, detail);
                        }
                        // Logic for %koth_capturetime% (Player specific)
                        return getPlayerCaptureTime(currentEvent, player);

                    // --- Logic for Positions/Ranks ---
                    case "top":
                         // Handle %koth_top_position%
                        if ("position".equals(detail) && currentEvent != null) {
                             int pos = currentEvent.getPosition(player);
                             return pos == -1 ? NONE : String.valueOf(pos);
                        }
                        break;
                    case "playername":
                        // Handle %koth_playername_<position>%
                        if (detail != null && currentEvent != null) {
                            return getPlayerNameAtPosition(currentEvent, detail);
                        }
                        break;

                    // --- Retro Compatibility (Old Placeholders) ---
                    case "name":
                        // Old %koth_name% -> Dynamic
                        return detail == null ? getDynamicName() : getKothNameById(detail);
                    case "time":
                        // Old %koth_time% -> Dynamic
                        return detail == null ? getDynamicTime() : getKothTimeById(detail);
                    case "location":
                        // Old %koth_location% -> Dynamic
                        return detail == null ? getDynamicLocation() : getKothLocationById(detail);
                    case "state":
                        // Old %koth_state% -> Dynamic
                        return detail == null ? getDynamicState() : getKothStateById(detail);
                    case "position":
                        // Old %koth_position% -> New %koth_top_position%
                        if (currentEvent != null) {
                            int pos = currentEvent.getPosition(player);
                            return pos == -1 ? NONE : String.valueOf(pos);
                        }
                        return NONE;
                }
            } else if (mainCategory.equals("stats")) {
                // Handle Stats
                if (subCategory.equals("wins")) {
                    PlayerData playerData = plugin.getPlayerDataManager().getAndLoad(player.getUniqueId().toString());
                    return playerData != null ? String.valueOf(playerData.getTotal(PlayerData.WINS).intValue()) : ZERO;
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        return null;
    }

    // --- Core Data Retrievers ---

    private KothEvent getCurrentKothEvent() {
        return plugin.getKothEventManager().getKothEvent();
    }

    private Schedule getNextSchedule() {
        return plugin.getScheduleManager().getNextSchedule();
    }

    // --- Dynamic Logic Implementation ---

    private String getDynamicName() {
        KothEvent event = getCurrentKothEvent();
        if (event != null) return event.getKoth().getName();
        Schedule schedule = getNextSchedule();
        return schedule != null ? schedule.getKoth().getName() : NONE;
    }

    private String getDynamicTime() {
        KothEvent event = getCurrentKothEvent();
        if (event != null) {
            return event.getState() == KothEventState.STALEMATE ? "Stalemate" : event.getTimeLeftFormatted();
        }
        Schedule schedule = getNextSchedule();
        return schedule != null ? schedule.getTimeLeftFormatted() : NONE;
    }

    private String getDynamicLocation() {
        KothEvent event = getCurrentKothEvent();
        if (event != null) return formatLocation(event.getKoth().getCenter());
        Schedule schedule = getNextSchedule();
        return schedule != null ? formatLocation(schedule.getKoth().getCenter()) : NONE;
    }

    private String getDynamicState() {
        KothEvent event = getCurrentKothEvent();
        if (event != null) return event.getState().getFancyName();
        return MineKoth.getInstance().getConfig().getString("messages.koth-states.NOT_STARTED", "Not Started");
    }

    // --- Gameplay Helper Methods ---

    private String getKothCapturer(KothEvent event) {
        return event != null ? event.getCapturerName() : NONE;
    }

    private String getKothTopPlayer(KothEvent event) {
        if (event != null) {
            Player topPlayer = event.getTopPlayer();
            return topPlayer != null ? topPlayer.getName() : NONE;
        }
        return NONE;
    }

    private String getKothCapturingPlayers(KothEvent event) {
        if (event != null && !event.getPlayersInZone().isEmpty()) {
            return event.getPlayersInZone().stream()
                    .map(Player::getName)
                    .collect(Collectors.joining(", "));
        }
        return NONE;
    }

    private String getKothWinner(KothEvent event) {
        if (event != null) {
            Player winner = event.getTopPlayer();
            return winner != null ? winner.getName() : NONE;
        }
        return NONE;
    }

    private String getKothTimeSinceEnd(KothEvent event) {
        if (event != null && event.getState() == KothEventState.CAPTURED) {
            return formatTime(event.getTimeSinceEnd());
        }
        return ZERO;
    }

    private String getKothIsStalemate(KothEvent event) {
        return event != null ? Boolean.toString(event.isStalemateEnabled()) : FALSE;
    }

    private String getCaptureTimeAtPosition(KothEvent event, String positionStr) {
        try {
            CapturingPlayers players = event.getGroup(Integer.parseInt(positionStr));
            if (players != null) {
                return players.getCaptureTimeFormatted();
            }
        } catch (NumberFormatException ignored) {}
        return NONE;
    }

    private String getPlayerCaptureTime(KothEvent event, Player player) {
        if (event != null) {
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
        return NONE;
    }

    private String getPlayerNameAtPosition(KothEvent event, String positionStr) {
        try {
            CapturingPlayers players = event.getGroup(Integer.parseInt(positionStr) - 1);
            if (players != null) {
                return players.getPlayers().stream()
                        .map(Player::getName)
                        .collect(Collectors.joining(", "));
            }
        } catch (NumberFormatException ignored) {}
        return NONE;
    }

    // --- ID Based Lookups (Using Simplified API) ---

    private String getKothNameById(String kothId) {
        try {
            Koth koth = plugin.getKothManager().getKothById(Integer.parseInt(kothId));
            return koth != null ? koth.getName() : NONE;
        } catch (NumberFormatException e) { return NONE; }
    }

    private String getKothLocationById(String kothId) {
        try {
            Koth koth = plugin.getKothManager().getKothById(Integer.parseInt(kothId));
            return koth != null ? formatLocation(koth.getCenter()) : NONE;
        } catch (NumberFormatException e) { return NONE; }
    }

    private String getKothStateById(String kothId) {
        try {
            Koth koth = plugin.getKothManager().getKothById(Integer.parseInt(kothId));
            if (koth == null) return NONE;

            // Simplified API usage
            Collection<KothEvent> events = plugin.getKothEventManager().getRunningKothsById(koth.getId());
            
            if (!events.isEmpty()) {
                // Assuming one event per Koth ID generally, or picking first
                return events.iterator().next().getState().getFancyName();
            }
            return MineKoth.getInstance().getConfig().getString("messages.koth-states.NOT_STARTED", "Not Started");
        } catch (NumberFormatException e) { return NONE; }
    }

    private String getKothTimeById(String kothId) {
        try {
            Koth koth = plugin.getKothManager().getKothById(Integer.parseInt(kothId));
            if (koth == null) return NONE;

            // Simplified API usage
            Collection<KothEvent> events = plugin.getKothEventManager().getRunningKothsById(koth.getId());

            if (!events.isEmpty()) {
                KothEvent event = events.iterator().next();
                return event.getState() == KothEventState.STALEMATE ? "Stalemate" : event.getTimeLeftFormatted();
            }

            Schedule schedule = plugin.getScheduleManager().getNextOccurrence(koth);
            return schedule != null ? schedule.getTimeLeftFormatted() : NONE;
        } catch (NumberFormatException e) { return NONE; }
    }

    private String getKothCapturerById(String kothId) {
        try {
            Koth koth = plugin.getKothManager().getKothById(Integer.parseInt(kothId));
            if (koth == null) return NONE;

            Collection<KothEvent> events = plugin.getKothEventManager().getRunningKothsById(koth.getId());
            if (!events.isEmpty()) {
                return events.iterator().next().getCapturerName();
            }
        } catch (NumberFormatException e) {}
        return NONE;
    }

    // --- Utilities ---

    private String formatLocation(Location loc) {
        return loc == null ? NONE : String.format("%d, %d", loc.getBlockX(), loc.getBlockZ());
    }

    private String formatTime(long timeInMillis) {
        long minutes = timeInMillis / 60000;
        long seconds = (timeInMillis % 60000) / 1000;
        return String.format("%02d:%02d", minutes, seconds);
    }
}