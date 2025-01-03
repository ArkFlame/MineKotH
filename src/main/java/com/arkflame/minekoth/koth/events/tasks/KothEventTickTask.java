package com.arkflame.minekoth.koth.events.tasks;

import com.arkflame.minekoth.MineKoth;

import org.bukkit.scheduler.BukkitRunnable;

public class KothEventTickTask extends BukkitRunnable {
    @Override
    public void run() {
        MineKoth.getInstance().getKothEventManager().tick();
    }
}
