package com.arkflame.minekoth.koth.events.tasks;

import com.arkflame.minekoth.MineKoth;

public class KothEventTickTask implements Runnable {
    @Override
    public void run() {
        MineKoth.getInstance().getKothEventManager().tick();
    }
}
