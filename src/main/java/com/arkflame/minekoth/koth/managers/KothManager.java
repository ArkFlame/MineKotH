package com.arkflame.minekoth.koth.managers;

import com.arkflame.minekoth.koth.Koth;

import java.util.HashMap;
import java.util.Map;

public class KothManager {
    private Map<Integer, Koth> koths = new HashMap<>();

    public int generateUniqueId() {
        // Generate a unique ID that is not currently used
        int id = 1; // Start from 1 or any other base value
        while (koths.containsKey(id)) {
            id++;
        }
        return id;
    }

    /**
     * Adds an existing koth to the manager.
     * 
     * @param koth The koth object to add.
     */
    public void addKoth(Koth koth) {
        if (koth != null) {
            koths.put(koth.getId(), koth);
        }
    }

    /**
     * Get a koth by its ID.
     * 
     * @param id The ID of the koth.
     * @return The koth object, or null if not found.
     */
    public Koth getKothById(int id) {
        return koths.get(id);
    }

    /**
     * Delete a koth by its ID.
     * 
     * @param id The ID of the koth to delete.
     * @return the deleted koth or null if it doesn't exist.
     */
    public Koth deleteKoth(int id) {
        Koth removedKoth = koths.remove(id);
        if (removedKoth != null) {
            removedKoth.despawnHologram();
        }
        return removedKoth;
    }

    /**
     * Get all the koths currently managed.
     * 
     * @return A map of koths by their IDs.
     */
    public Map<Integer, Koth> getAllkoths() {
        return new HashMap<>(koths);
    }

    public Koth getKothByName(String kothName) {
        for (Koth koth : koths.values()) {
            if (koth.getName().equalsIgnoreCase(kothName)) {
                return koth;
            }
        }
        return null;
    }

    public void tick() {
        for (Koth koth : koths.values()) {
            if (!koth.isHologramSpawned()) {
                koth.spawnHologram();
            }
        }
    }
}
