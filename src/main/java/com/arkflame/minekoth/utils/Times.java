package com.arkflame.minekoth.utils;

public class Times {
    public static int parseToSeconds(String time) {
        try {
            String lowerTime = time.toLowerCase().trim();
            if (lowerTime.endsWith("min") || lowerTime.endsWith("minutes")) {
                return Integer.parseInt(lowerTime.replaceAll("[^0-9]", "")) * 60;
            } else if (lowerTime.endsWith("h") || lowerTime.endsWith("hours")) {
                return Integer.parseInt(lowerTime.replaceAll("[^0-9]", "")) * 3600;
            } else if (lowerTime.endsWith("s") || lowerTime.endsWith("secs") || lowerTime.endsWith("sec") || lowerTime.endsWith("seconds")) {
                return Integer.parseInt(lowerTime.replaceAll("[^0-9]", ""));
            } else if (lowerTime.endsWith("m")) {
                return Integer.parseInt(lowerTime.replaceAll("[^0-9]", "")) * 60;
            }
        } catch (NumberFormatException e) {
            return 0;
        }
        return 0;
    }
}
