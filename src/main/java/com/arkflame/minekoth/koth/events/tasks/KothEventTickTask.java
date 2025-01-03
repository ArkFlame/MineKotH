package com.arkflame.minekoth.koth.events.tasks;

import com.arkflame.minekoth.MineKoth;
import com.arkflame.minekoth.koth.events.KothEvent;
import org.bukkit.scheduler.BukkitRunnable;

public class KothEventTickTask extends BukkitRunnable {
    @Override
    public void run() {
        KothEvent kothEvent = MineKoth.getInstance().
getKothEventManager().getKothEvent();
        if (kothEvent != null) {
            kothEvent.tick();
        }
    }
}
