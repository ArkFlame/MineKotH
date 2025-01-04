package com.arkflame.minekoth.utils;

import com.arkflame.minekoth.koth.KothTime;

public class Times {
    public static int parseToSeconds(String time) {
        try {
            String lowerTime = time.toLowerCase().trim();
            if (lowerTime.endsWith("min") || lowerTime.endsWith("minutes")) {
                return Integer.parseInt(lowerTime.replaceAll("[^0-9]", "")) * 60;
            } else if (lowerTime.endsWith("h") || lowerTime.endsWith("hours")) {
                return Integer.parseInt(lowerTime.replaceAll("[^0-9]", "")) * 3600;
            } else if (lowerTime.endsWith("s") || lowerTime.endsWith("secs") || lowerTime.endsWith("sec")
                    || lowerTime.endsWith("seconds")) {
                return Integer.parseInt(lowerTime.replaceAll("[^0-9]", ""));
            } else if (lowerTime.endsWith("m")) {
                return Integer.parseInt(lowerTime.replaceAll("[^0-9]", "")) * 60;
            }
        } catch (NumberFormatException e) {
            return 0;
        }
        return 0;
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
}
