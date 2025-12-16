package com.arkflame.minekoth.holograms;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.plugin.PluginManager;

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

    public static final HologramsAPIUniversal getHologramsAPI(String engine) {
        PluginManager pm = Bukkit.getPluginManager();
        
        if (engine == null) engine = "auto";

        // Check for specific engines
        if (engine.equalsIgnoreCase("DecentHolograms") && pm.isPluginEnabled("DecentHolograms")) {
            return DECENT_HOLOGRAMS = DECENT_HOLOGRAMS != null ? DECENT_HOLOGRAMS : new DecentHolograms();
        } else if (engine.equalsIgnoreCase("HolographicDisplays") && pm.isPluginEnabled("HolographicDisplays")) {
            return HOLOGRAPHIC_DISPLAYS = HOLOGRAPHIC_DISPLAYS != null ? HOLOGRAPHIC_DISPLAYS : new HolographicDisplays();
        } else if (engine.equalsIgnoreCase("FancyHolograms") && pm.isPluginEnabled("FancyHolograms")) {
            return FANCY_HOLOGRAMS = FANCY_HOLOGRAMS != null ? FANCY_HOLOGRAMS : new FancyHolograms();
        }

        // Auto detection
        if (engine.equalsIgnoreCase("auto")) {
            if (pm.isPluginEnabled("DecentHolograms")) {
                return DECENT_HOLOGRAMS = DECENT_HOLOGRAMS != null ? DECENT_HOLOGRAMS : new DecentHolograms();
            } else if (pm.isPluginEnabled("HolographicDisplays")) {
                return HOLOGRAPHIC_DISPLAYS = HOLOGRAPHIC_DISPLAYS != null ? HOLOGRAPHIC_DISPLAYS : new HolographicDisplays();
            } else if (pm.isPluginEnabled("FancyHolograms")) {
                return FANCY_HOLOGRAMS = FANCY_HOLOGRAMS != null ? FANCY_HOLOGRAMS : new FancyHolograms();
            }
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