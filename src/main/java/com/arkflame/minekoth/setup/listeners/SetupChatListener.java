package com.arkflame.minekoth.setup.listeners;

import java.util.Collections;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.inventory.Inventory;

import com.arkflame.minekoth.MineKoth;
import com.arkflame.minekoth.setup.session.SetupSession;
import com.arkflame.minekoth.utils.FoliaAPI;
import com.arkflame.minekoth.utils.Times;

public class SetupChatListener implements Listener {

    @EventHandler
    public void onPlayerChat(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();
        SetupSession session = MineKoth.getInstance().getSessionManager().getSession(player);
        if (session == null) {
            return;
        }

        event.setCancelled(true);
        
        String message = event.getMessage();

        if (!session.isNameSet()) {
            session.setName(message);
            player.sendMessage(ChatColor.GREEN + "Name set to: " + ChatColor.AQUA + message);
            player.sendMessage(ChatColor.GREEN + "Right-click to set Position 1 and Position 2.");
            return;
        }

        if (!session.isFirstPositionSet() || !session.isSecondPositionSet()) {
            player.sendMessage(ChatColor.RED + "You must set the positions of the koth first.");
            return;
        }

        if (!session.isTimesSet()) {
            session.setTimes(message);
            player.sendMessage(ChatColor.GREEN + "Run times set to: " + ChatColor.AQUA + message);
            player.sendMessage(ChatColor.GREEN + "Enter the time limit (e.g., 30min).");
            return;
        }

        if (!session.isTimeLimitSet()) {
            session.setTimeLimit(Times.parseToSeconds(message));
            player.sendMessage(ChatColor.GREEN + "Time limit set to: " + ChatColor.AQUA + message);
            player.sendMessage(ChatColor.GREEN + "Enter the capture time (e.g., 5min).");
            return;
        }

        if (!session.isCaptureTimeSet()) {
            session.setCaptureTime(Times.parseToSeconds(message));
            player.sendMessage(ChatColor.GREEN + "Capture time set to: " + ChatColor.AQUA + message);
            player.sendMessage(ChatColor.GREEN + "Enter days (e.g., MONDAY, TUESDAY, ALL...).");
            return;
        }

        if (!session.isDaysSet()) {
            session.setDays(message);
            player.sendMessage(ChatColor.GREEN + "Days set to: " + ChatColor.AQUA + message);
            player.sendMessage(ChatColor.GREEN + "Put rewards in the chest.");
            openRewardsInventory(player);
            return;
        }

        session.addRewardsCommand(Collections.singletonList(message));
        player.sendMessage(ChatColor.GREEN + "Rewards commands added: " + ChatColor.AQUA + message);
        player.sendMessage(ChatColor.GREEN + "koth setup complete. Type /koth setup to finish.");
    }

    private void openRewardsInventory(Player player) {
        FoliaAPI.runTask(MineKoth.getInstance(), () -> {
            Inventory inventory = Bukkit.createInventory(null, 27, ChatColor.DARK_GREEN + "Rewards");
            player.openInventory(inventory);
        });
    }

}
