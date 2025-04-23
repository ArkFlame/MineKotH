package com.arkflame.minekoth.playerdata;

import java.io.File;

import com.arkflame.minekoth.MineKoth;
import com.arkflame.minekoth.playerdata.mysql.MySQLPlayerDataManager;
import com.arkflame.minekoth.playerdata.yaml.YamlPlayerDataManager;
import com.arkflame.minekoth.utils.ConfigUtil;

public class PlayerDataInitializer {
    public static PlayerDataManager initializeDatabase(MineKoth plugin) {
        // Get the storage type from config (mysql, yaml, memory).
        String storageType = plugin.getConfig().getString("player-data.storage", "mysql").toLowerCase();

        switch (storageType) {
            case "mysql":
                // MySQL configuration from config.yml.
                String url = plugin.getConfig().getString("player-data.mysql.url", "jdbc:mysql://localhost:3306/db");
                String username = plugin.getConfig().getString("player-data.mysql.username", "root");
                String password = plugin.getConfig().getString("player-data.mysql.password", "password");

                // Attempt to create a HikariCP DataSource.
                plugin.getLogger().info("Using MySQLPlayerDataManager with HikariCP.");
                return new MySQLPlayerDataManager(url, username, password, plugin.getLogger());
            case "yaml":
                // Use YAML-based persistence.
                File dataFolder = new File(plugin.getDataFolder(), plugin.getConfig().getString("player-data.yaml.directory", "playerdata"));
                // Ensure the data folder exists.
                if (!dataFolder.exists() && !dataFolder.mkdirs()) {
                    plugin.getLogger().severe("Failed to create YAML data folder: " + dataFolder.getAbsolutePath());
                    return new PlayerDataManager();
                }
                plugin.getLogger().info("Using YamlPlayerDataManager.");
                return new YamlPlayerDataManager(dataFolder, new ConfigUtil(plugin), plugin.getLogger());
            case "memory":
            default:
                // Use in-memory persistence.
                plugin.getLogger().info("Using in-memory PlayerDataManager.");
                return new PlayerDataManager();
        }
    }
}