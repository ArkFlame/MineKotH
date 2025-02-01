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
import com.arkflame.minekoth.lang.Lang;
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
        Lang lang = MineKoth.getInstance().getLangManager().getLang(player);

        if (!session.isNameSet()) {
            session.setName(message);
            player.sendMessage(lang.getMessage("messages.name-set").replace("<value>", message));
            if (!session.isFirstPositionSet() || !session.isSecondPositionSet()) {
                player.sendMessage(lang.getMessage("messages.set-positions"));
            }
        } else if (!session.isFirstPositionSet() || !session.isSecondPositionSet()) {
            player.sendMessage(lang.getMessage("messages.set-first-positions"));
            return;
        } else if (!session.isTimesSet()) {
            if (!session.isValidTimes(message)) {
                player.sendMessage(lang.getMessage("messages.invalid-times"));
                return;
            }
            session.setTimes(message);
            player.sendMessage(lang.getMessage("messages.times-set").replace("<value>", message));
            
            if (!session.isDaysSet()) {
                player.sendMessage(lang.getMessage("messages.enter-days"));
            }
        } else if (!session.isDaysSet()) {
            if (!session.isValidDays(message)) {
                player.sendMessage(lang.getMessage("messages.invalid-days"));
                return;
            }
            session.setDays(message);
            player.sendMessage(lang.getMessage("messages.days-set").replace("<value>", message));
            
            if (!session.isTimeLimitSet()) {
                player.sendMessage(lang.getMessage("messages.enter-time-limit"));
            }
        } else if (!session.isTimeLimitSet()) {
            if (!session.isValidTimeLimit(message)) {
                player.sendMessage(lang.getMessage("messages.invalid-time-limit"));
                return;
            }
            session.setTimeLimit(Times.parseToSeconds(message));
            player.sendMessage(lang.getMessage("messages.time-limit-set").replace("<value>", message));
            if (!session.isCaptureTimeSet()) {
                player.sendMessage(lang.getMessage("messages.enter-capture-time"));
            }
        } else if (!session.isCaptureTimeSet()) {
            if (!session.isValidCaptureTime(message)) {
                player.sendMessage(lang.getMessage("messages.invalid-capture-time"));
                return;
            }
            session.setCaptureTime(Times.parseToSeconds(message));
            player.sendMessage(lang.getMessage("messages.capture-time-set").replace("<value>", message));
            
            if (!session.isRewardsSet()) {
                player.sendMessage(lang.getMessage("messages.put-rewards"));
                openRewardsInventory(player);
            }
        } else if (!session.isLootTypeSet()) {
            if (!session.isValidLootType(message)) {
                player.sendMessage(lang.getMessage("messages.invalid-loot-type"));
                return;
            }
            session.setLootType(message);
            player.sendMessage(lang.getMessage("messages.loot-type-set").replace("<value>", message));
            if (!session.isLootAmountSet()) {
                player.sendMessage(lang.getMessage("messages.enter-loot-amount"));
            } else if (!session.isRewardsCommandsSet()) {
                player.sendMessage(lang.getMessage("messages.enter-rewards-commands"));
                player.sendMessage(lang.getMessage("messages.example-rewards-commands"));
                player.sendMessage(lang.getMessage("messages.finish-setup"));
            }
        } else if (!session.isLootAmountSet()) {
            if (!session.isValidLootAmount(message)) {
                player.sendMessage(lang.getMessage("messages.invalid-loot-amount"));
                return;
            }
            session.setLootAmount(Integer.parseInt(message));
            player.sendMessage(lang.getMessage("messages.loot-amount-set").replace("<value>", message));
            if (!session.isRewardsCommandsSet()) {
                player.sendMessage(lang.getMessage("messages.enter-rewards-commands"));
                player.sendMessage(lang.getMessage("messages.example-rewards-commands"));
                player.sendMessage(lang.getMessage("messages.finish-setup"));
            }
        } else {
            session.addRewardsCommand(Collections.singletonList(message));
            player.sendMessage(lang.getMessage("messages.rewards-command-added").replace("<value>", message));
            player.sendMessage(lang.getMessage("messages.setup-complete"));
        }
        if (session.isEditing() && !session.isEditingRewards() && session.isComplete()) {
            SetupCommand.handleFinish(player, null);
            FoliaAPI.runTask(() -> {
                new KothEditMenu(player, MineKoth.getInstance().getKothManager().getKothById(session.getId())).open(player);
            });
        }
    }

    public static void openRewardsInventory(Player player) {
        FoliaAPI.runTask(() -> {
            Inventory inventory = Bukkit.createInventory(null, 27, ChatColor.DARK_GREEN + "Rewards");
            player.openInventory(inventory);
        });
    }
}
