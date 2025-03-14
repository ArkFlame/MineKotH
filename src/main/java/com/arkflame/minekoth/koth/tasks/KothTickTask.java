package com.arkflame.minekoth.koth.tasks;

import com.arkflame.minekoth.MineKoth;

public class KothTickTask implements Runnable {
    @Override
    public void run() {
        MineKoth.getInstance().getKothManager().tick();
    }
}
