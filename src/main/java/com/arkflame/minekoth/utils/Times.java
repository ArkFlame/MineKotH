package com.arkflame.minekoth.utils;

import java.time.DayOfWeek;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

import com.arkflame.minekoth.koth.KothTime;

public class Times {
    /**
     * Parses a time string into seconds.
     * 
     * Supported formats:
     * - Pure number (e.g. "60"): directly interpreted as seconds
     * - Number with s/sec/second/seconds suffix (e.g. "60s"): interpreted as
     * seconds
     * - Number with m/min/minute/minutes suffix (e.g. "5m"): interpreted as minutes
     * (converted to seconds)
     * - Number with h/hr/hour/hours suffix (e.g. "1h"): interpreted as hours
     * (converted to seconds)
     * 
     * @param input The time string to parse
     * @return The time in seconds, or 0 if parsing fails
     */
    public static int parseToSeconds(String input) {
        if (input == null || input.isEmpty()) {
            return 0;
        }

        String time = input.toLowerCase().trim();

        // Try to parse as pure number (seconds)
        if (time.matches("^\\d+$")) {
            try {
                return Integer.parseInt(time);
            } catch (NumberFormatException e) {
                return 0;
            }
        }

        // Extract number and unit
        if (time.matches("^\\d+\\s*[smh].*$")) {
            // Extract the number part
            int number;
            try {
                number = Integer.parseInt(time.replaceAll("\\D.*$", ""));
            } catch (NumberFormatException e) {
                return 0;
            }

            // Get first character of the unit part (after removing digits and spaces)
            char unit = time.replaceAll("^\\d+\\s*", "").charAt(0);

            // Convert to seconds based on unit
            switch (unit) {
                case 's':
                    return number;
                case 'm':
                    return number * 60;
                case 'h':
                    return number * 3600;
                default:
                    return 0;
            }
        }

        return 0;
    }

    /**
     * Converts seconds to a formatted time string.
     * 
     * Examples:
     * - 30 seconds → "30 Seconds"
     * - 60 seconds → "1 Minute"
     * - 90 seconds → "1 Minute 30 Seconds"
     * - 3600 seconds → "1 Hour"
     * - 3630 seconds → "1 Hour 30 Seconds"
     * - 3690 seconds → "1 Hour 1 Minute 30 Seconds"
     * 
     * @param seconds Total time in seconds
     * @return Formatted time string
     */
    public static String formatSeconds(int seconds) {
        if (seconds <= 0) {
            return "0 Seconds";
        }

        int hours = seconds / 3600;
        int minutes = (seconds % 3600) / 60;
        int remainingSeconds = seconds % 60;

        StringBuilder result = new StringBuilder();

        if (hours > 0) {
            result.append(hours).append(hours == 1 ? " Hour" : " Hours");
        }

        if (minutes > 0) {
            if (result.length() > 0) {
                result.append(" ");
            }
            result.append(minutes).append(minutes == 1 ? " Minute" : " Minutes");
        }

        if (remainingSeconds > 0) {
            if (result.length() > 0) {
                result.append(" ");
            }
            result.append(remainingSeconds).append(remainingSeconds == 1 ? " Second" : " Seconds");
        }

        return result.toString();
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
