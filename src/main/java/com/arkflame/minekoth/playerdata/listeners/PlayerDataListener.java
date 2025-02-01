package com.arkflame.minekoth.playerdata.listeners;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import com.arkflame.minekoth.MineKoth;
import com.arkflame.minekoth.utils.FoliaAPI;

public class PlayerDataListener implements Listener {
    @EventHandler(ignoreCancelled = true)
    public void onPlayerJoin(PlayerJoinEvent event) {
        FoliaAPI.runTaskAsync(() -> {
            MineKoth.getInstance().getPlayerDataManager().getAndLoad(event.getPlayer().getUniqueId().toString());
        }, 20L);
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        FoliaAPI.runTaskAsync(() -> {
            MineKoth.getInstance().getPlayerDataManager().save(event.getPlayer().getUniqueId().toString());
        });
    }
}
