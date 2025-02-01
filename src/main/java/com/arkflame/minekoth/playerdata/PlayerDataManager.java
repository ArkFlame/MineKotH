package com.arkflame.minekoth.playerdata;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Manages PlayerData instances.
 */
public class PlayerDataManager {

    // Thread-safe map to store loaded PlayerData instances
    private final Map<String, PlayerData> playerDataCache = new ConcurrentHashMap<>();

    /**
     * Retrieves the PlayerData for the given player ID, loading it if not already loaded.
     *
     * @param playerId The unique identifier of the player.
     * @return The PlayerData instance.
     */
    public PlayerData getAndLoad(String playerId) {
        return playerDataCache.computeIfAbsent(playerId, id -> {
            PlayerData playerData = createPlayerDataInstance(id);
            playerData.load();
            return playerData;
        });
    }

    /**
     * Retrieves the PlayerData for the given player ID if it's already loaded.
     *
     * @param playerId The unique identifier of the player.
     * @return The PlayerData instance, or null if not loaded.
     */
    public PlayerData getIfLoaded(String playerId) {
        return playerDataCache.get(playerId);
    }

    /**
     * Saves the PlayerData for the given player ID.
     *
     * @param playerId The unique identifier of the player.
     */
    public void save(String playerId) {
        PlayerData playerData = playerDataCache.get(playerId);
        if (playerData != null) {
            playerData.save();
        }
    }

    /**
     * Removes the PlayerData from the cache for the given player ID.
     *
     * @param playerId The unique identifier of the player.
     */
    public void remove(String playerId) {
        playerDataCache.remove(playerId);
    }

    /**
     * Saves the PlayerData and removes it from the cache for the given player ID.
     *
     * @param playerId The unique identifier of the player.
     */
    public void saveAndRemove(String playerId) {
        PlayerData playerData = playerDataCache.remove(playerId);
        if (playerData != null) {
            playerData.save();
        }
    }

    /**
     * Saves all loaded PlayerData instances.
     */
    public void saveAll() {
        playerDataCache.values().forEach(PlayerData::save);
    }

    /**
     * Clears the cache without saving.
     * Use with caution.
     */
    public void clearCache() {
        playerDataCache.clear();
    }

    /**
     * Creates a new PlayerData instance.
     * Override this method if you use subclasses of PlayerData (e.g., MySQLPlayerData, YamlPlayerData).
     *
     * @param playerId The unique identifier of the player.
     * @return A new PlayerData instance.
     */
    protected PlayerData createPlayerDataInstance(String playerId) {
        // By default, returns a basic PlayerData instance.
        // Replace with custom implementation if needed.
        return new PlayerData();
    }
}
