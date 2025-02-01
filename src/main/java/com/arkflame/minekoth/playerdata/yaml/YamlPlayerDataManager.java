package com.arkflame.minekoth.playerdata.yaml;

import com.arkflame.minekoth.playerdata.PlayerData;
import com.arkflame.minekoth.playerdata.PlayerDataManager;
import com.arkflame.minekoth.utils.ConfigUtil;

import java.io.File;
import java.util.logging.Logger;

/**
 * Manager for YamlPlayerData instances.
 */
public class YamlPlayerDataManager extends PlayerDataManager {

    private final File dataFolder;
    private final ConfigUtil configUtil;
    private final Logger logger;

    /**
     * Constructs a new YamlPlayerDataManager.
     *
     * @param dataFolder The folder where player YAML files are stored.
     * @param configUtil Utility for file configuration operations.
     * @param logger     Logger for error messages.
     */
    public YamlPlayerDataManager(File dataFolder, ConfigUtil configUtil, Logger logger) {
        this.dataFolder = dataFolder;
        this.configUtil = configUtil;
        this.logger = logger;
    }

    /**
     * Creates a new YamlPlayerData instance.
     *
     * @param playerId The unique identifier of the player.
     * @return A new YamlPlayerData instance.
     */
    @Override
    protected PlayerData createPlayerDataInstance(String playerId) {
        // Ensure the data folder exists.
        if (!dataFolder.exists() && !dataFolder.mkdirs()) {
            logger.warning("Unable to create data folder: " + dataFolder.getAbsolutePath());
        }
        // Each player file is named "<playerId>.yml"
        File file = new File(dataFolder, playerId + ".yml");
        return new YamlPlayerData(file, configUtil, logger);
    }
}
