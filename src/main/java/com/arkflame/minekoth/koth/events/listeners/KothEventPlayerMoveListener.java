package com.arkflame.minekoth.koth.events.listeners;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerTeleportEvent;

import com.arkflame.minekoth.koth.events.managers.KothEventManager;

public class KothEventPlayerMoveListener implements Listener {
    private KothEventManager kothEventManager;

    public KothEventPlayerMoveListener(KothEventManager kothEventManager) {
        this.kothEventManager = kothEventManager;
    }

    @EventHandler(ignoreCancelled = true)
    public void onPlayerMove(PlayerMoveEvent event) {
        kothEventManager.updatePlayerState(event.getPlayer(), event.getTo());
    }

    @EventHandler(ignoreCancelled = true)
    public void onPlayerDeath(PlayerDeathEvent event) {
        Bukkit.getServer().broadcastMessage("Player " + event.getEntity().getName() + " died.");
        Player player = event.getEntity();
        kothEventManager.updatePlayerState(player, player.getLocation(), true);
    }

    @EventHandler(ignoreCancelled = true)
    public void onPlayerTeleport(PlayerTeleportEvent event) {
        kothEventManager.updatePlayerState(event.getPlayer(), event.getTo());
    }
}
