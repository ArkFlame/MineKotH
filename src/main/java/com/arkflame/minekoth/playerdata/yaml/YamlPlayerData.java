package com.arkflame.minekoth.playerdata.yaml;

import com.arkflame.minekoth.playerdata.PlayerData;
import com.arkflame.minekoth.playerdata.StatValue;
import com.arkflame.minekoth.utils.ConfigUtil;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Extends PlayerData to load and save player statistics to a YAML file.
 * 
 * Uses a provided ConfigUtil for file operations.
 */
public class YamlPlayerData extends PlayerData {

    private final File file;
    private final ConfigUtil configUtil;
    private final Logger logger;
    private YamlConfiguration config;

    public YamlPlayerData(File file, ConfigUtil configUtil, Logger logger) {
        this.file = file;
        this.configUtil = configUtil;
        this.logger = logger;
    }

    @Override
    public void load() {
        config = configUtil.loadConfig(file);
        if (config == null) {
            logger.log(Level.WARNING, "Config file not found or could not be loaded: " + file.getName());
            return;
        }

        // Clear current data
        clearData();

        // Load total stats from the "totalStats" section.
        if (config.isConfigurationSection("totalStats")) {
            Set<String> totalKeys = config.getConfigurationSection("totalStats").getKeys(false);
            for (String key : totalKeys) {
                Object value = config.get("totalStats." + key);
                if (value instanceof Number) {
                    super.setTotal(key, (Number) value);
                }
            }
        }

        // Load per-Koth stats from the "statsByKoth" section.
        if (config.isConfigurationSection("statsByKoth")) {
            Set<String> statKeys = config.getConfigurationSection("statsByKoth").getKeys(false);
            for (String key : statKeys) {
                if (config.isConfigurationSection("statsByKoth." + key)) {
                    Set<String> kothIds = config.getConfigurationSection("statsByKoth." + key).getKeys(false);
                    for (String idStr : kothIds) {
                        try {
                            int kothId = Integer.parseInt(idStr);
                            Object value = config.get("statsByKoth." + key + "." + idStr);
                            if (value instanceof Number) {
                                super.setByKoth(key, kothId, (Number) value);
                            }
                        } catch (NumberFormatException e) {
                            logger.log(Level.WARNING, "Invalid koth id in config: " + idStr, e);
                        }
                    }
                }
            }
        }
    }

    @Override
    public void save() {
        // Create a new configuration if needed.
        if (config == null) {
            config = new YamlConfiguration();
        }

        // Save total stats.
        for (Map.Entry<String, StatValue> entry : getTotalStats().entrySet()) {
            config.set("totalStats." + entry.getKey(), entry.getValue().getValue());
        }

        // Save per-Koth stats.
        for (Map.Entry<String, Map<Integer, StatValue>> mapEntry : getStatsByKoth().entrySet()) {
            String key = mapEntry.getKey();
            for (Map.Entry<Integer, StatValue> kothEntry : mapEntry.getValue().entrySet()) {
                config.set("statsByKoth." + key + "." + kothEntry.getKey(), kothEntry.getValue().getValue());
            }
        }

        // Save the configuration to file.
        configUtil.saveConfig(config, file);
    }

    /**
     * Clears the in-memory maps. Uses protected getters from PlayerData.
     */
    private void clearData() {
        getTotalStats().clear();
        getStatsByKoth().clear();
    }
}
