package com.arkflame.minekoth.schedule.managers;

import com.arkflame.minekoth.schedule.Schedule;
import com.arkflame.minekoth.utils.Times;
import com.arkflame.minekoth.MineKoth;
import com.arkflame.minekoth.koth.KothTime;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class ScheduleManager {
    private final Map<Integer, Schedule> schedulesById = new ConcurrentHashMap<>();
    private final Map<String, List<Schedule>> schedulesByTime = new ConcurrentHashMap<>();
    private Schedule nextSchedule;

    public void addSchedule(Schedule schedule) {
        // Ensure we are removing any existing schedule with the same ID
        removeSchedule(schedule.getId());
        schedulesById.put(schedule.getId(), schedule);
        updateScheduleMapping(schedule);
        calculateNextKoth();
    }

    public boolean removeSchedule(int id) {
        Schedule schedule = schedulesById.remove(id);
        if (schedule != null) {
            removeScheduleMapping(schedule);
            calculateNextKoth();
            return true;
        }
        return false;
    }

    public void removeSchedulesByKoth(int kothId) {
        List<Integer> schedulesToRemove = new ArrayList<>();

        for (Schedule schedule : schedulesById.values()) {
            if (schedule.getKothId() == kothId) {
                schedulesToRemove.add(schedule.getId());
            }
        }

        for (int id : schedulesToRemove) {
            removeSchedule(id);
            MineKoth.getInstance().getScheduleLoader().delete(id);
        }
    }

    public Schedule scheduleKoth(int kothId, int hour, int minute, String ...dayNames) {
        List<DayOfWeek> days = dayNames.length > 0 ? Times.parseDayNames(dayNames) : Times.getAllDays();
        if (days.isEmpty()) return null;

        // Generate a unique ID for the new schedule
        int uniqueId = generateUniqueId();
        
        Schedule schedule = new Schedule(
                uniqueId,
                kothId,
                days,
                hour,
                minute
        );

        addSchedule(schedule);
        MineKoth.getInstance().getScheduleLoader().save(schedule);

        return schedule;
    }

    public void scheduleKoth(int kothId, String times, String ...dayNames) {
        String[] timeEntries = times.split(" ");

        for (String timeEntry : timeEntries) {
            try {
                KothTime kothTime = Times.parseTimeEntry(timeEntry);
                if (kothTime == null) {
                    continue; // Skip invalid time entries
                }
                scheduleKoth(kothId, kothTime.getHour(), kothTime.getMinute(), dayNames);
            } catch (NumberFormatException e) {
                // Ignore invalid number formats
            }
        }
    }

    private int generateUniqueId() {
        // Generate a unique ID that is not currently used
        int id = 1; // Start from 1 or any other base value
        while (schedulesById.containsKey(id)) {
            id++;
        }
        return id;
    }

    public Schedule getScheduleById(int id) {
        return schedulesById.get(id);
    }

    public List<Schedule> getSchedulesByTime(DayOfWeek day, int hour, int minute) {
        return schedulesByTime.getOrDefault(formatKey(day, hour, minute), Collections.emptyList());
    }

    private void updateScheduleMapping(Schedule schedule) {
        for (DayOfWeek day : schedule.getDays()) {
            String key = formatKey(day, schedule.getHour(), schedule.getMinute());
            schedulesByTime.computeIfAbsent(key, k -> new ArrayList<>()).add(schedule);
        }
    }

    private void removeScheduleMapping(Schedule schedule) {
        for (DayOfWeek day : schedule.getDays()) {
            String key = formatKey(day, schedule.getHour(), schedule.getMinute());
            List<Schedule> schedules = schedulesByTime.get(key);
            if (schedules != null) {
                schedules.remove(schedule);
                if (schedules.isEmpty()) {
                    schedulesByTime.remove(key);
                }
            }
        }
    }

    private String formatKey(DayOfWeek day, int hour, int minute) {
        return day.name() + ":" + hour + ":" + minute;
    }

    public Schedule getNextSchedule() {
        return nextSchedule;
    }

    public void calculateNextKoth() {
        LocalDateTime now = LocalDateTime.now();
        Schedule nearest = null;
        long nearestDelta = Long.MAX_VALUE;

        for (Schedule schedule : schedulesById.values()) {
            for (DayOfWeek day : schedule.getDays()) {
                LocalDateTime scheduleTime = now.with(day)
                        .withHour(schedule.getHour())
                        .withMinute(schedule.getMinute())
                        .withSecond(0)
                        .withNano(0);

                if (scheduleTime.isBefore(now)) {
                    scheduleTime = scheduleTime.plusWeeks(1);
                }

                long delta = scheduleTime.toEpochSecond(java.time.ZoneOffset.UTC) - now.toEpochSecond(java.time.ZoneOffset.UTC);

                if (delta < nearestDelta) {
                    nearest = schedule;
                    nearestDelta = delta;
                }
            }
        }
        
        nextSchedule = nearest;
    }

    public LocalDateTime getNextOccurrence(Schedule schedule) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime nextOccurrence = null;

        for (DayOfWeek day : schedule.getDays()) {
            LocalDateTime occurrence = now.with(day)
                    .withHour(schedule.getHour())
                    .withMinute(schedule.getMinute())
                    .withSecond(0)
                    .withNano(0);

            if (occurrence.isBefore(now)) {
                occurrence = occurrence.plusWeeks(1);
            }

            if (nextOccurrence == null || occurrence.isBefore(nextOccurrence)) {
                nextOccurrence = occurrence;
            }
        }

        return nextOccurrence;
    }

    public List<Schedule> getAllSchedules() {
        return new ArrayList<>(schedulesById.values());
    }

    public Schedule[] getSchedulesByKoth(int id) {
        return schedulesById.values().stream()
                .filter(schedule -> schedule.getKothId() == id)
                .toArray(Schedule[]::new);
    }
}
