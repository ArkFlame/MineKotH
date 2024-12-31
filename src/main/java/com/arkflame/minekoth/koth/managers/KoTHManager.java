package com.arkflame.minekoth.koth.managers;

import com.arkflame.minekoth.koth.KoTH;

import java.util.HashMap;
import java.util.Map;

public class KoTHManager {
    private Map<Integer, KoTH> koths = new HashMap<>();
    private int nextId = 1;

    /**
     * Get the next unique ID for a KoTH.
     * 
     * @return the next ID
     */
    public int getNextId() {
        return nextId++;
    }

    /**
     * Adds an existing KoTH to the manager.
     * 
     * @param koth The KoTH object to add.
     */
    public void addKoTH(KoTH koth) {
        if (koth != null) {
            koths.put(koth.getId(), koth);
        }
    }

    /**
     * Get a KoTH by its ID.
     * 
     * @param id The ID of the KoTH.
     * @return The KoTH object, or null if not found.
     */
    public KoTH getKoTHById(int id) {
        return koths.get(id);
    }

    /**
     * Delete a KoTH by its ID.
     * 
     * @param id The ID of the KoTH to delete.
     * @return the deleted KoTH or null if it doesn't exist.
     */
    public KoTH deleteKoTH(int id) {
        return koths.remove(id);
    }

    /**
     * Get all the KoTHs currently managed.
     * 
     * @return A map of KoTHs by their IDs.
     */
    public Map<Integer, KoTH> getAllKoTHs() {
        return new HashMap<>(koths);
    }
}
