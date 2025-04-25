package com.arkflame.minekoth.holograms;

import org.bukkit.Bukkit;
import org.bukkit.Location;

public abstract class HologramsAPIUniversal {
    public static HologramsAPIUniversal DECENT_HOLOGRAMS;
    public static HologramsAPIUniversal HOLOGRAPHIC_DISPLAYS;
    public static HologramsAPIUniversal FANCY_HOLOGRAMS;
    public static final HologramsAPIUniversal NONE = new HologramsAPIUniversal("None") {
        @Override
        public void createHologram(String id, Location location, String... text) {
            // No operation
        }

        @Override
        public void deleteHologram(String id) {
            // No operation
        }

        @Override
        public void updateHologram(String id, String... text) {
            // No operation
        }

        @Override
        public void clearHolograms() {
            // No operation
        }
    };

    public static final HologramsAPIUniversal getHologramsAPI() {
        if (Bukkit.getPluginManager().isPluginEnabled("DecentHolograms")) {
            return DECENT_HOLOGRAMS = DECENT_HOLOGRAMS != null ? DECENT_HOLOGRAMS : new DecentHolograms();
        } else if (Bukkit.getPluginManager().isPluginEnabled("HolographicDisplays")) {
            return HOLOGRAPHIC_DISPLAYS = HOLOGRAPHIC_DISPLAYS != null ? HOLOGRAPHIC_DISPLAYS : new HolographicDisplays();
        } else if (Bukkit.getPluginManager().isPluginEnabled("FancyHolograms")) {
            return FANCY_HOLOGRAMS = FANCY_HOLOGRAMS != null ? FANCY_HOLOGRAMS : new FancyHolograms();
        }
        return NONE;
    }

    private String name;
    
    public HologramsAPIUniversal(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public abstract void createHologram(String id, Location location, String... text);
    public abstract void deleteHologram(String id);
    public abstract void updateHologram(String id, String... text);
    public abstract void clearHolograms();
}
