package com.arkflame.minekoth.commands;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.arkflame.minekoth.setup.commands.SetupCommand;
import com.arkflame.minekoth.utils.Titles;
import com.arkflame.minekoth.MineKoth;
import com.arkflame.minekoth.koth.Koth;
import com.arkflame.minekoth.koth.managers.KothManager;
import com.arkflame.minekoth.schedule.Schedule;
import com.arkflame.minekoth.schedule.commands.ScheduleCommand;
import com.arkflame.minekoth.schedule.managers.ScheduleManager;

public class KothCommand implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "Only players can use this command.");
            return true;
        }

        Player player = (Player) sender;
        KothManager kothManager = MineKoth.getInstance().getKothManager();
        ScheduleManager scheduleManager = MineKoth.getInstance().getScheduleManager();

        if (args.length == 0 || args[0].equalsIgnoreCase("help")) {
            sendHelpMessage(player);
            return true;
        }

        switch (args[0].toLowerCase()) {

            case "setup":
                SetupCommand.run(player, args);
                break;

            case "list":
                if (kothManager.getAllkoths().isEmpty()) {
                    player.sendMessage(ChatColor.RED + "There are no koths to list.");
                    break;
                }
                player.sendMessage(ChatColor.GOLD + "koth List:");
                kothManager.getAllkoths().values().forEach(koth ->
                        player.sendMessage(ChatColor.YELLOW + "ID: " + koth.getId() + ", Name: " + koth.getName()));
                break;

            case "info":
                if (args.length < 2) {
                    player.sendMessage(ChatColor.RED + "Usage: /koth info <id>");
                    break;
                }
                try {
                    int id = Integer.parseInt(args[1]);
                    Koth koth = kothManager.getKothById(id);
                    if (koth == null) {
                        player.sendMessage(ChatColor.RED + "No koth found with ID " + id + ".");
                        break;
                    }
                    sendkothInfo(player, koth);
                } catch (NumberFormatException e) {
                    player.sendMessage(ChatColor.RED + "Invalid koth ID. It must be a number.");
                }
                break;
            case "delete":
                if (args.length < 2) {
                    player.sendMessage(ChatColor.RED + "Usage: /koth delete <id>");
                    break;
                }
                try {
                    int id = Integer.parseInt(args[1]);
                    Koth removed = kothManager.deleteKoth(id);
                    scheduleManager.removeSchedulesByKoth(id);
                    if (removed == null) {
                        player.sendMessage(ChatColor.RED + "No koth found with ID " + id + ".");
                        return true;
                    }
                    player.sendMessage(ChatColor.GREEN + "koth with ID " + id + " deleted successfully.");
                } catch (NumberFormatException e) {
                    player.sendMessage(ChatColor.RED + "Invalid koth ID. It must be a number.");
                }
                break;
            case "schedule":
                ScheduleCommand.run(player, args);
                break;
            case "start":
                Schedule schedule = MineKoth.getInstance().getScheduleManager().getNextSchedule();
                if (schedule == null) {
                    sender.sendMessage(ChatColor.RED + "No schedules available.");
                    break;
                }
                if (MineKoth.getInstance().getKothEventManager().isEventActive()) {
                    sender.sendMessage(ChatColor.RED + "An event is already active.");
                    break;
                }
                Koth koth = schedule.getKoth();
                if (koth == null) {
                    sender.sendMessage(ChatColor.RED + "Invalid koth.");
                    break;
                }
                MineKoth.getInstance().getKothEventManager().start(koth);
                sender.sendMessage(ChatColor.GREEN + "Schedules started.");
                break;
            case "stop":
                if (!MineKoth.getInstance().getKothEventManager().isEventActive()) {
                    sender.sendMessage(ChatColor.RED + "No event is active.");
                    break;
                }
                MineKoth.getInstance().getKothEventManager().end();
                sender.sendMessage(ChatColor.GREEN + "Event stopped.");
                Titles.sendTitle(ChatColor.GREEN + "Stopped", ChatColor.YELLOW + "Koth stopped by " + player.getName(), 10, 60, 10);
                break;
            default:
                player.sendMessage(ChatColor.RED + "Unknown subcommand. Use /koth help for a list of commands.");
                break;
        }

        return true;
    }

    private void sendHelpMessage(Player player) {
        player.sendMessage(ChatColor.GOLD + "Usage of koth commands:");
        player.sendMessage(ChatColor.YELLOW + " /koth setup" + ChatColor.WHITE + " - Finish and save the current koth setup.");
        player.sendMessage(ChatColor.YELLOW + " /koth schedule" + ChatColor.WHITE + " - Schedule a koth.");
        player.sendMessage(ChatColor.YELLOW + " /koth list" + ChatColor.WHITE + " - List all existing koths.");
        player.sendMessage(ChatColor.YELLOW + " /koth info <id>" + ChatColor.WHITE + " - Get details about a specific koth.");
        player.sendMessage(ChatColor.YELLOW + " /koth delete <id>" + ChatColor.WHITE + " - Delete a specific koth.");
        player.sendMessage(ChatColor.YELLOW + " /koth start" + ChatColor.WHITE + " - Start the next scheduled koth.");
        player.sendMessage(ChatColor.YELLOW + " /koth stop" + ChatColor.WHITE + " - Stop the current koth event.");
    }

    private void sendkothInfo(Player player, Koth koth) {
        player.sendMessage(ChatColor.GOLD + "koth Info:");
        player.sendMessage(ChatColor.YELLOW + "ID: " + koth.getId());
        player.sendMessage(ChatColor.YELLOW + "Name: " + koth.getName());
        player.sendMessage(ChatColor.YELLOW + "World: " + koth.getWorldName());
        player.sendMessage(ChatColor.YELLOW + "Time Limit: " + koth.getTimeLimit());
        player.sendMessage(ChatColor.YELLOW + "Time to Capture: " + koth.getTimeToCapture());
        player.sendMessage(ChatColor.GREEN + "Rewards: " + ChatColor.AQUA + koth.getRewards().getRewardsCommands());
    }
}
