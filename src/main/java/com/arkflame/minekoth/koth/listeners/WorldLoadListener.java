package com.arkflame.minekoth.koth.listeners;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.world.WorldLoadEvent;

import com.arkflame.minekoth.koth.Koth;
import com.arkflame.minekoth.koth.managers.KothManager;

public class WorldLoadListener implements Listener {
    private KothManager kothManager;

    public WorldLoadListener(KothManager kothManager) {
        this.kothManager = kothManager;
    }

    @EventHandler
    public void onWorldLoad(WorldLoadEvent event) {
        // Spawn holograms
        for (Koth koth : kothManager.getAllkoths().values()) {
            if (koth.getWorld() == event.getWorld()) {
                koth.spawnHologram();
            }
        }
    }
}
