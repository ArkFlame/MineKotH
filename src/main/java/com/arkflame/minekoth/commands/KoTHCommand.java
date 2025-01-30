package com.arkflame.minekoth.commands;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.arkflame.minekoth.setup.commands.SetupCommand;
import com.arkflame.minekoth.utils.FoliaAPI;
import com.arkflame.minekoth.utils.Sounds;
import com.arkflame.minekoth.utils.Titles;
import com.arkflame.minekoth.MineKoth;
import com.arkflame.minekoth.koth.Koth;
import com.arkflame.minekoth.koth.events.CapturingPlayers;
import com.arkflame.minekoth.koth.events.KothEvent;
import com.arkflame.minekoth.koth.events.KothEvent.KothEventState;
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
                kothManager.getAllkoths().values().forEach(koth -> player
                        .sendMessage(ChatColor.YELLOW + "ID: " + koth.getId() + ", Name: " + koth.getName()));
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
                Koth koth = schedule.getKoth();
                if (koth == null) {
                    sender.sendMessage(ChatColor.RED + "Invalid koth.");
                    break;
                }
                if (MineKoth.getInstance().getKothEventManager().getKothEvent(koth) != null) {
                    sender.sendMessage(ChatColor.RED + "Koth is already running.");
                    break;
                }
                MineKoth.getInstance().getKothEventManager().start(koth);
                sender.sendMessage(ChatColor.GREEN + "Ran next schedule (" + koth.getName() + ")");
                sender.sendMessage(ChatColor.GREEN + "Koths running:");
                for (KothEvent k : MineKoth.getInstance().getKothEventManager().getRunningKoths()) {
                    sender.sendMessage(ChatColor.GREEN + " - " + k.getKoth().getName());
                }
                break;
            case "stop":
                if (!MineKoth.getInstance().getKothEventManager().isEventActive()) {
                    sender.sendMessage(ChatColor.RED + "No event is active.");
                    break;
                }
                if (MineKoth.getInstance().getKothEventManager().getKothEvent().getState() == KothEventState.CAPTURED) {
                    sender.sendMessage(ChatColor.RED + "Koth is already captured, wait.");
                    break;
                }
                MineKoth.getInstance().getKothEventManager().end();
                sender.sendMessage(ChatColor.GREEN + "Event stopped.");
                Titles.sendTitle("&aStopped", "&eKoth stopped by " + player.getName(),
                        10, 60, 10);
                break;
            case "capture":
                if (!MineKoth.getInstance().getKothEventManager().isEventActive()) {
                    sender.sendMessage(ChatColor.RED + "No event is active.");
                    break;
                }
                if (MineKoth.getInstance().getKothEventManager().getKothEvent().getState() == KothEventState.CAPTURED) {
                    sender.sendMessage(ChatColor.RED + "Koth is already captured, wait.");
                    break;
                }
                MineKoth.getInstance().getKothEventManager().getKothEvent().setCaptured(new CapturingPlayers(player));
                break;
            case "teleport":
            case "tp":
                if (args.length < 2) {
                    player.sendMessage(ChatColor.RED + "Usage: /koth tp <id/name>");
                    break;
                }

                String kothIdOrName = String.join(" ", args).substring(args[0].length() + 1);
                Koth tpKoth;
                try {
                    tpKoth = MineKoth.getInstance().getKothManager().getKothById(Integer.parseInt(kothIdOrName));
                } catch (NumberFormatException e) {
                    tpKoth = MineKoth.getInstance().getKothManager().getKothByName(kothIdOrName);
                }

                if (tpKoth == null) {
                    player.sendMessage(ChatColor.RED + "Could not find koth with ID or name: " + kothIdOrName);
                    break;
                }

                Location center = tpKoth.getCenter();

                FoliaAPI.runTask(() -> {
                    player.teleport(center);
                    Sounds.play(player, 1, 1, "ENTITY_ENDERMAN_TELEPORT");
                });
                break;
            case "bet":
                // participant can be any online player
                // kothId or name is optional, if not input, kothEventManager.getKothEvent() is
                // used
                if (args.length < 3) {
                    player.sendMessage(ChatColor.RED + "Usage: /koth bet <amount> <participant> [kothId/name]");
                    break;
                }
                // Join all args from kothId/name to the end to get the kothid or name
                String kothIdOrNameBet = String.join(" ", args)
                        .substring(args[0].length() + args[1].length() + args[2].length() + 2).strip();
                KothEvent betKothEvent = null;
                Koth betKoth = null;
                try {
                    betKoth = MineKoth.getInstance().getKothManager().getKothById(Integer.parseInt(kothIdOrNameBet));
                } catch (NumberFormatException e) {
                    betKoth = MineKoth.getInstance().getKothManager().getKothByName(kothIdOrNameBet);
                }

                if (betKoth != null) {
                    betKothEvent = MineKoth.getInstance().getKothEventManager().getKothEvent(betKoth);
                }

                if (betKothEvent == null && kothIdOrNameBet.isEmpty()) {
                    betKothEvent = MineKoth.getInstance().getKothEventManager().getKothEvent();
                }

                if (betKothEvent == null) {
                    if (MineKoth.getInstance().getKothEventManager().getRunningKoths().length == 0) {
                        player.sendMessage(ChatColor.RED + "No koths are running.");
                    } else {
                        player.sendMessage(ChatColor.RED + "Could not find koth with ID or name: " + kothIdOrNameBet);
                        player.sendMessage(ChatColor.RED + "Available Koths:");
                        for (KothEvent running : MineKoth.getInstance().getKothEventManager().getRunningKoths()) {
                            player.sendMessage(ChatColor.RED + " - " + running.getKoth().getName());
                        }
                    }
                    break;
                }

                if (args.length < 3) {
                    player.sendMessage(ChatColor.RED + "Usage: /koth bet <amount> <participant> <kothId/name>");
                    break;
                }

                betKothEvent.getKothEventBets().placeBet(player, args[2], Double.parseDouble(args[1]));
                break;
            default:
                player.sendMessage(ChatColor.RED + "Unknown subcommand. Use /koth help for a list of commands.");
                break;
        }

        return true;
    }

    private void sendHelpMessage(Player player) {
        player.sendMessage(ChatColor.GOLD + "Usage of koth commands:");
        player.sendMessage(
                ChatColor.YELLOW + " /koth setup" + ChatColor.WHITE + " - Finish and save the current koth setup.");
        player.sendMessage(ChatColor.YELLOW + " /koth schedule" + ChatColor.WHITE + " - Schedule a koth.");
        player.sendMessage(ChatColor.YELLOW + " /koth list" + ChatColor.WHITE + " - List all existing koths.");
        player.sendMessage(
                ChatColor.YELLOW + " /koth info <id>" + ChatColor.WHITE + " - Get details about a specific koth.");
        player.sendMessage(ChatColor.YELLOW + " /koth delete <id>" + ChatColor.WHITE + " - Delete a specific koth.");
        player.sendMessage(ChatColor.YELLOW + " /koth start" + ChatColor.WHITE + " - Start the next scheduled koth.");
        player.sendMessage(ChatColor.YELLOW + " /koth stop" + ChatColor.WHITE + " - Stop the current koth event.");
        player.sendMessage(ChatColor.YELLOW + " /koth capture" + ChatColor.WHITE + " - Capture the current koth");
        player.sendMessage(ChatColor.YELLOW + " /koth tp <id/name>" + ChatColor.WHITE + " - Teleport to a koth");
    }

    private void sendkothInfo(Player player, Koth koth) {
        player.sendMessage(ChatColor.GOLD + "koth Info:");
        player.sendMessage(ChatColor.YELLOW + "ID: " + koth.getId());
        player.sendMessage(ChatColor.YELLOW + "Name: " + koth.getName());
        player.sendMessage(ChatColor.YELLOW + "World: " + koth.getWorldName());
        player.sendMessage(ChatColor.YELLOW + "Time Limit: " + koth.getTimeLimit());
        player.sendMessage(ChatColor.YELLOW + "Time to Capture: " + koth.getTimeToCapture());
        player.sendMessage(
                ChatColor.GREEN + "Reward Items: " + ChatColor.AQUA + koth.getRewards().getRewardsItems().size());
        player.sendMessage(
                ChatColor.GREEN + "Reward Commands: " + ChatColor.AQUA + koth.getRewards().getRewardsCommands());
    }
}
