package com.arkflame.minekoth.schedule.tasks;

import com.arkflame.minekoth.MineKoTH;
import com.arkflame.minekoth.koth.KoTH;
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
            // Get the koth by schedule
            KoTH koTH = schedule.getKoTH();
            // Start the koth event
            MineKoTH.getInstance().getKoTHEventManager().start(koTH);
        }
    }
}
