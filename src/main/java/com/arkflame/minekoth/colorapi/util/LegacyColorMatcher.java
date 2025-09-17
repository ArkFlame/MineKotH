package com.arkflame.minekoth.colorapi.util;

import net.md_5.bungee.api.ChatColor;
import java.awt.Color;
import java.util.HashMap;
import java.util.Map;

/**
 * A utility to find the closest legacy ChatColor to a modern RGB color.
 * Correctly treats ChatColor as a class, not an enum.
 */
public final class LegacyColorMatcher {
    // We use a standard HashMap since ChatColor is not an enum.
    public static final Map<ChatColor, Color> LEGACY_COLOR_MAP = new HashMap<>();

    static {
        LEGACY_COLOR_MAP.put(ChatColor.BLACK, new Color(0, 0, 0));
        LEGACY_COLOR_MAP.put(ChatColor.DARK_BLUE, new Color(0, 0, 170));
        LEGACY_COLOR_MAP.put(ChatColor.DARK_GREEN, new Color(0, 170, 0));
        LEGACY_COLOR_MAP.put(ChatColor.DARK_AQUA, new Color(0, 170, 170));
        LEGACY_COLOR_MAP.put(ChatColor.DARK_RED, new Color(170, 0, 0));
        LEGACY_COLOR_MAP.put(ChatColor.DARK_PURPLE, new Color(170, 0, 170));
        LEGACY_COLOR_MAP.put(ChatColor.GOLD, new Color(255, 170, 0));
        LEGACY_COLOR_MAP.put(ChatColor.GRAY, new Color(170, 170, 170));
        LEGACY_COLOR_MAP.put(ChatColor.DARK_GRAY, new Color(85, 85, 85));
        LEGACY_COLOR_MAP.put(ChatColor.BLUE, new Color(85, 85, 255));
        LEGACY_COLOR_MAP.put(ChatColor.GREEN, new Color(85, 255, 85));
        LEGACY_COLOR_MAP.put(ChatColor.AQUA, new Color(85, 255, 255));
        LEGACY_COLOR_MAP.put(ChatColor.RED, new Color(255, 85, 85));
        LEGACY_COLOR_MAP.put(ChatColor.LIGHT_PURPLE, new Color(255, 85, 255));
        LEGACY_COLOR_MAP.put(ChatColor.YELLOW, new Color(255, 255, 85));
        LEGACY_COLOR_MAP.put(ChatColor.WHITE, new Color(255, 255, 255));
    }

    private LegacyColorMatcher() {}

    public static ChatColor getClosest(Color targetColor) {
        ChatColor closestColor = ChatColor.WHITE;
        double minDistance = Double.MAX_VALUE;

        for (Map.Entry<ChatColor, Color> entry : LEGACY_COLOR_MAP.entrySet()) {
            double distance = colorDistance(targetColor, entry.getValue());
            if (distance < minDistance) {
                minDistance = distance;
                closestColor = entry.getKey();
            }
        }
        return closestColor;
    }

    private static double colorDistance(Color c1, Color c2) {
        int r1 = c1.getRed(); int g1 = c1.getGreen(); int b1 = c1.getBlue();
        int r2 = c2.getRed(); int g2 = c2.getGreen(); int b2 = c2.getBlue();
        return Math.sqrt(Math.pow(r1 - r2, 2) + Math.pow(g1 - g2, 2) + Math.pow(b1 - b2, 2));
    }
}