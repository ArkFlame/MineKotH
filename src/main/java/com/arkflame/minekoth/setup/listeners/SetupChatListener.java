package com.arkflame.minekoth.setup.listeners;

import java.util.Collection;
import java.util.Collections;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import com.arkflame.minekoth.MineKoth;
import com.arkflame.minekoth.koth.Koth;
import com.arkflame.minekoth.lang.Lang;
import com.arkflame.minekoth.setup.commands.SetupCommand;
import com.arkflame.minekoth.setup.session.SetupSession;
import com.arkflame.minekoth.utils.FoliaAPI;
import com.arkflame.minekoth.utils.KothScheduleUtil;
import com.arkflame.minekoth.utils.Times;

public class SetupChatListener implements Listener {

    @EventHandler(ignoreCancelled = true, priority = EventPriority.LOW)
    public void onPlayerChat(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();
        SetupSession session = MineKoth.getInstance().getSessionManager().getSession(player);
        if (session == null) {
            return;
        }

        event.setCancelled(true);

        String message = event.getMessage();
        message = ChatColor.stripColor(message.trim());
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
            message = message.toLowerCase();
            message = KothScheduleUtil.processMessage(message);
            if (message == null) {
                player.sendMessage(lang.getMessage("messages.invalid-times"));
                return;
            }
            session.setTimes(message);
            player.sendMessage(lang.getMessage("messages.times-set").replace("<value>", message));

            if (!session.isDaysSet()) {
                player.sendMessage(lang.getMessage("messages.enter-days"));
            }
        } else if (!session.isDaysSet()) {
            message = message.toUpperCase();
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
            int time = Times.parseToSeconds(message);
            if (!session.isValidTimeLimit(message) || time == 0) {
                player.sendMessage(lang.getMessage("messages.invalid-time-limit"));
                return;
            }
            session.setTimeLimit(time);
            player.sendMessage(
                    lang.getMessage("messages.time-limit-set").replace("<value>", Times.formatSeconds(time)));
            if (!session.isCaptureTimeSet()) {
                player.sendMessage(lang.getMessage("messages.enter-capture-time"));
            }
        } else if (!session.isCaptureTimeSet()) {
            int time = Times.parseToSeconds(message);
            if (!session.isValidCaptureTime(message) || time == 0) {
                player.sendMessage(lang.getMessage("messages.invalid-capture-time"));
                return;
            }
            session.setCaptureTime(time);
            player.sendMessage(
                    lang.getMessage("messages.capture-time-set").replace("<value>", Times.formatSeconds(time)));

            if (!session.isRewardsSet()) {
                player.sendMessage(lang.getMessage("messages.put-rewards"));
                openRewardsInventory(player, session.getKoth());
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
        }
    }

    public static void openRewardsInventory(Player player, Koth koth) {
        FoliaAPI.runTask(() -> {
            Inventory inventory = Bukkit.createInventory(null, 27, ChatColor.DARK_GREEN + "Rewards");
            Collection<ItemStack> rewards = koth.getRewards().getRewardsItems();
            if (rewards != null) {
                for (ItemStack oldReward : rewards) {
                    inventory.addItem(oldReward);
                }
            }
            player.openInventory(inventory);
        });
    }
}
