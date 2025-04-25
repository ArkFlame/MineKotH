package com.arkflame.minekoth.holograms;

import de.oliver.fancyholograms.api.FancyHologramsPlugin;
import de.oliver.fancyholograms.api.data.HologramData;
import de.oliver.fancyholograms.api.data.TextHologramData;
import de.oliver.fancyholograms.api.hologram.Hologram;
import org.bukkit.Location;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public class FancyHolograms extends HologramsAPIUniversal {
    private final Map<String, Hologram> holograms = new ConcurrentHashMap<>();

    public FancyHolograms() {
        super("FancyHolograms");
    }

    @Override
    public void createHologram(String id, Location location, String... text) {
        deleteHologram(id); // Remove existing hologram with the same ID

        TextHologramData hologramData = new TextHologramData(id, location);
        for (String line : text) {
            hologramData.addLine(line);
        }

        Hologram hologram = FancyHologramsPlugin.get().getHologramManager().create(hologramData);
        FancyHologramsPlugin.get().getHologramManager().addHologram(hologram);
        holograms.put(id, hologram);
    }

    @Override
    public void deleteHologram(String id) {
        Hologram hologram = holograms.remove(id);
        if (hologram != null) {
            FancyHologramsPlugin.get().getHologramManager().removeHologram(hologram);
        }
    }

    @Override
    public void updateHologram(String id, String... text) {
        Optional<Hologram> optionalHologram = FancyHologramsPlugin.get().getHologramManager().getHologram(id);
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
        for (String id : holograms.keySet()) {
            deleteHologram(id);
        }
        holograms.clear();
    }
}
