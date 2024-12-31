package com.arkflame.minekoth.commands;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.arkflame.minekoth.MineKoTH;
import com.arkflame.minekoth.setup.commands.SetupCommand;
import com.arkflame.minekoth.setup.session.SetupSession;
import com.arkflame.minekoth.koth.KoTH;
import com.arkflame.minekoth.koth.managers.KoTHManager;
import com.arkflame.minekoth.schedule.commands.ScheduleCommand;
import com.arkflame.minekoth.schedule.managers.ScheduleManager;

public class KoTHCommand implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "Only players can use this command.");
            return true;
        }

        Player player = (Player) sender;
        KoTHManager kothManager = MineKoTH.getInstance().getKoTHManager();
        ScheduleManager scheduleManager = MineKoTH.getInstance().getScheduleManager();

        if (args.length == 0 || args[0].equalsIgnoreCase("help")) {
            sendHelpMessage(player);
            return true;
        }

        switch (args[0].toLowerCase()) {
            case "create":
                if (MineKoTH.getInstance().getSessionManager().hasSession(player)) {
                    player.sendMessage(ChatColor.RED + "You are already in a setup session. Use /koth cancel to cancel.");
                    return true;
                }
                MineKoTH.getInstance().getSessionManager().addSession(player, new SetupSession());
                player.sendMessage(ChatColor.GREEN + "KoTH creation started! Enter the name of the KoTH.");
                return true;

            case "cancel":
                if (!MineKoTH.getInstance().getSessionManager().hasSession(player)) {
                    player.sendMessage(ChatColor.RED + "You are not in a setup session.");
                    return true;
                }
                MineKoTH.getInstance().getSessionManager().removeSession(player);
                player.sendMessage(ChatColor.RED + "KoTH creation cancelled.");
                return true;

            case "setup":
                SetupCommand.run(player, args);
                return true;

            case "list":
                if (kothManager.getAllKoTHs().isEmpty()) {
                    player.sendMessage(ChatColor.RED + "There are no KoTHs to list.");
                    return true;
                }
                player.sendMessage(ChatColor.GOLD + "KoTH List:");
                kothManager.getAllKoTHs().values().forEach(koth ->
                        player.sendMessage(ChatColor.YELLOW + "ID: " + koth.getId() + ", Name: " + koth.getName()));
                return true;

            case "info":
                if (args.length < 2) {
                    player.sendMessage(ChatColor.RED + "Usage: /koth info <id>");
                    return true;
                }
                try {
                    int id = Integer.parseInt(args[1]);
                    KoTH koth = kothManager.getKoTHById(id);
                    if (koth == null) {
                        player.sendMessage(ChatColor.RED + "No KoTH found with ID " + id + ".");
                        return true;
                    }
                    sendKoTHInfo(player, koth);
                    return true;
                } catch (NumberFormatException e) {
                    player.sendMessage(ChatColor.RED + "Invalid KoTH ID. It must be a number.");
                    return true;
                }

            case "delete":
                if (args.length < 2) {
                    player.sendMessage(ChatColor.RED + "Usage: /koth delete <id>");
                    return true;
                }
                try {
                    int id = Integer.parseInt(args[1]);
                    KoTH removed = kothManager.deleteKoTH(id);
                    scheduleManager.removeSchedulesByKoTH(id);
                    if (removed == null) {
                        player.sendMessage(ChatColor.RED + "No KoTH found with ID " + id + ".");
                        return true;
                    }
                    player.sendMessage(ChatColor.GREEN + "KoTH with ID " + id + " deleted successfully.");
                    return true;
                } catch (NumberFormatException e) {
                    player.sendMessage(ChatColor.RED + "Invalid KoTH ID. It must be a number.");
                    return true;
                }
            
            case "schedule":
                ScheduleCommand.run(player, args);
                return true;

            default:
                player.sendMessage(ChatColor.RED + "Unknown subcommand. Use /koth help for a list of commands.");
                return true;
        }
    }

    private void sendHelpMessage(Player player) {
        player.sendMessage(ChatColor.GOLD + "KoTH Commands:");
        player.sendMessage(ChatColor.YELLOW + "/koth create" + ChatColor.WHITE + " - Start creating a new KoTH.");
        player.sendMessage(ChatColor.YELLOW + "/koth cancel" + ChatColor.WHITE + " - Cancel the current KoTH setup.");
        player.sendMessage(ChatColor.YELLOW + "/koth setup" + ChatColor.WHITE + " - Finish and save the current KoTH setup.");
        player.sendMessage(ChatColor.YELLOW + "/koth list" + ChatColor.WHITE + " - List all existing KoTHs.");
        player.sendMessage(ChatColor.YELLOW + "/koth info <id>" + ChatColor.WHITE + " - Get details about a specific KoTH.");
        player.sendMessage(ChatColor.YELLOW + "/koth delete <id>" + ChatColor.WHITE + " - Delete a specific KoTH.");
    }

    private void sendKoTHInfo(Player player, KoTH koth) {
        player.sendMessage(ChatColor.GOLD + "KoTH Info:");
        player.sendMessage(ChatColor.YELLOW + "ID: " + koth.getId());
        player.sendMessage(ChatColor.YELLOW + "Name: " + koth.getName());
        player.sendMessage(ChatColor.YELLOW + "World: " + koth.getWorldName());
        player.sendMessage(ChatColor.YELLOW + "Time Limit: " + koth.getTimeLimit());
        player.sendMessage(ChatColor.YELLOW + "Time to Capture: " + koth.getTimeToCapture());
        player.sendMessage(ChatColor.GREEN + "Rewards: " + ChatColor.AQUA + koth.getRewards().getRewardsCommands());
    }
}
