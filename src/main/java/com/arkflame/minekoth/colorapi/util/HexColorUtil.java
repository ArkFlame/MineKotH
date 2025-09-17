package com.arkflame.minekoth.colorapi.util;

import net.md_5.bungee.api.ChatColor;
import java.awt.Color;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * A utility class for handling Hex and Legacy color conversions.
 * It uses reflection to call modern (1.16+) ChatColor methods when available,
 * ensuring backward compatibility with older server versions.
 * The reflected method is cached for performance.
 */
public final class HexColorUtil {

    private static final Method ofColorMethod;
    private static final boolean isModern;
    private static boolean loggedError = false;

    static {
        Method foundMethod;
        try {
            // Reflect the more efficient of(Color) method directly
            foundMethod = ChatColor.class.getMethod("of", Color.class);
        } catch (NoSuchMethodException e) {
            foundMethod = null;
        }
        ofColorMethod = foundMethod;
        isModern = ofColorMethod != null;
    }

    /**
     * Checks if the server version supports native hex colors (1.16+).
     * @return true if hex colors are supported, false otherwise.
     */
    public static boolean isModern() {
        return isModern;
    }

    /**
     * Converts a java.awt.Color to a BungeeCord ChatColor.
     * On modern versions, this will be a hex color.
     * On legacy versions, it will be the closest matching legacy ChatColor.
     *
     * @param color The color to convert.
     * @return The corresponding ChatColor object.
     */
    public static ChatColor of(Color color) {
        if (!isModern) {
            // Fallback for legacy versions (pre-1.16)
            return LegacyColorMatcher.getClosest(color);
        }
        try {
            // Invoke the cached of(Color) method. This is faster than formatting to a String first.
            return (ChatColor) ofColorMethod.invoke(null, color);
        } catch (IllegalAccessException | InvocationTargetException e) {
            // This should ideally never happen unless there's a security manager issue.
            // Log the error once to avoid console spam.
            if (!loggedError) {
                System.err.println("MineKoth failed to invoke the ChatColor.of(Color) method via reflection.");
                e.printStackTrace();
                loggedError = true;
            }
            // Fallback to a safe default color.
            return ChatColor.WHITE;
        }
    }

    /**
     * Converts a java.awt.Color into its string representation for use in Minecraft text.
     * On modern versions, this will be a hex code (e.g., "#RRGGBB").
     * On legacy versions, this will be a legacy color code (e.g., "§c").
     *
     * @param color The color to format.
     * @return The color formatted as a string.
     */
    public static String toLegacyFormat(Color color) {
        // Reuse the main logic to ensure consistency between methods.
        return of(color).toString();
    }
}