package com.arkflame.minekoth.schedule;

import com.arkflame.minekoth.MineKoth;
import com.arkflame.minekoth.koth.Koth;
import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;

public class Schedule {
    private final int id;
    private final int kothId;
    private List<DayOfWeek> days;
    private int hour;
    private int minute;

    public Schedule(int id, int kothId, List<DayOfWeek> days, int hour, int minute) {
        this.id = id;
        this.kothId = kothId;
        this.days = days;
        this.hour = hour;
        this.minute = minute;
    }

    public int getId() {
        return id;
    }

    public int getKothId() {
        return kothId;
    }

    public List<DayOfWeek> getDays() {
        return days;
    }

    public void setDays(List<DayOfWeek> days) {
        this.days = days;
    }

    public int getHour() {
        return hour;
    }

    public void setHour(int hour) {
        this.hour = hour;
    }

    public int getMinute() {
        return minute;
    }

    public void setMinute(int minute) {
        this.minute = minute;
    }

    public Koth getKoth() {
        return MineKoth.getInstance().getKothManager().getKothById(kothId);
    }

    public boolean matches(DayOfWeek day, int hour, int minute) {
        return days.contains(day) && this.hour == hour && this.minute == minute;
    }

    @Override
    public String toString() {
        return "Schedule{" +
                "id=" + id +
                ", kothId=" + kothId +
                ", days=" + days +
                ", hour=" + hour +
                ", minute=" + minute +
                '}';
    }

    /**
     * Returns a formatted string representing the time left until the next
     * scheduled event.
     * 
     * @return Formatted time string
     */
    public String getTimeLeftFormatted() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime nextStartTime = calculateNextStartTime(now);

        // Calculate seconds left until the scheduled start time
        long secondsLeft = calculateSecondsLeft(now, nextStartTime);

        return formatTimeLeft(secondsLeft);
    }

    /**
     * Calculates the next start time based on the current time and scheduled days.
     * 
     * @param now Current time
     * @return The next scheduled start time
     */
    private LocalDateTime calculateNextStartTime(LocalDateTime now) {
        return MineKoth.getInstance().getScheduleManager().getNextOccurrence(this);
    }

    /**
     * Calculates seconds left between two LocalDateTime objects.
     * 
     * @param now        Current time
     * @param targetTime Target time
     * @return Seconds left (non-negative)
     */
    private long calculateSecondsLeft(LocalDateTime now, LocalDateTime targetTime) {
        long secondsLeft = now.until(targetTime, ChronoUnit.SECONDS);
        return Math.max(0, secondsLeft);
    }

    /**
     * Formats the seconds left into a human-readable string.
     * 
     * @param secondsLeft Seconds left until the event
     * @return Formatted time string
     */
    private String formatTimeLeft(long secondsLeft) {
        if (secondsLeft <= 0) {
            return "0";
        }

        long days = secondsLeft / 86400;
        long hours = (secondsLeft % 86400) / 3600;
        long minutes = (secondsLeft % 3600) / 60;
        long seconds = secondsLeft % 60;

        return formatTimeComponents(days, hours, minutes, seconds);
    }

    /**
     * Formats time components into a human-readable string.
     * 
     * @param days    Days component
     * @param hours   Hours component
     * @param minutes Minutes component
     * @param seconds Seconds component
     * @return Formatted time string
     */
    private String formatTimeComponents(long days, long hours, long minutes, long seconds) {
        String daysText = MineKoth.getInstance().getConfig().getString("messages.schedules.days", "days");

        if (days > 0) {
            return String.format("%d %s %02d:%02d:%02d", days, daysText, hours, minutes, seconds);
        } else if (hours > 0) {
            return String.format("%02d:%02d:%02d", hours, minutes, seconds);
        } else if (minutes > 0) {
            return String.format("%02d:%02d", minutes, seconds);
        } else {
            return String.format("%d", seconds);
        }
    }

    public List<String> getDayNames() {
        List<String> dayNames = new ArrayList<>(days.size());
        for (DayOfWeek day : days) {
            dayNames.add(day.name());
        }
        return dayNames;
    }
}