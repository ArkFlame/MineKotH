package com.arkflame.minekoth.colorapi.util;

import net.md_5.bungee.api.ChatColor;
import java.awt.Color;

/**
 * An internal, version-agnostic representation of a color.
 * It can hold either a modern java.awt.Color (for hex) or a legacy ChatColor enum.
 * This completely avoids the versioning issues of Bungee's ChatColor class.
 */
public class ColorWrapper {
    private final Color color; // For hex
    private final ChatColor legacyColor; // For &c, &l, etc.

    public ColorWrapper(Color color) {
        this.color = color;
        this.legacyColor = null;
    }

    public ColorWrapper(ChatColor legacyColor) {
        this.legacyColor = legacyColor;
        this.color = null;
    }

    public boolean isLegacy() {
        return legacyColor != null;
    }

    public Color getColor() {
        if (color != null) {
            return color;
        }
        // If we only have a legacy color, we need its RGB value.
        // We find it by looking it up in our matcher's map.
        if (legacyColor != null) {
            Color awtColor = LegacyColorMatcher.LEGACY_COLOR_MAP.get(legacyColor);
            return awtColor != null ? awtColor : Color.WHITE;
        }
        return Color.WHITE;
    }

    public ChatColor getLegacyColor() {
        return legacyColor;
    }

    /**
     * Gets the appropriate Bungee ChatColor object for this wrapper.
     * On 1.8, it will find the closest legacy color for a hex value.
     */
    public ChatColor toBungee() {
        if (legacyColor != null) {
            return legacyColor;
        }
        // If we have a hex color...
        if (HexColorUtil.isModern()) {
            // On modern servers, we can use the hex value directly.
            return HexColorUtil.of(color);
        } else {
            // On legacy servers, find the closest matching legacy color.
            return LegacyColorMatcher.getClosest(color);
        }
    }
    
    /**
     * Gets the appropriate legacy string representation ('§c', or '§x§R§R...').
     */
    public String toLegacyString() {
        if (legacyColor != null) {
            return legacyColor.toString();
        }
        // If we have a hex color...
        return HexColorUtil.toLegacyFormat(color);
    }
}