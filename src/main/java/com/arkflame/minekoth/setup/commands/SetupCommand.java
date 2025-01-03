package com.arkflame.minekoth.setup.commands;

import org.bukkit.entity.Player;

import com.arkflame.minekoth.MineKoth;
import com.arkflame.minekoth.koth.Koth;
import com.arkflame.minekoth.koth.Rewards;
import com.arkflame.minekoth.setup.session.SetupSession;

import net.md_5.bungee.api.ChatColor;

public class SetupCommand {
    public static void run(Player player, String[] args) {
        SetupSession session = MineKoth.getInstance().getSessionManager().getSession(player);
        if (session == null) {
            player.sendMessage(ChatColor.RED + "You are not in a setup session.");
            return;
        }

        if (!session.isComplete()) {
            player.sendMessage(ChatColor.RED + "Setup is not complete. Finish all steps before using this command.");
            return;
        }

        // Save the koth instance to your storage system here.
        Koth koth = new Koth(MineKoth.getInstance().getKothManager().getNextId(), session.getName(), player.getWorld().getName(), session.getFirstPosition(),
                session.getSecondPosition(), session.getTimeLimit(), session.getCaptureTime(),
                new Rewards(session.getRewardsCommands(), session.getRewards()), session.getTimes());
        MineKoth.getInstance().getSessionManager().removeSession(player);
        player.sendMessage(ChatColor.GREEN + "koth setup complete! koth saved.");
        player.sendMessage(ChatColor.GREEN + "Name: " + ChatColor.AQUA + session.getName());
        player.sendMessage(ChatColor.GREEN + "Times: " + ChatColor.AQUA + session.getTimes());
        player.sendMessage(ChatColor.GREEN + "Time Limit: " + ChatColor.AQUA + session.getTimeLimit() + " seconds");
        player.sendMessage(ChatColor.GREEN + "Capture Time: " + ChatColor.AQUA + session.getCaptureTime() + " seconds");
        player.sendMessage(ChatColor.GREEN + "Rewards: " + ChatColor.AQUA + session.getRewardsCommands());
        MineKoth.getInstance().getKothManager().addKoth(koth);
        MineKoth.getInstance().getScheduleManager().scheduleKoth(koth);
        return;
    }
}
