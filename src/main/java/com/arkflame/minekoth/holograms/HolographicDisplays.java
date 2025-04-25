package com.arkflame.minekoth.holograms;

import org.bukkit.Bukkit;
import org.bukkit.Location;

import me.filoghost.holographicdisplays.api.HolographicDisplaysAPI;
import me.filoghost.holographicdisplays.api.hologram.Hologram;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class HolographicDisplays extends HologramsAPIUniversal {

    private final Map<String, Hologram> holograms = new ConcurrentHashMap<>();

    public HolographicDisplays() {
        super("HolographicDisplays");
    }

    @Override
    public void createHologram(String id, Location location, String... text) {
        deleteHologram(id); // Remove existing hologram with the same ID

        Hologram hologram = HolographicDisplaysAPI
                .get(Bukkit.getPluginManager().getPlugin("HolographicDisplays")).createHologram(location);
        for (String line : text) {
            hologram.getLines().appendText(line);
        }
        holograms.put(id, hologram);
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
            hologram.getLines().clear();
            for (String line : text) {
                hologram.getLines().appendText(line);
            }
        }
    }

    @Override
    public void clearHolograms() {
        holograms.keySet().forEach(this::deleteHologram);
        holograms.clear();
    }
}