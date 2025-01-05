package com.arkflame.minekoth.setup.listeners;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryCloseEvent;

import com.arkflame.minekoth.MineKoth;
import com.arkflame.minekoth.setup.session.SetupSession;

public class SetupInventoryCloseListener implements Listener {

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        Player player = (Player) event.getPlayer();
        SetupSession session = MineKoth.getInstance().getSessionManager().getSession(player);
        if (session == null) {
            return;
        }

        if (session.isDaysSet() && !session.isRewardsSet()) {
            session.setRewards(event.getInventory().getContents());
            player.sendMessage(ChatColor.GREEN + "Rewards set. Please enter commands without / and using %player% for rewards.");
            player.sendMessage(ChatColor.GREEN + "Example: " + ChatColor.BLUE + "give %player% diamond 1");
            player.sendMessage(ChatColor.GREEN + "Type /koth setup finish when done.");
        }
    }

}
