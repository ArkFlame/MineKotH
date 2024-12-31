package com.arkflame.minekoth.utils;

import org.bukkit.Location;

public class Locations {
    public static String toString(Location loc) {
        return "X: " + loc.getBlockX() + " Y: " + loc.getBlockY() + " Z: " + loc.getBlockZ();
    }
}
