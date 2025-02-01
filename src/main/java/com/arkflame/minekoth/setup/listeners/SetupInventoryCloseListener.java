package com.arkflame.minekoth.setup.listeners;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import com.arkflame.minekoth.MineKoth;
import com.arkflame.minekoth.lang.Lang;
import com.arkflame.minekoth.setup.session.SetupSession;

public class SetupInventoryCloseListener implements Listener {

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        Player player = (Player) event.getPlayer();
        SetupSession session = MineKoth.getInstance().getSessionManager().getSession(player);
        if (session == null) {
            return;
        }
        if (event.getInventory() == null || event.getInventory().getType() != InventoryType.CHEST) {
            return;
        }

        Lang lang = MineKoth.getInstance().getLangManager().getLang(player);

        if (!session.isRewardsSet()) {
            List<ItemStack> items = new ArrayList<>();
            Inventory inventory = event.getInventory();
            for (int i = 0; i < 27; i++) {
                ItemStack item = inventory.getItem(i);
                if (item != null && item.getType() != Material.AIR && item.getAmount() > 0) {
                    items.add(item);
                }
            }
            session.setRewards(items.toArray(new ItemStack[0]));
            player.sendMessage(lang.getMessage("messages.rewards-set").replace("<count>", String.valueOf(items.size())));
            if (!session.isLootTypeSet()) {
                player.sendMessage(lang.getMessage("messages.enter-loot-type"));
            }
        } else {
            player.sendMessage(lang.getMessage("messages.rewards-already-set"));
        }
    }
}
