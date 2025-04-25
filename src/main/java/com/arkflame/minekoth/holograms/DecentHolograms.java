package com.arkflame.minekoth.holograms;

import org.bukkit.Bukkit;
import org.bukkit.Location;

import eu.decentsoftware.holograms.api.DHAPI;
import eu.decentsoftware.holograms.api.holograms.Hologram;

import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class DecentHolograms extends HologramsAPIUniversal {
    private final Map<String, Hologram> hologramIds = new ConcurrentHashMap<>();

    public DecentHolograms() {
        super("DecentHolograms");
    }

    @Override
    public boolean isEnabled() {
        return Bukkit.getPluginManager().isPluginEnabled("DecentHolograms");
    }

    @Override
    public void createHologram(String id, Location location, String... text) {
        if (location.getWorld() == null) {
            return;
        }

        if (!location.getChunk().isLoaded()) {
            return;
        }

        if (hologramIds.containsKey(id)) {
            Hologram oldHologram = hologramIds.remove(id);
            if (oldHologram != null) {
                id = oldHologram.getName();
                oldHologram.delete();
            }
        }

        Hologram hologram = DHAPI.createHologram(id, location, false, Arrays.asList(text));
        if (hologram != null) {
            hologramIds.put(id, hologram);
            return;
        } else {
            DHAPI.removeHologram(id);
        }
    }

    @Override
    public void deleteHologram(String id) {
        Hologram hologram = hologramIds.remove(id);
        if (hologram != null) {
            hologram.delete();
        }
    }

    @Override
    public void updateHologram(String id, String... text) {
        Hologram hologram = hologramIds.get(id);
        if (hologram != null) {
            DHAPI.setHologramLines(hologram, Arrays.asList(text));
        }
    }

    @Override
    public void clearHolograms() {
        for (Hologram hologram : hologramIds.values()) {
            hologram.delete();
        }
        hologramIds.clear();
    }
}