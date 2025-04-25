package com.arkflame.minekoth.holograms;

import org.bukkit.Location;

import eu.decentsoftware.holograms.api.DHAPI;
import eu.decentsoftware.holograms.api.holograms.Hologram;

import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class DecentHolograms extends HologramsAPIUniversal {
    private final Map<String, Hologram> holograms = new ConcurrentHashMap<>();

    public DecentHolograms() {
        super("DecentHolograms");
    }

    @Override
    public void createHologram(String id, Location location, String... text) {
        if (location.getWorld() == null) {
            return;
        }

        if (!location.getChunk().isLoaded()) {
            return;
        }

        if (holograms.containsKey(id)) {
            Hologram oldHologram = holograms.remove(id);
            if (oldHologram != null) {
                id = oldHologram.getName();
                oldHologram.delete();
            }
        }

        Hologram hologram = DHAPI.createHologram(id, location, false, Arrays.asList(text));
        if (hologram != null) {
            holograms.put(id, hologram);
            return;
        } else {
            DHAPI.removeHologram(id);
        }
    }

    @Override
    public void deleteHologram(String id) {
        Hologram hologram = holograms.remove(id);
        if (hologram != null) {
            hologram.delete();
        }
    }

    @Override
    public void updateHologram(String id, String... text) {
        Hologram hologram = holograms.get(id);
        if (hologram != null) {
            DHAPI.setHologramLines(hologram, Arrays.asList(text));
        }
    }

    @Override
    public void clearHolograms() {
        for (Hologram hologram : holograms.values()) {
            hologram.delete();
        }
        holograms.clear();
    }
}