package com.arkflame.minekoth.holograms;

import org.bukkit.Location;

public abstract class HologramsAPIUniversal {
    public static final HologramsAPIUniversal DECENT_HOLOGRAMS = new DecentHolograms();
    public static final HologramsAPIUniversal HOLOGRAPHIC_DISPLAYS = new HolographicDisplays();
    public static final HologramsAPIUniversal FANCY_HOLOGRAMS = new FancyHolograms();
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
        if (DECENT_HOLOGRAMS.isEnabled()) {
            return DECENT_HOLOGRAMS;
        } else if (HOLOGRAPHIC_DISPLAYS.isEnabled()) {
            return HOLOGRAPHIC_DISPLAYS;
        } else if (FANCY_HOLOGRAMS.isEnabled()) {
            return FANCY_HOLOGRAMS;
        }
        return NONE;
    }

    private String name;
    private boolean enabled;
    
    public HologramsAPIUniversal(String name) {
        this.name = name;
        this.enabled = false;
    }

    public String getName() {
        return name;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public abstract void createHologram(String id, Location location, String... text);
    public abstract void deleteHologram(String id);
    public abstract void updateHologram(String id, String... text);
    public abstract void clearHolograms();
}
