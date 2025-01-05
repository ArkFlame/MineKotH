package com.arkflame.minekoth.schedule.commands;

import com.arkflame.minekoth.MineKoth;
import com.arkflame.minekoth.koth.KothTime;
import com.arkflame.minekoth.schedule.Schedule;
import com.arkflame.minekoth.utils.Times;

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
            // Add more subcommands here
            case "remove":
                if (args.length < 3) {
                    sender.sendMessage(ChatColor.RED + "Usage: /koth schedule remove <id>");
                    return;
                }

                try {
                    int id = Integer.parseInt(args[2]);
                    MineKoth.getInstance().getScheduleManager().removeSchedule(id);
                    sender.sendMessage(ChatColor.GREEN + "Schedule removed.");
                } catch (NumberFormatException e) {
                    sender.sendMessage(ChatColor.RED + "Invalid schedule ID.");
                }
                break;
            case "add":
                if (args.length < 4) {
                    sender.sendMessage(ChatColor.RED + "Usage: /koth schedule add <kothId> <hh:mm,hh:mmm...> [monday,tuesday...]");
                    return;
                }

                try {
                    int kothId = Integer.parseInt(args[2]);
                    String[] timeEntries = args[3].split(",");
                    for (String timeEntry : timeEntries) {
                        String[] dayNames = args.length > 4 ? args[4].split(",") : new String[]{"MONDAY", "TUESDAY", "WEDNESDAY", "THURSDAY", "FRIDAY", "SATURDAY", "SUNDAY"};
                        if (dayNames.length == 0) {
                            sender.sendMessage(ChatColor.RED + "Invalid day names.");
                            return;
                        }
                        KothTime kothTime = Times.parseTimeEntry(timeEntry);
                        if (kothTime == null) {
                            sender.sendMessage(ChatColor.RED + "Invalid time format.");
                            return;
                        }
                        Schedule schedule = MineKoth.getInstance().getScheduleManager().scheduleKoth(kothId, kothTime.getHour(), kothTime.getMinute(), dayNames);
                        sender.sendMessage(ChatColor.GOLD + "Schedule added: " + formatSchedule(schedule, MineKoth.getInstance().getScheduleManager().getNextOccurrence(schedule)));
                    }
                } catch (NumberFormatException e) {
                    sender.sendMessage(ChatColor.RED + "Invalid koth ID, hour, or minute.");
                }
                break;
            default:
                sendHelp(sender);
                break;
        }
    }

    private static void listSchedules(Player sender) {
        List<Schedule> schedules = MineKoth.getInstance().getScheduleManager().getAllSchedules();

        if (schedules.isEmpty()) {
            sender.sendMessage(ChatColor.RED + "There are no schedules available.");
            return;
        }

        // Sort schedules by next occurrence
        schedules.sort((s1, s2) -> {
            LocalDateTime next1 = MineKoth.getInstance().getScheduleManager().getNextOccurrence(s1);
            LocalDateTime next2 = MineKoth.getInstance().getScheduleManager().getNextOccurrence(s2);
            return next1.compareTo(next2);
        });

        sender.sendMessage(ChatColor.GOLD + "koth Schedules:");

        LocalDateTime now = LocalDateTime.now();
        boolean first = true;

        for (Schedule schedule : schedules) {
            LocalDateTime nextOccurrence = MineKoth.getInstance().getScheduleManager().getNextOccurrence(schedule);
            ChatColor color = first ? ChatColor.AQUA : ChatColor.GREEN;

            sender.sendMessage(color + formatSchedule(schedule, nextOccurrence));
            if (nextOccurrence.isAfter(now)) {
                first = false; // Only the next schedule is highlighted in blue
            }
        }
    }

    private static String formatSchedule(Schedule schedule, LocalDateTime nextOccurrence) {
        return String.format(
                "ID: %d | koth: %s | Days: %s | Time: %02d:%02d | Next: %s",
                schedule.getId(),
                schedule.getKoth().getName(),
                schedule.getDays().stream().map(Enum::name).collect(Collectors.joining(", ")),
                schedule.getHour(),
                schedule.getMinute(),
                nextOccurrence != null ? nextOccurrence.toString() : "N/A");
    }

    private static void sendHelp(Player sender) {
        sender.sendMessage(ChatColor.GOLD + "Usage of schedule commands:");
        sender.sendMessage(ChatColor.YELLOW + " /koth schedule list" + ChatColor.WHITE + " - List all schedules.");
        sender.sendMessage(ChatColor.YELLOW + " /koth schedule add <kothId> <hh:mm,hh:mmm...> [monday,tuesday...]" + ChatColor.WHITE + " - Add a new schedule.");
        sender.sendMessage(ChatColor.YELLOW + " /koth schedule remove <id>" + ChatColor.WHITE + " - Remove a schedule.");
    }
}
