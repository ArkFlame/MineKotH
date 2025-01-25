package com.arkflame.minekoth.setup.commands;

import org.bukkit.entity.Player;

import com.arkflame.minekoth.MineKoth;
import com.arkflame.minekoth.koth.Koth;
import com.arkflame.minekoth.koth.Rewards;
import com.arkflame.minekoth.setup.session.SetupSession;

import net.md_5.bungee.api.ChatColor;

public class SetupCommand {
    public static void run(Player player, String[] args) {
        if (args.length < 2) {
            player.sendMessage(ChatColor.GOLD + "Usage of setup commands:");
            player.sendMessage(ChatColor.YELLOW + " /koth setup start - Start a new koth setup session.");
            player.sendMessage(ChatColor.YELLOW + " /koth setup cancel - Cancel the current koth setup session.");
            player.sendMessage(ChatColor.YELLOW + " /koth setup finish - Finish the current koth setup session.");
            player.sendMessage(ChatColor.YELLOW + " /koth setup <id> - Edit an existing koth.");
            return;
        }

        switch (args[1].toLowerCase()) {
            case "start":
                if (MineKoth.getInstance().getSessionManager().hasSession(player)) {
                    player.sendMessage(
                            ChatColor.RED + "You are already in a setup session. Use /koth cancel to cancel.");
                    break;
                }
                MineKoth.getInstance().getSessionManager().addSession(player, new SetupSession());
                player.sendMessage(ChatColor.GREEN + "koth creation started! Enter the name of the koth.");
                break;
            case "cancel":
                if (!MineKoth.getInstance().getSessionManager().hasSession(player)) {
                    player.sendMessage(ChatColor.RED + "You are not in a setup session.");
                    break;
                }
                MineKoth.getInstance().getSessionManager().removeSession(player);
                player.sendMessage(ChatColor.RED + "koth creation cancelled.");
                break;
            case "finish":
                SetupSession session = MineKoth.getInstance().getSessionManager().getSession(player);
                if (session == null) {
                    player.sendMessage(ChatColor.RED + "You are not in a setup session.");
                    break;
                }

                if (!session.isComplete()) {
                    player.sendMessage(
                            ChatColor.RED + "Setup is not complete. Finish all steps before using this command.");
                    if (!session.isNameSet()) {
                        player.sendMessage(
                                ChatColor.RED + "You must set the name of the koth first.");
                    }
                    if (!session.isFirstPositionSet()) {
                        player.sendMessage(
                                ChatColor.RED + "You must set the first position of the koth first.");
                    }
                    if (!session.isSecondPositionSet()) {
                        player.sendMessage(
                                ChatColor.RED + "You must set the second position of the koth first.");
                    }
                    if (!session.isTimesSet()) {
                        player.sendMessage(
                                ChatColor.RED + "You must set the times of the koth first.");
                    }
                    if (!session.isTimeLimitSet()) {
                        player.sendMessage(
                                ChatColor.RED + "You must set the time limit of the koth first.");
                    }
                    if (!session.isCaptureTimeSet()) {
                        player.sendMessage(
                                ChatColor.RED + "You must set the capture time of the koth first.");
                    }
                    if (!session.isRewardsSet()) {
                        player.sendMessage(
                                ChatColor.RED + "You must set the rewards of the koth first.");
                    }
                    break;
                }

                int sessionId = session.getId();

                if (sessionId != -1) {
                    MineKoth.getInstance().getKothManager().deleteKoth(session.getId());
                    MineKoth.getInstance().getScheduleManager().removeSchedulesByKoth(session.getId());
                } else {
                    sessionId = MineKoth.getInstance().getKothManager().getNextId();
                }

                // Save the koth instance to your storage system here.
                Koth koth = new Koth(
                    sessionId, 
                    session.getName(),
                    session.getWorldName(), 
                    session.getFirstPosition(),
                    session.getSecondPosition(), 
                    session.getTimeLimit(), 
                    session.getCaptureTime(),
                    new Rewards(
                        session.getRewardsCommands(), 
                        session.getRewards(), 
                        session.getLootType(),
                        session.getLootAmount()),
                    session.getTimes());

                MineKoth.getInstance().getSessionManager().removeSession(player);

                MineKoth.getInstance().getKothManager().addKoth(koth);
                MineKoth.getInstance().getScheduleManager().scheduleKoth(koth.getId(), session.getTimes(),
                        session.getDays());

                player.sendMessage(ChatColor.GREEN + "koth setup complete! koth saved.");
                player.sendMessage(ChatColor.GREEN + "Name: " + ChatColor.AQUA + session.getName());
                player.sendMessage(ChatColor.GREEN + "Times: " + ChatColor.AQUA + session.getTimes());
                player.sendMessage(
                        ChatColor.GREEN + "Time Limit: " + ChatColor.AQUA + session.getTimeLimit() + " seconds");
                player.sendMessage(
                        ChatColor.GREEN + "Capture Time: " + ChatColor.AQUA + session.getCaptureTime() + " seconds");
                player.sendMessage(ChatColor.GREEN + "Rewards: " + ChatColor.AQUA + session.getRewardsCommands());
                break;
            default:
                String kothId = args[1];
                try {
                    int id = Integer.parseInt(kothId);
                    Koth foundKoth = MineKoth.getInstance().getKothManager().getKothById(id);
                    if (foundKoth == null) {
                        player.sendMessage(ChatColor.RED + "Koth with id " + kothId + " not found.");
                        break;
                    }
                    MineKoth.getInstance().getSessionManager().addSession(player, new SetupSession(id));
                    player.sendMessage(ChatColor.GREEN + "koth edit started! Enter the name of the koth.");
                } catch (NumberFormatException e) {
                    player.sendMessage(ChatColor.RED + "Invalid koth ID. It must be a number.");
                }

                break;
        }

        return;
    }
}
