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
import com.arkflame.minekoth.menus.KothEditMenu;
import com.arkflame.minekoth.setup.commands.SetupCommand;
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
            if (!session.isFirstPositionSet() || !session.isSecondPositionSet()) {
                player.sendMessage(ChatColor.GREEN + "Right-click to set Position 1 and Position 2.");
            }
        } else if (!session.isFirstPositionSet() || !session.isSecondPositionSet()) {
            player.sendMessage(ChatColor.RED + "You must set the positions of the koth first.");
            return;
        } else if (!session.isTimesSet()) {
            if (!session.isValidTimes(message)) {
                player.sendMessage(ChatColor.RED + "Invalid times. Please enter a valid one.");
                return;
            }
            session.setTimes(message);
            player.sendMessage(ChatColor.GREEN + "Run times set to: " + ChatColor.AQUA + message);
            
            if (!session.isDaysSet()) {
                player.sendMessage(ChatColor.GREEN + "Enter days (e.g., MONDAY, TUESDAY, ALL...).");
            }
        } else if (!session.isDaysSet()) {
            if (!session.isValidDays(message)) {
                player.sendMessage(ChatColor.RED + "Invalid days. Please enter a valid one.");
                return;
            }
            session.setDays(message);
            player.sendMessage(ChatColor.GREEN + "Days set to: " + ChatColor.AQUA + message);
            
            if (!session.isTimeLimitSet()) {
                player.sendMessage(ChatColor.GREEN + "Enter the time limit (e.g., 30min).");
            }
        } else if (!session.isTimeLimitSet()) {
            if (!session.isValidTimeLimit(message)) {
                player.sendMessage(ChatColor.RED + "Invalid time limit. Please enter a valid one.");
                return;
            }
            session.setTimeLimit(Times.parseToSeconds(message));
            player.sendMessage(ChatColor.GREEN + "Time limit set to: " + ChatColor.AQUA + message);
            if (!session.isCaptureTimeSet()) {
                player.sendMessage(ChatColor.GREEN + "Enter the capture time (e.g., 5min).");
            }
        } else if (!session.isCaptureTimeSet()) {
            if (!session.isValidCaptureTime(message)) {
                player.sendMessage(ChatColor.RED + "Invalid capture time. Please enter a valid one.");
                return;
            }
            session.setCaptureTime(Times.parseToSeconds(message));
            player.sendMessage(ChatColor.GREEN + "Capture time set to: " + ChatColor.AQUA + message);
            
            if (!session.isRewardsSet()) {
                player.sendMessage(ChatColor.GREEN + "Put rewards in the chest.");
                openRewardsInventory(player);
            }
        } else if (!session.isLootTypeSet()) {
            if (!session.isValidLootType(message)) {
                player.sendMessage(ChatColor.RED + "Invalid loot type. Please enter a valid one. (DEFAULT, RANDOM)");
                return;
            }
            session.setLootType(message);
            player.sendMessage(ChatColor.GREEN + "Loot type set to: " + ChatColor.AQUA + message);
            if (!session.isLootAmountSet()) {
                player.sendMessage(ChatColor.GREEN + "Enter the loot amount.");
            } else if (!session.isRewardsCommandsSet()) {
                player.sendMessage(ChatColor.GREEN + "Enter the rewards commands.");
                player.sendMessage(ChatColor.GREEN + "Example: " + ChatColor.BLUE + "give %player% diamond 1");
                player.sendMessage(ChatColor.GREEN + "Type /koth setup finish when done.");
            }
        } else if (!session.isLootAmountSet()) {
            if (!session.isValidLootAmount(message)) {
                player.sendMessage(ChatColor.RED + "Invalid loot amount. Please enter a valid one. (e.g., 1)");
                return;
            }
            session.setLootAmount(Integer.parseInt(message));
            player.sendMessage(ChatColor.GREEN + "Loot amount set to: " + ChatColor.AQUA + message);
            if (!session.isRewardsCommandsSet()) {
                player.sendMessage(ChatColor.GREEN + "Enter the rewards commands.");
                player.sendMessage(ChatColor.GREEN + "Example: " + ChatColor.BLUE + "give %player% diamond 1");
                player.sendMessage(ChatColor.GREEN + "Type /koth setup finish when done.");
            }
        } else {
            session.addRewardsCommand(Collections.singletonList(message));
            player.sendMessage(ChatColor.GREEN + "Rewards commands added: " + ChatColor.AQUA + message);
            player.sendMessage(ChatColor.GREEN + "koth setup complete. Type /koth setup to finish.");
        }
        if (session.isEditing() && !session.isEditingRewards() && session.isComplete()) {
            SetupCommand.handleFinish(player, null);
            new KothEditMenu(MineKoth.getInstance().getKothManager().getKothById(session.getId())).open(player);
        }
    }

    public static void openRewardsInventory(Player player) {
        FoliaAPI.runTask(() -> {
            Inventory inventory = Bukkit.createInventory(null, 27, ChatColor.DARK_GREEN + "Rewards");
            player.openInventory(inventory);
        });
    }

}
