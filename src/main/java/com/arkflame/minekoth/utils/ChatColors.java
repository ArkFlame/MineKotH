package com.arkflame.minekoth.utils;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

public class ChatColors {
    public static final char COLOR_CHAR = '\u00A7';
    // Pattern to match hex colors in format <#FFFFFF>
    public static final Pattern HEX_PATTERN = Pattern.compile("<#([A-Fa-f0-9]{6})>");
    // Pattern to match color names like <red>
    public static final Pattern COLOR_NAME_PATTERN = Pattern.compile("<(\\w+)>");
    // Pattern to match gradient tags
    public static final Pattern GRADIENT_PATTERN = Pattern.compile("<gradient:(#[A-Fa-f0-9]{6})>(.*?)</gradient:(#[A-Fa-f0-9]{6})>");
    // Pattern to match rainbow tags
    public static final Pattern RAINBOW_PATTERN = Pattern.compile("<rainbow>(.*?)</rainbow>");

    public static String color(String text) {
        if (text == null) {
            return null;
        }
        text = processGradients(text);
        text = processRainbow(text);
        text = processHexColors(text);
        text = processColorNames(text);
        return ChatColor.translateAlternateColorCodes('&', text);
    }

    public static List<String> color(List<String> text) {
        for (int i = 0; i < text.size(); i++) {
            text.set(i, color(text.get(i)));
        }
        return text;
    }
    
    public static void sendMessage(Player player, String text) {
        player.sendMessage(color(text));
    }

    public static String processHexColors(String message) {
        Matcher matcher = HEX_PATTERN.matcher(message);
        StringBuffer buffer = new StringBuffer();
        while (matcher.find()) {
            String hexColor = matcher.group(1);
            StringBuilder replacement = new StringBuilder(COLOR_CHAR + "x");
            for (char c : hexColor.toCharArray()) {
                replacement.append(COLOR_CHAR).append(c);
            }
            matcher.appendReplacement(buffer, replacement.toString());
        }
        matcher.appendTail(buffer);
        return buffer.toString();
    }

    public static String processColorNames(String message) {
        Matcher matcher = COLOR_NAME_PATTERN.matcher(message);
        StringBuffer buffer = new StringBuffer();
        while (matcher.find()) {
            String colorName = matcher.group(1).toUpperCase();
            ChatColor chatColor;
            try {
                chatColor = ChatColor.valueOf(colorName);
            } catch (IllegalArgumentException e) {
                chatColor = null;
            }
            String replacement = chatColor != null ? chatColor.toString() : matcher.group(0);
            matcher.appendReplacement(buffer, replacement);
        }
        matcher.appendTail(buffer);
        return buffer.toString();
    }

    public static String processRainbow(String message) {
        Matcher matcher = RAINBOW_PATTERN.matcher(message);
        StringBuffer buffer = new StringBuffer();
        while (matcher.find()) {
            String text = matcher.group(1);
            String rainbowText = applyRainbow(text);
            matcher.appendReplacement(buffer, rainbowText);
        }
        matcher.appendTail(buffer);
        return buffer.toString();
    }

    public static String processGradients(String message) {
        Matcher matcher = GRADIENT_PATTERN.matcher(message);
        StringBuffer buffer = new StringBuffer();
        while (matcher.find()) {
            String startColor = matcher.group(1);
            String text = matcher.group(2);
            String endColor = matcher.group(3);

            String gradientText = applyGradient(text, startColor, endColor);
            matcher.appendReplacement(buffer, gradientText);
        }
        matcher.appendTail(buffer);
        return buffer.toString();
    }

    private static String applyRainbow(String text) {
        ChatColor[] rainbowColors = {
            ChatColor.RED,
            ChatColor.GOLD,
            ChatColor.YELLOW,
            ChatColor.GREEN,
            ChatColor.AQUA,
            ChatColor.BLUE,
            ChatColor.LIGHT_PURPLE
        };
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < text.length(); i++) {
            ChatColor color = rainbowColors[i % rainbowColors.length];
            builder.append(color).append(text.charAt(i));
        }
        return builder.toString();
    }

    private static String applyGradient(String text, String startColor, String endColor) {
        int startRed = Integer.parseInt(startColor.substring(1, 3), 16);
        int startGreen = Integer.parseInt(startColor.substring(3, 5), 16);
        int startBlue = Integer.parseInt(startColor.substring(5, 7), 16);

        int endRed = Integer.parseInt(endColor.substring(1, 3), 16);
        int endGreen = Integer.parseInt(endColor.substring(3, 5), 16);
        int endBlue = Integer.parseInt(endColor.substring(5, 7), 16);

        int length = text.length();
        StringBuilder builder = new StringBuilder();

        for (int i = 0; i < length; i++) {
            double ratio = (double) i / Math.max(length - 1, 1);
            int red = (int) (startRed + (endRed - startRed) * ratio);
            int green = (int) (startGreen + (endGreen - startGreen) * ratio);
            int blue = (int) (startBlue + (endBlue - startBlue) * ratio);

            String hexColor = String.format("#%02X%02X%02X", red, green, blue);
            StringBuilder hexCode = new StringBuilder(COLOR_CHAR + "x");
            for (char c : hexColor.substring(1).toCharArray()) {
                hexCode.append(COLOR_CHAR).append(c);
            }
            builder.append(hexCode).append(text.charAt(i));
        }
        return builder.toString();
    }
}
