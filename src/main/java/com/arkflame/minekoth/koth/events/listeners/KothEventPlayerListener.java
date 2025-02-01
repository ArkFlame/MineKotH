package com.arkflame.minekoth.koth.events.listeners;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.inventory.ItemStack;

import com.arkflame.minekoth.MineKoth;
import com.arkflame.minekoth.koth.events.KothEvent;
import com.arkflame.minekoth.koth.events.managers.KothEventManager;
import com.arkflame.minekoth.playerdata.PlayerData;
import com.arkflame.minekoth.utils.Materials;

public class KothEventPlayerListener implements Listener {
    private KothEventManager kothEventManager;

    public KothEventPlayerListener(KothEventManager kothEventManager) {
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
                int totalKills = kothEvent.getStats().getPlayerStats(killer.getUniqueId()).getTotalPlayerKills();
                if (totalKills == 1) {
                    killer.getInventory().addItem(new ItemStack(Materials.get("REDSTONE_BLOCK"), 1));
                    killer.sendMessage("You received a reward for your first kill!");
                }
                int killstreak = kothEvent.getStats().getPlayerStats(killer.getUniqueId()).getCurrentKillStreak();
                if (killstreak == 2) {
                    killer.getInventory().addItem(new ItemStack(Materials.get("DIRT"), 1));
                    killer.sendMessage("You received a reward for your " + killstreak + " killstreak!");
                } else if (killstreak == 3) {
                    killer.getInventory().addItem(new ItemStack(Materials.get("STONE"), 1));
                    killer.sendMessage("You received a reward for your " + killstreak + " killstreak!");
                } else if (killstreak >= 4) {
                    killer.getInventory().addItem(new ItemStack(Materials.get("GOLD_INGOT"), 1));
                    killer.sendMessage("You received a reward for your " + killstreak + " killstreak!");
                }
                PlayerData playerData = MineKoth.getInstance().getPlayerDataManager().getIfLoaded(killer.getUniqueId().toString());
                if (playerData != null) {
                    playerData.incrementKillCount(kothEvent.getKoth().getId());
                }
            }
            kothEvent.getStats().addDeath(player.getUniqueId());
            PlayerData playerData = MineKoth.getInstance().getPlayerDataManager().getIfLoaded(player.getUniqueId().toString());
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
                PlayerData playerData = MineKoth.getInstance().getPlayerDataManager().getIfLoaded(event.getDamager().getUniqueId().toString());
                kothEvent.getStats().addDamageDone(event.getDamager().getUniqueId(), (int) event.getDamage());
                if (playerData != null) {
                    playerData.addDamageDealt(kothEvent.getKoth().getId(), (int) event.getDamage());
                }
            }

            if (event.getEntity() instanceof Player) {
                PlayerData playerData = MineKoth.getInstance().getPlayerDataManager().getIfLoaded(event.getEntity().getUniqueId().toString());
                kothEvent.getStats().addDamageReceived(event.getEntity().getUniqueId(), (int) event.getDamage());
                if (playerData != null) {
                    playerData.addDamageReceived(kothEvent.getKoth().getId(), (int) event.getDamage());
                }
            }
        }
    }
}
