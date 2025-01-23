package com.arkflame.minekoth.schedule.tasks;

import com.arkflame.minekoth.MineKoth;
import com.arkflame.minekoth.koth.Koth;
import com.arkflame.minekoth.schedule.Schedule;
import com.arkflame.minekoth.utils.Sounds;
import com.arkflame.minekoth.utils.Titles;

import org.bukkit.scheduler.BukkitRunnable;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.List;

public class ScheduleRunnerTask extends BukkitRunnable {

    private final List<Integer> countdownIntervals = Arrays.asList(60, 30, 15, 10, 5, 4, 3, 2, 1);

    @Override
    public void run() {
        if (MineKoth.getInstance().getKothEventManager().getKothEvent() != null) {
            return;
        }

        Schedule schedule = MineKoth.getInstance().getScheduleManager().getNextSchedule();

        if (schedule == null) {
            return; // No schedules available
        }

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime startTime = now.withHour(schedule.getHour()).withMinute(schedule.getMinute()).withSecond(0);

        if (startTime.isBefore(now)) {
            MineKoth.getInstance().getScheduleManager().calculateNextKoth();
            return; // The scheduled time has already passed
        }

        // Calculate seconds left until the scheduled start time
        long secondsLeft = now.until(startTime, ChronoUnit.SECONDS);

        if (secondsLeft < 0) {
            MineKoth.getInstance().getScheduleManager().calculateNextKoth();
            return; // The scheduled time has already passed
        }

        Koth koth = schedule.getKoth();

        if (countdownIntervals.contains((int) secondsLeft)) {
            Titles.sendTitle(
                    "&e" + secondsLeft,
                    "&a" + "koth " + koth.getName() + " starting",
                    10, 20, 10);
            Sounds.play(1.0f, 1.0f, "CLICK");
        }

        if (secondsLeft == 0) {
            MineKoth.getInstance().getKothEventManager().start(koth);
        }
    }
}
