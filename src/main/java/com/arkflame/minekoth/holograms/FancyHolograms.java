package com.arkflame.minekoth.holograms;

import de.oliver.fancyholograms.api.FancyHologramsPlugin;
import de.oliver.fancyholograms.api.HologramManager;
import de.oliver.fancyholograms.api.data.HologramData;
import de.oliver.fancyholograms.api.data.TextHologramData;
import de.oliver.fancyholograms.api.hologram.Hologram;
import org.bukkit.Bukkit;
import org.bukkit.Location;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

public class FancyHolograms extends HologramsAPIUniversal {

    private final Set<String> hologramIds = new HashSet<>();
    private final HologramManager hologramManager;

    public FancyHolograms() {
        super("FancyHolograms");
        if (isEnabled()) {
            this.hologramManager = FancyHologramsPlugin.get().getHologramManager();
        } else {
            hologramManager = null;
        }
    }

    @Override
    public boolean isEnabled() {
        return Bukkit.getPluginManager().isPluginEnabled("FancyHolograms");
    }

    @Override
    public void createHologram(String id, Location location, String... text) {
        deleteHologram(id); // Remove existing hologram with the same ID

        TextHologramData hologramData = new TextHologramData(id, location);
        for (String line : text) {
            hologramData.addLine(line);
        }

        Hologram hologram = hologramManager.create(hologramData);
        hologramManager.addHologram(hologram);
        hologramIds.add(id);
    }

    @Override
    public void deleteHologram(String id) {
        Optional<Hologram> optionalHologram = hologramManager.getHologram(id);
        optionalHologram.ifPresent(hologramManager::removeHologram);
        hologramIds.remove(id);
    }

    @Override
    public void updateHologram(String id, String... text) {
        Optional<Hologram> optionalHologram = hologramManager.getHologram(id);
        if (optionalHologram.isPresent()) {
            Hologram hologram = optionalHologram.get();
            HologramData data = hologram.getData();
            if (data instanceof TextHologramData) {
                TextHologramData textData = (TextHologramData) data;
                textData.getText().clear();
                for (String line : text) {
                    textData.addLine(line);
                }
                hologram.queueUpdate();
            }
        }
    }

    @Override
    public void clearHolograms() {
        for (String id : new HashSet<>(hologramIds)) {
            deleteHologram(id);
        }
        hologramIds.clear();
    }
}
