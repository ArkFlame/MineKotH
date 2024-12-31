package com.arkflame.minekoth.schedule.tasks;

import com.arkflame.minekoth.MineKoTH;
import com.arkflame.minekoth.schedule.Schedule;

import org.bukkit.scheduler.BukkitRunnable;
import java.time.LocalDateTime;
import java.util.List;

public class ScheduleRunnerTask extends BukkitRunnable {
    @Override
    public void run() {
        LocalDateTime now = LocalDateTime.now();
        List<Schedule> schedules = MineKoTH.getInstance().getScheduleManager()
                .getSchedulesByTime(now.getDayOfWeek(), now.getHour(), now.getMinute());

        for (Schedule schedule : schedules) {
            //schedule.getKoTH().start();
            // TODO: Effectively start the koth
        }
    }
}
