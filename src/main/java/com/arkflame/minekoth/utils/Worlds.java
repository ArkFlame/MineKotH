package com.arkflame.minekoth.utils;

import org.bukkit.Location;

public class Worlds {
    public static void strikeLightning(Location loc) {
        FoliaAPI.runTask(() -> {
            if (loc != null) loc.getWorld().strikeLightning(loc);
        });
    }
    public static void strikeLightningEffect(Location loc) {
        FoliaAPI.runTask(() -> {
            if (loc != null) loc.getWorld().strikeLightningEffect(loc);
        });
    }
}
