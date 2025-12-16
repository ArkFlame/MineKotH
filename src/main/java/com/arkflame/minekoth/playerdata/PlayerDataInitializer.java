package com.arkflame.minekoth.playerdata;

import java.io.File;
import java.util.logging.Logger;

import org.bukkit.configuration.Configuration;

import com.arkflame.minekoth.MineKoth;
import com.arkflame.minekoth.playerdata.mysql.MySQLPlayerDataManager;
import com.arkflame.minekoth.playerdata.yaml.YamlPlayerDataManager;
import com.arkflame.minekoth.utils.ConfigUtil;

public class PlayerDataInitializer {
    public static PlayerDataManager initializeDatabase(MineKoth plugin) {
        Configuration config = plugin.getConfig();
        String storageType = config.getString("player-data.storage", "mysql").toLowerCase();
        return initializeDatabase(plugin, storageType);
    }

    public static PlayerDataManager initializeDatabase(MineKoth plugin, String storageType) {
        Logger logger = plugin.getLogger();
        Configuration config = plugin.getConfig();
        switch (storageType) {
            case "mysql":
                String urlPath = "player-data.mysql.url";
                String userPath = "player-data.mysql.username";
                String passPath = "player-data.mysql.password";

                String url = config.getString(urlPath);
                String username = config.getString(userPath);
                String password = config.getString(passPath);

                if (url == null || url.isEmpty()) {
                    logger.info("No value was found in " + urlPath + ", fallback to default mysql url");
                    url = "jdbc:mysql://localhost:3306/db";
                }

                if (username == null || username.isEmpty()) {
                    logger.info("No value was found in " + userPath + ", fallback to default mysql username");
                    username = "root";
                }

                if (password == null || password.isEmpty()) {
                    logger.info("No value was found in " + passPath + ", fallback to default mysql password");
                    password = "password";
                }

                logger.info("Using MySQLPlayerDataManager with HikariCP. (" + url + ")");
                return new MySQLPlayerDataManager(url, username, password, logger);
            case "yaml":
                File dataFolder = new File(plugin.getDataFolder(),
                        config.getString("player-data.yaml.directory", "playerdata"));
                if (!dataFolder.exists() && !dataFolder.mkdirs()) {
                    logger.severe("====================================================");
                    logger.severe(" FAILED TO CREATE YAML DATA FOLDER: " + dataFolder.getAbsolutePath());
                    logger.severe(" Please check your file permissions.");
                    logger.severe(" The plugin will not enable to prevent data loss.");
                    logger.severe("====================================================");
                    throw new RuntimeException("Failed to initialize YAML storage. Aborting plugin enable.");
                }
                logger.info("Using YamlPlayerDataManager.");
                return new YamlPlayerDataManager(dataFolder, new ConfigUtil(plugin), logger);
            case "memory":
            default:
                // Use in-memory persistence.
                logger.info("Using in-memory PlayerDataManager.");
                return new PlayerDataManager();
        }
    }
}