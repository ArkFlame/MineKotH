package com.arkflame.minekoth.koth.events.listeners;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerTeleportEvent;

import com.arkflame.minekoth.MineKoth;
import com.arkflame.minekoth.koth.events.KothEvent;
import com.arkflame.minekoth.koth.events.config.KillStreaksConfig;
import com.arkflame.minekoth.koth.events.managers.KothEventManager;
import com.arkflame.minekoth.lang.Lang;
import com.arkflame.minekoth.playerdata.PlayerData;

public class KothEventPlayerListener implements Listener {
    private KothEventManager kothEventManager;
    private KillStreaksConfig killStreaksConfig;

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
                Lang lang = MineKoth.getInstance().getLangManager().getLang(killer);

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

    @EventHandler(ignoreCancelled = true)
    public void onPlayerTeleport(PlayerTeleportEvent event) {
        kothEventManager.updatePlayerState(event.getPlayer(), event.getTo());
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

    @EventHandler(ignoreCancelled = true)
    public void onPlayerQuit(PlayerQuitEvent event) {
        kothEventManager.updatePlayerState(event.getPlayer(), event.getPlayer().getLocation(), true);
    }
}
