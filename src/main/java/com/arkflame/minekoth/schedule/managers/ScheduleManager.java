package com.arkflame.minekoth.schedule.managers;

import com.arkflame.minekoth.schedule.Schedule;
import com.arkflame.minekoth.utils.Times;
import com.arkflame.minekoth.koth.Koth;
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
        schedulesById.put(schedule.getId(), schedule);
        updateScheduleMapping(schedule);
        calculateNextKoth();
    }

    public void removeSchedule(int id) {
        Schedule schedule = schedulesById.remove(id);
        if (schedule != null) {
            removeScheduleMapping(schedule);
            calculateNextKoth();
        }
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
        }
    }

    public Schedule scheduleKoth(Koth koth) {
        removeSchedulesByKoth(koth.getId());
        String times = koth.getTimes();
        String[] timeEntries = times.split(" ");
        Set<DayOfWeek> days = EnumSet.allOf(DayOfWeek.class); // TODO: Support specific days in the future

        for (String timeEntry : timeEntries) {
            try {
                KothTime kothTime = Times.parseTimeEntry(timeEntry);
                if (kothTime == null) {
                    continue; // Skip invalid time entries
                }
                Schedule schedule = new Schedule(
                        generateUniqueId(),
                        koth.getId(),
                        days,
                        kothTime.getHour(),
                        kothTime.getMinute()
                );

                addSchedule(schedule);
                return schedule;
            } catch (NumberFormatException e) {
                // Ignore invalid number formats
            }
        }

        return null;
    }

    public Schedule scheduleKoth(int kothId, int hour, int minute, List<String> dayNames) {
        Set<DayOfWeek> days = EnumSet.noneOf(DayOfWeek.class);

        for (String dayName : dayNames) {
            try {
                DayOfWeek day = DayOfWeek.valueOf(dayName.toUpperCase());
                days.add(day);
            } catch (IllegalArgumentException e) {
                // Ignore invalid day names
            }
        }

        Schedule schedule = new Schedule(
                generateUniqueId(),
                kothId,
                days,
                hour,
                minute
        );

        addSchedule(schedule);

        return schedule;
    }

    private int generateUniqueId() {
        return schedulesById.keySet().stream().max(Integer::compare).orElse(0) + 1;
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
}