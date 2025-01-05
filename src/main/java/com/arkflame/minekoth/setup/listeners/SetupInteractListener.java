package com.arkflame.minekoth.setup.listeners;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;

import com.arkflame.minekoth.MineKoth;
import com.arkflame.minekoth.koth.Position;
import com.arkflame.minekoth.setup.session.SetupSession;
import com.arkflame.minekoth.utils.Locations;

public class SetupInteractListener implements Listener {

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        SetupSession session = MineKoth.getInstance().getSessionManager().getSession(player);
        if (session == null) {
            return;
        }

        event.setCancelled(true);

        if (!session.isNameSet()) {
            player.sendMessage(ChatColor.RED + "You must set the name of the koth first.");
            return;
        }

        if (!session.isFirstPositionSet()) {
            Location loc = event.getClickedBlock().getLocation();
            session.setFirstPosition(new Position(loc.getX(), loc.getY(), loc.getZ()));
            player.sendMessage(ChatColor.GREEN + "Position 1 set at: " + ChatColor.AQUA + Locations.toString(loc));
            return;
        }

        if (!session.isSecondPositionSet()) {
            Location loc = event.getClickedBlock().getLocation();
            session.setSecondPosition(new Position(loc.getX(), loc.getY(), loc.getZ()));
            player.sendMessage(ChatColor.GREEN + "Position 2 set at: " + ChatColor.AQUA + Locations.toString(loc));
            player.sendMessage(ChatColor.GREEN + "Enter the times to run the koth (e.g., 8pm 9pm 10pm).");
            return;
        }
    }

}
