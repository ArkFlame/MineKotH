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
    private Set<DayOfWeek> days;
    private int hour;
    private int minute;

    public Schedule(int id, int kothId, Set<DayOfWeek> days, int hour, int minute) {
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

    public Set<DayOfWeek> getDays() {
        return days;
    }

    public void setDays(Set<DayOfWeek> days) {
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

    public String getTimeLeftFormatted() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime startTime = now.withHour(getHour()).withMinute(getMinute()).withSecond(0);
    
        // Find the next scheduled day
        DayOfWeek currentDay = now.getDayOfWeek();
        DayOfWeek nextScheduledDay = null;
        for (DayOfWeek day : days) {
            if (day.getValue() >= currentDay.getValue()) {
                nextScheduledDay = day;
                break;
            }
        }
    
        // If no scheduled day is found in the current week, pick the first one from the next week
        if (nextScheduledDay == null) {
            nextScheduledDay = days.iterator().next();
            startTime = startTime.plusWeeks(1);
        } else if (nextScheduledDay != currentDay) {
            startTime = startTime.plusDays(nextScheduledDay.getValue() - currentDay.getValue());
        }
    
        // Calculate the correct day of the month for the next scheduled day
        if (startTime.isBefore(now)) {
            startTime = startTime.plusWeeks(1);
        }
    
        // Calculate seconds left until the scheduled start time
        long secondsLeft = now.until(startTime, ChronoUnit.SECONDS);
    
        if (secondsLeft < 0) {
            return "0";
        }
    
        long days = secondsLeft / 86400;
        long hours = (secondsLeft % 86400) / 3600;
        long minutes = (secondsLeft % 3600) / 60;
        long seconds = secondsLeft % 60;
    
        if (days > 0) {
            return String.format("%d " + MineKoth.getInstance().getLangManager().getLang(null).getMessage("messages.days") + " %02d:%02d:%02d", days, hours, minutes, seconds);
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