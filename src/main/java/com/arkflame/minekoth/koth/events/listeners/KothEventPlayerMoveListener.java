package com.arkflame.minekoth.koth.events.listeners;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerTeleportEvent;

import com.arkflame.minekoth.koth.events.KothEvent;
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
        Player player = event.getEntity();
        KothEvent kothEvent = kothEventManager.getKothEvent();
        if (kothEvent != null) {
            Player killer = player.getKiller();
            if (killer != null && kothEvent.getKoth().isInside(killer.getLocation())) {
                kothEvent.getStats().addPlayerKill(killer.getUniqueId());
            }
        }
        kothEventManager.updatePlayerState(player, player.getLocation(), true);
    }

    @EventHandler(ignoreCancelled = true)
    public void onPlayerTeleport(PlayerTeleportEvent event) {
        kothEventManager.updatePlayerState(event.getPlayer(), event.getTo());
    }
}
