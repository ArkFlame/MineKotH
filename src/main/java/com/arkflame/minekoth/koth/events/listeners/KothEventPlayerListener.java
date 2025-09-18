package com.arkflame.minekoth.koth.events.listeners;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;

import com.arkflame.minekoth.MineKoth;
import com.arkflame.minekoth.koth.events.KothEvent;
import com.arkflame.minekoth.koth.events.config.KillStreaksConfig;
import com.arkflame.minekoth.koth.events.managers.KothEventManager;
import com.arkflame.minekoth.lang.Lang;
import com.arkflame.minekoth.playerdata.PlayerData;

public class KothEventPlayerListener implements Listener {
    private KothEventManager kothEventManager;
    private KillStreaksConfig killStreaksConfig;
    
    // A small squared distance to check for anti-cheat compatibility. 3*3=9
    private static final double ANTI_CHEAT_TELEPORT_DISTANCE_SQUARED = 9.0;

    public KothEventPlayerListener(KothEventManager kothEventManager) {
        this.kothEventManager = kothEventManager;
        killStreaksConfig = new KillStreaksConfig(MineKoth.getInstance().getConfig());
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
                int totalKills = kothEvent.getStats().getPlayerStats(killer.getUniqueId()).getTotalPlayerKills();
                int killstreak = kothEvent.getStats().getPlayerStats(killer.getUniqueId()).getCurrentKillStreak();

                if (killStreaksConfig.isKillstreaksEnabled()) {
                    if (totalKills == 1) {
                        killStreaksConfig.giveRewards(killer, 1);
                    }

                    if (killstreak > 1) {
                        killStreaksConfig.giveRewards(killer, killstreak);
                    }
                }

                PlayerData playerData = MineKoth.getInstance().getPlayerDataManager()
                        .getIfLoaded(killer.getUniqueId().toString());
                if (playerData != null) {
                    playerData.incrementKillCount(kothEvent.getKoth().getId());
                }
            }
            kothEvent.getStats().addDeath(player.getUniqueId());
            PlayerData playerData = MineKoth.getInstance().getPlayerDataManager()
                    .getIfLoaded(player.getUniqueId().toString());
            if (playerData != null) {
                playerData.incrementDeathCount(kothEvent.getKoth().getId());
            }
        }
        kothEventManager.updatePlayerState(player, player.getLocation(), true);
    }

    /**
     * UPDATED: Prevents players without permission from teleporting into an active KoTH.
     * Allows Ender Pearl teleports and small anti-cheat adjustments.
     */
    @EventHandler(ignoreCancelled = true)
    public void onPlayerTeleport(PlayerTeleportEvent event) {
        Player player = event.getPlayer();
        KothEvent kothEvent = kothEventManager.getKothEvent();

        // Check if there is an active KoTH event
        if (kothEvent != null) {
            // Check if the destination is inside the KoTH area
            if (kothEvent.getKoth().isInside(event.getTo())) {
                // If the player does not have bypass permission, check the teleport cause
                if (!player.hasPermission("minekoth.bypass.teleport")) {
                    TeleportCause cause = event.getCause();

                    // Allow Ender Pearls
                    if (cause == TeleportCause.ENDER_PEARL) {
                        // This is allowed, proceed to update state
                    }
                    // Allow small teleports to prevent issues with anti-cheats
                    else if (event.getFrom().getWorld().equals(event.getTo().getWorld()) && event.getFrom().distanceSquared(event.getTo()) < ANTI_CHEAT_TELEPORT_DISTANCE_SQUARED) {
                        // This is a minor teleport, likely from an anti-cheat, allow it
                    }
                    // Block all other causes (COMMAND, PLUGIN, etc.)
                    else {
                        Lang lang = MineKoth.getInstance().getLangManager().getLang(player);
                        // Assumes a lang key like 'TELEPORT_BLOCKED_IN_KOTH' exists
                        player.sendMessage(lang.getMessage("TELEPORT_BLOCKED_IN_KOTH"));
                        event.setCancelled(true);
                        // Do not update player state if the event is cancelled
                        return;
                    }
                }
            }
        }

        // If the teleport was not cancelled, update the player's state
        kothEventManager.updatePlayerState(player, event.getTo());
    }

    @EventHandler(ignoreCancelled = true)
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
        KothEvent kothEvent = kothEventManager.getKothEvent();
        if (kothEvent != null) {
            if (event.getDamager() instanceof Player) {
                Player damager = (Player) event.getDamager();
                PlayerData playerData = MineKoth.getInstance().getPlayerDataManager()
                        .getIfLoaded(damager.getUniqueId().toString());
                kothEvent.getStats().addDamageDone(damager.getUniqueId(), (int) event.getDamage());
                if (playerData != null) {
                    playerData.addDamageDealt(kothEvent.getKoth().getId(), (int) event.getDamage());
                }
            }

            if (event.getEntity() instanceof Player) {
                Player damaged = (Player) event.getEntity();
                PlayerData playerData = MineKoth.getInstance().getPlayerDataManager()
                        .getIfLoaded(damaged.getUniqueId().toString());
                kothEvent.getStats().addDamageReceived(damaged.getUniqueId(), (int) event.getDamage());
                if (playerData != null) {
                    playerData.addDamageReceived(kothEvent.getKoth().getId(), (int) event.getDamage());
                }
            }
        }
    }
    
    /**
     * NEW: Prevents players without permission from using commands inside an active KoTH.
     */
    @EventHandler(ignoreCancelled = true)
    public void onPlayerCommand(PlayerCommandPreprocessEvent event) {
        Player player = event.getPlayer();
        
        // Allow players with bypass permission
        if (player.hasPermission("minekoth.bypass.command")) {
            return;
        }

        KothEvent kothEvent = kothEventManager.getKothEvent();

        // Check if there is an active KoTH event and if the player is inside
        if (kothEvent != null && kothEvent.getKoth().isInside(player.getLocation())) {
            Lang lang = MineKoth.getInstance().getLangManager().getLang(player);
            // Assumes a lang key like 'COMMANDS_BLOCKED_IN_KOTH' exists
            player.sendMessage(lang.getMessage("COMMANDS_BLOCKED_IN_KOTH"));
            event.setCancelled(true);
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onPlayerQuit(PlayerQuitEvent event) {
        kothEventManager.updatePlayerState(event.getPlayer(), event.getPlayer().getLocation(), true);
    }
}