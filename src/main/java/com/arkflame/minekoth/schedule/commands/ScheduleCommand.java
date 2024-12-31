package com.arkflame.minekoth.schedule.commands;

import com.arkflame.minekoth.MineKoTH;
import com.arkflame.minekoth.schedule.Schedule;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.entity.Player;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

public class ScheduleCommand {
    public static void run(Player sender, String[] args) {
        if (args.length < 2) {
            sendHelp(sender);
            return;
        }

        String subCommand = args[1].toLowerCase();

        switch (subCommand) {
            case "list":
                listSchedules(sender);
                break;
            default:
                sendHelp(sender);
                break;
        }
    }

    private static void listSchedules(Player sender) {
        List<Schedule> schedules = MineKoTH.getInstance().getScheduleManager().getAllSchedules();

        if (schedules.isEmpty()) {
            sender.sendMessage(ChatColor.RED + "There are no schedules available.");
            return;
        }

        // Sort schedules by next occurrence
        schedules.sort((s1, s2) -> {
            LocalDateTime next1 = MineKoTH.getInstance().getScheduleManager().getNextOccurrence(s1);
            LocalDateTime next2 = MineKoTH.getInstance().getScheduleManager().getNextOccurrence(s2);
            return next1.compareTo(next2);
        });

        sender.sendMessage(ChatColor.GOLD + "KoTH Schedules:");

        LocalDateTime now = LocalDateTime.now();
        boolean first = true;

        for (Schedule schedule : schedules) {
            LocalDateTime nextOccurrence = MineKoTH.getInstance().getScheduleManager().getNextOccurrence(schedule);
            ChatColor color = first ? ChatColor.BLUE : ChatColor.GREEN;

            sender.sendMessage(color + formatSchedule(schedule, nextOccurrence));
            if (nextOccurrence.isAfter(now)) {
                first = false; // Only the next schedule is highlighted in blue
            }
        }
    }

    private static String formatSchedule(Schedule schedule, LocalDateTime nextOccurrence) {
        return String.format(
                "ID: %d | KoTH: %s | Days: %s | Time: %02d:%02d | Next: %s",
                schedule.getId(),
                schedule.getKoTH().getName(),
                schedule.getDays().stream().map(Enum::name).collect(Collectors.joining(", ")),
                schedule.getHour(),
                schedule.getMinute(),
                nextOccurrence != null ? nextOccurrence.toString() : "N/A"
        );
    }

    private static void sendHelp(Player sender) {
        sender.sendMessage(ChatColor.YELLOW + "Usage of /koth schedule:");
        sender.sendMessage(ChatColor.AQUA + "list" + ChatColor.WHITE + " - Lists all schedules ordered by date and time.");
    }
}
