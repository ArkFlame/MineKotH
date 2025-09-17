package com.arkflame.minekoth.bossbarapi;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerTeleportEvent;

import com.arkflame.minekoth.bossbarapi.bridge.LegacyWitherBossBar;

/**
 * Handles player events to ensure legacy Wither-based boss bars function correctly.
 * This listener is only registered on 1.8 servers.
 */
class BossBarListener implements Listener {

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        // FIX: Iterate through all active bars and remove the player from each one.
        for (BossBarAPI bar : BossBarManager.getActiveBars()) {
            bar.removePlayer(player);
        }
    }

    @EventHandler
    public void onPlayerTeleport(PlayerTeleportEvent event) {
        Player player = event.getPlayer();
        // After a teleport, immediately update the position of any withers for this player.
        for (BossBarAPI bar : BossBarManager.getActiveBars()) {
            if (bar.getPlayers().contains(player)) {
                ((LegacyWitherBossBar) bar.getBridge()).updatePosition(player);
            }
        }
    }
}