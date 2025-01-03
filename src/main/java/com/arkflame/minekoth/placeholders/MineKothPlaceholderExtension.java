package com.arkflame.minekoth.placeholders;

import org.bukkit.entity.Player;

import com.arkflame.minekoth.MineKoth;
import com.arkflame.minekoth.koth.events.KothEvent;
import com.arkflame.minekoth.schedule.Schedule;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;

public class MineKothPlaceholderExtension extends PlaceholderExpansion {

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
        return "1.0.0";
    }

    @Override
    public String onPlaceholderRequest(Player player, String identifier) {
        if (player == null) {
            return "";
        }

        if (identifier.equals("koth_name")) {
            KothEvent kothEvent = MineKoth.getInstance().getKothEventManager().getKothEvent();
            Schedule nextSchedule = MineKoth.getInstance().getScheduleManager().getNextSchedule();

            if (kothEvent != null) {
                return kothEvent.getKoth().getName();
            } else if (nextSchedule != null) {
                return nextSchedule.getKoth().getName();
            } else {
                return "None";
            }
        }

        if (identifier.equals("koth_state")) {
            KothEvent kothEvent = MineKoth.getInstance().getKothEventManager().getKothEvent();
            Schedule nextSchedule = MineKoth.getInstance().getScheduleManager().getNextSchedule();

            if (kothEvent != null) {
                return kothEvent.getState().toString();
            } else if (nextSchedule != null) {
                return "Next";
            } else {
                return "None";
            }
        }

        if (identifier.equals("koth_time")) {
            KothEvent kothEvent = MineKoth.getInstance().getKothEventManager().getKothEvent();
            Schedule nextSchedule = MineKoth.getInstance().getScheduleManager().getNextSchedule();

            if (kothEvent != null) {
                if (kothEvent.getState() == KothEvent.KothEventState.CAPTURING) {
                    return String.valueOf(kothEvent.getTimeLeftToCaptureFormatted());
                } else if (kothEvent.getState() == KothEvent.KothEventState.STALEMATE) {
                    return "Stalemate";
                } else {
                    return kothEvent.getTimeLeftToFinishFormatted();
                }
            } else if (nextSchedule != null) {
                // Return the time left to start the next schedule
                return nextSchedule.getTimeLeftFormatted();
            } else {
                return "0";
            }
        }

        return null;
    }

}
