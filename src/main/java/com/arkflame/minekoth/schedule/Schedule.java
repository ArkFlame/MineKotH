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
        // Initialize start time with the scheduled hour and minute
        LocalDateTime startTime = now.withHour(getHour()).withMinute(getMinute()).withSecond(0);

        // Find the next scheduled day
        DayOfWeek currentDay = now.getDayOfWeek();
        DayOfWeek nextScheduledDay = findNextScheduledDay(currentDay);

        // Adjust the start time based on the next scheduled day
        startTime = adjustStartTimeForDay(startTime, currentDay, nextScheduledDay);

        // If the calculated time is in the past, move to the next occurrence
        if (startTime.isBefore(now)) {
            startTime = startTime.plusWeeks(1);
        }

        return startTime;
    }

    /**
     * Finds the next scheduled day from the current day.
     * 
     * @param currentDay The current day of the week
     * @return The next scheduled day of the week
     */
    private DayOfWeek findNextScheduledDay(DayOfWeek currentDay) {
        return days.stream()
                .filter(day -> day.getValue() >= currentDay.getValue())
                .min(Comparator.comparingInt(DayOfWeek::getValue))
                .orElse(days.stream()
                        .min(Comparator.comparingInt(DayOfWeek::getValue))
                        .orElse(null));
    }

    /**
     * Adjusts the start time based on the current and next scheduled day.
     * 
     * @param startTime        Base start time with hour and minute set
     * @param currentDay       Current day of the week
     * @param nextScheduledDay Next scheduled day of the week
     * @return Adjusted start time
     */
    private LocalDateTime adjustStartTimeForDay(LocalDateTime startTime, DayOfWeek currentDay,
            DayOfWeek nextScheduledDay) {
        if (nextScheduledDay == null) {
            // If no day is scheduled, return a time far in the future
            return startTime.plusYears(1000);
        }

        // If next scheduled day is in the next week
        if (nextScheduledDay.getValue() < currentDay.getValue()) {
            return startTime.plusDays(7 - (currentDay.getValue() - nextScheduledDay.getValue()));
        }

        // If next scheduled day is later this week
        return startTime.plusDays(nextScheduledDay.getValue() - currentDay.getValue());
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