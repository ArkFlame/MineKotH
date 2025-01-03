package com.arkflame.minekoth.schedule;

import com.arkflame.minekoth.MineKoth;
import com.arkflame.minekoth.koth.Koth;
import java.time.DayOfWeek;
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
}