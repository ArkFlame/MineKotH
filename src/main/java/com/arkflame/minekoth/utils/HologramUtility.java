package com.arkflame.minekoth.utils;

import eu.decentsoftware.holograms.api.DHAPI;
import eu.decentsoftware.holograms.api.holograms.Hologram;
import org.bukkit.Location;
import org.bukkit.plugin.java.JavaPlugin;

import com.google.common.collect.Lists;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class HologramUtility {

    private static boolean isDecentHologramsEnabled = false;
    private static final Map<String, Hologram> hologramCache = new HashMap<>();

    /**
     * Initializes the HologramUtility with the plugin instance.
     * Checks if DecentHolograms is enabled and sets the flag accordingly.
     *
     * @param plugin The plugin instance.
     */
    public static void initialize(JavaPlugin plugin) {
        isDecentHologramsEnabled = plugin.getServer().getPluginManager().isPluginEnabled("DecentHolograms");
        if (isDecentHologramsEnabled) {
            plugin.getLogger().info("DecentHolograms is enabled. HologramUtility initialized.");
        } else {
            plugin.getLogger().warning("DecentHolograms is not enabled. HologramUtility will not function.");
        }
    }

    /**
     * Creates a hologram at the specified location with the given text.
     * Caches the hologram for reuse.
     *
     * @param id       A unique identifier for the hologram.
     * @param location The location to spawn the hologram.
     * @param text     The text to display on the hologram.
     */
    public static void createHologram(String id, Location location, String ...text) {
        if (!isDecentHologramsEnabled) {
            return;
        }

        if (hologramCache.containsKey(id)) {
            hologramCache.remove(id).delete();
        }

        Hologram hologram = DHAPI.createHologram(UUID.randomUUID().toString(), location, false, Arrays.asList(text));
        if (hologram != null) {
            hologramCache.put(id, hologram);
        }
    }

    /**
     * Updates the text of an existing hologram.
     *
     * @param id   The unique identifier of the hologram.
     * @param text The new text to display on the hologram.
     */
    public static void updateHologram(String id, String text) {
        if (!isDecentHologramsEnabled) {
            return;
        }

        Hologram hologram = hologramCache.get(id);
        if (hologram != null) {
            DHAPI.setHologramLines(hologram, Collections.singletonList(text));
        }
    }

    /**
     * Deletes a hologram by its ID.
     *
     * @param id The unique identifier of the hologram.
     */
    public static void deleteHologram(String id) {
        if (!isDecentHologramsEnabled) {
            return;
        }

        Hologram hologram = hologramCache.remove(id);
        if (hologram != null) {
            hologram.delete();
        }
    }

    /**
     * Clears all cached holograms and deletes them.
     */
    public static void clearHolograms() {
        if (!isDecentHologramsEnabled) {
            return;
        }

        for (Hologram hologram : hologramCache.values()) {
            hologram.delete();
        }
        hologramCache.clear();
    }
}