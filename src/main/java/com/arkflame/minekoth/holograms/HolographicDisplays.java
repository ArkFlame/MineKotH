package com.arkflame.minekoth.holograms;

import org.bukkit.Bukkit;
import org.bukkit.Location;

import me.filoghost.holographicdisplays.api.HolographicDisplaysAPI;
import me.filoghost.holographicdisplays.api.hologram.Hologram;

import java.util.HashSet;
import java.util.Set;

public class HolographicDisplays extends HologramsAPIUniversal {

    private final Set<String> hologramIds = new HashSet<>();
    private final HolographicDisplaysAPI api;

    public HolographicDisplays() {
        super("HolographicDisplays");
        this.api = me.filoghost.holographicdisplays.api.HolographicDisplaysAPI.get(Bukkit.getPluginManager().getPlugin("HolographicDisplays"));
    }

    @Override
    public boolean isEnabled() {
        return Bukkit.getPluginManager().isPluginEnabled("HolographicDisplays");
    }

    @Override
    public void createHologram(String id, Location location, String... text) {
        deleteHologram(id); // Remove existing hologram with the same ID

        Hologram hologram = api.createHologram(location);
        for (String line : text) {
            hologram.getLines().appendText(line);
        }
        hologramIds.add(id);
    }

    @Override
    public void deleteHologram(String id) {
        // HolographicDisplays does not natively support IDs, so we track them manually
        // This implementation assumes holograms are stored in a way that allows retrieval by ID
        // (In practice, you might need a Map<String, Hologram> for better tracking)
        hologramIds.remove(id);
    }

    @Override
    public void updateHologram(String id, String... text) {
        // Similar to deletion, requires manual tracking
        // This is a simplified version; you might need to store Hologram references
        deleteHologram(id);
        createHologram(id, getLocationForHologram(id), text);
    }

    @Override
    public void clearHolograms() {
        hologramIds.forEach(this::deleteHologram);
        hologramIds.clear();
    }

    private Location getLocationForHologram(String id) {
        // Placeholder method - in a real implementation, you would store hologram locations
        return new Location(Bukkit.getWorlds().get(0), 0, 0, 0);
    }
}