package com.arkflame.minekoth.schedule.commands;

import com.arkflame.minekoth.MineKoth;
import com.arkflame.minekoth.koth.KothTime;
import com.arkflame.minekoth.lang.Lang;
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
        Lang lang = MineKoth.getInstance().getLangManager().getLang(sender);

        switch (subCommand) {
            case "list":
                listSchedules(sender, lang);
                break;
            case "remove":
                if (!sender.hasPermission("minekoth.command.schedule.remove")) {
                    sender.sendMessage(MineKoth.getInstance().getLangManager().getLang(sender).getMessage("messages.no-permission")
                    .replace("<node>", "minekoth.command.schedule.remove"));
                    return;
                }

                if (args.length < 3) {
                    sender.sendMessage(lang.getMessage("messages.usage-remove"));
                    return;
                }

                try {
                    int id = Integer.parseInt(args[2]);
                    MineKoth.getInstance().getScheduleManager().removeSchedule(id);
                    MineKoth.getInstance().getScheduleLoader().delete(id);
                    sender.sendMessage(lang.getMessage("messages.schedule-removed"));
                } catch (NumberFormatException e) {
                    sender.sendMessage(lang.getMessage("messages.invalid-schedule-id"));
                }
                break;
            case "add":
                if (!sender.hasPermission("minekoth.command.schedule.add")) {
                    sender.sendMessage(MineKoth.getInstance().getLangManager().getLang(sender).getMessage("messages.no-permission")
                    .replace("<node>", "minekoth.command.schedule.add"));
                    return;
                }

                if (args.length < 4) {
                    sender.sendMessage(lang.getMessage("messages.usage-add"));
                    return;
                }

                try {
                    int kothId = Integer.parseInt(args[2]);
                    if (MineKoth.getInstance().getKothManager().getKothById(kothId) == null) {
                        sender.sendMessage(lang.getMessage("messages.invalid-koth-id"));
                        return;
                    }
                    String[] timeEntries = args[3].split(",");
                    for (String timeEntry : timeEntries) {
                        String[] dayNames = args.length > 4 ? args[4].split(",") : new String[]{"MONDAY", "TUESDAY", "WEDNESDAY", "THURSDAY", "FRIDAY", "SATURDAY", "SUNDAY"};
                        if (dayNames.length == 0) {
                            sender.sendMessage(lang.getMessage("messages.invalid-day-names"));
                            return;
                        }
                        KothTime kothTime = Times.parseTimeEntry(timeEntry);
                        if (kothTime == null) {
                            sender.sendMessage(lang.getMessage("messages.invalid-time-format"));
                            return;
                        }
                        Schedule schedule = MineKoth.getInstance().getScheduleManager().scheduleKoth(kothId, kothTime.getHour(), kothTime.getMinute(), dayNames);
                        sender.sendMessage(lang.getMessage("messages.schedule-added").replace("<schedule>", formatSchedule(schedule, MineKoth.getInstance().getScheduleManager().getNextOccurrence(schedule))));
                    }
                } catch (NumberFormatException e) {
                    sender.sendMessage(lang.getMessage("messages.invalid-koth-id-hour-minute"));
                }
                break;
            default:
                sendHelp(sender);
                break;
        }
    }

    private static void listSchedules(Player sender, Lang lang) {
        if (!sender.hasPermission("minekoth.command.schedule.list")) {
            sender.sendMessage(MineKoth.getInstance().getLangManager().getLang(sender).getMessage("messages.no-permission")
            .replace("<node>", "minekoth.command.schedule.list"));
            return;
        }

        List<Schedule> schedules = MineKoth.getInstance().getScheduleManager().getAllSchedules();

        if (schedules.isEmpty()) {
            sender.sendMessage(lang.getMessage("messages.no-schedules-available"));
            return;
        }

        // Sort schedules by next occurrence
        schedules.sort((s1, s2) -> {
            LocalDateTime next1 = MineKoth.getInstance().getScheduleManager().getNextOccurrence(s1);
            LocalDateTime next2 = MineKoth.getInstance().getScheduleManager().getNextOccurrence(s2);
            return next1.compareTo(next2);
        });

        sender.sendMessage(lang.getMessage("messages.koth-schedules"));

        LocalDateTime now = LocalDateTime.now();
        boolean first = true;

        for (Schedule schedule : schedules) {
            LocalDateTime nextOccurrence = MineKoth.getInstance().getScheduleManager().getNextOccurrence(schedule);
            ChatColor color = first ? ChatColor.AQUA : ChatColor.GREEN;

            sender.sendMessage(color + formatSchedule(schedule, nextOccurrence));
            if (nextOccurrence != null && nextOccurrence.isAfter(now)) {
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
        Lang lang = MineKoth.getInstance().getLangManager().getLang(sender);
        sender.sendMessage(lang.getMessage("messages.schedule-help"));
    }
}
