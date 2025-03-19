package com.arkflame.minekoth.utils;

import java.time.DayOfWeek;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

import com.arkflame.minekoth.koth.KothTime;

public class Times {
    public static int parseToSeconds(String time) {
        String lowerTime = time.toLowerCase().trim();
        int number = getNumber(lowerTime);
        if (lowerTime.startsWith("s")) {
            return number;
        } else if (lowerTime.startsWith("m")) {
            return number * 60;
        } else if (lowerTime.startsWith("h")) {
            return number * 3600;
        }
        return 0;
    }

    public static int getNumber(String time) {
        try {
            return Integer.parseInt(time.replaceAll("[^0-9]", ""));
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    public static KothTime parseTimeEntry(String timeEntry) {
        int hour, minute = 0;
        boolean isPm = timeEntry.toLowerCase().contains("pm");

        String time = timeEntry.replaceAll("[^0-9:\\s]", "").trim();
        String[] parts = time.split(":");

        if (parts.length == 2) {
            hour = Integer.parseInt(parts[0]);
            minute = Integer.parseInt(parts[1]);
        } else if (parts.length == 1) {
            hour = Integer.parseInt(parts[0]);
        } else {
            return null; // Skip invalid format
        }

        if (isPm && hour < 12) {
            hour += 12;
        } else if (!isPm && hour == 12) {
            hour = 0;
        }

        if (hour < 0 || hour > 23 || minute < 0 || minute > 59) {
            return null; // Skip out-of-range values
        }

        return new KothTime(hour, minute);
    }

    public static List<DayOfWeek> getAllDays() {
        return new ArrayList<>(EnumSet.allOf(DayOfWeek.class));
    }

    public static List<DayOfWeek> parseDayNames(String[] dayNames) {
        List<DayOfWeek> days = new ArrayList<>();
        if (days == null || dayNames.length == 0) {
            return getAllDays();
        }

        for (String dayName : dayNames) {
            if (dayName.equalsIgnoreCase("all")) {
                return getAllDays();
            }
            try {
                DayOfWeek day = DayOfWeek.valueOf(dayName.toUpperCase());
                days.add(day);
            } catch (IllegalArgumentException e) {
                // Ignore invalid day names
            }
        }

        return days;
    }
}
