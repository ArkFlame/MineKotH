package com.arkflame.minekoth.utils;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Utility class for KOTH scheduling
 * Handles time interval to schedule conversion
 */
public class KothScheduleUtil {

    /**
     * Processes an input message and generates a schedule if needed
     * 
     * @param message Input message (could be a schedule or time interval)
     * @return Formatted schedule if the input was a time interval, otherwise the original message
     */
    public static String processMessage(String message) {
        if (message == null || message.trim().isEmpty()) {
            return message;
        }
        
        // Check if the message is already a valid schedule
        if (isValidTimes(message)) {
            return message;
        }
        
        // Check if the message is a time interval pattern
        int intervalMinutes = parseTimeInterval(message.trim().toLowerCase());
        
        // If it's a valid interval, generate a schedule
        if (intervalMinutes > 0 && intervalMinutes > 5) {
            return generateSchedule(intervalMinutes);
        }
        
        // If it's neither a valid schedule nor a time interval, return null
        return null;
    }
    
    /**
     * Validates if a string contains valid time formats
     * 
     * @param message String containing times to validate
     * @return True if valid, false otherwise
     */
    public static boolean isValidTimes(String message) {
        if (message == null || message.isEmpty()) {
            return false;
        }

        // Regular expression to match 12-hour (with am/pm), 24-hour time formats, and
        // multiple times separated by commas
        String timePattern = "^(?:(1[0-2]|[1-9])(am|pm)|([01]?[0-9]|2[0-3]):([0-5]?[0-9])|(24:00))(,\\s?(?:(1[0-2]|[1-9])(am|pm)|([01]?[0-9]|2[0-3]):([0-5]?[0-9])|(24:00)))*$";
        return message.toLowerCase().matches(timePattern);
    }
    
    /**
     * Parses the input time interval and converts it to minutes
     * 
     * @param input Normalized user input
     * @return The interval in minutes, or -1 if invalid
     */
    private static int parseTimeInterval(String input) {
        // Pattern for hour formats: 1 hour, 1h, 1hr, etc.
        Pattern hourPattern = Pattern.compile("^(\\d+)\\s*(h|hr|hour|hours)$");
        Matcher hourMatcher = hourPattern.matcher(input);
        
        // Pattern for minute formats: 30 minutes, 30m, 30min, etc.
        Pattern minutePattern = Pattern.compile("^(\\d+)\\s*(m|min|minute|minutes)$");
        Matcher minuteMatcher = minutePattern.matcher(input);
        
        if (hourMatcher.matches()) {
            return Integer.parseInt(hourMatcher.group(1)) * 60;
        } else if (minuteMatcher.matches()) {
            return Integer.parseInt(minuteMatcher.group(1));
        } else {
            return -1;
        }
    }
    
    /**
     * Generates a formatted schedule string based on the interval in minutes
     * 
     * @param intervalMinutes Interval in minutes
     * @return Formatted schedule string
     */
    private static String generateSchedule(int intervalMinutes) {
        List<String> times = new ArrayList<>();
        
        // Generate times for a 24-hour period (00:00 to 24:00)
        for (int minutes = 0; minutes <= 24 * 60; minutes += intervalMinutes) {
            if (minutes == 24 * 60) {
                times.add("24:00");
            } else {
                int hours = minutes / 60;
                int mins = minutes % 60;
                times.add(String.format("%d:%02d", hours, mins));
            }
        }
        
        return String.join(", ", times);
    }
}