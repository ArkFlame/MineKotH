package com.arkflame.minekoth.playerdata;

import java.io.File;
import java.util.logging.Level;

import com.arkflame.minekoth.MineKoth;
import com.arkflame.minekoth.playerdata.mysql.MySQLPlayerDataManager;
import com.arkflame.minekoth.playerdata.yaml.YamlPlayerDataManager;
import com.arkflame.minekoth.utils.ConfigUtil;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

public class PlayerDataInitializer {

    /**
     * Creates and returns a HikariCP DataSource for the MySQL database.
     * Returns null if DataSource creation fails.
     */
    public static HikariDataSource createHikariDataSource(String host, int port, String database, String username, String password) {
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl("jdbc:mysql://" + host + ":" + port + "/" + database + "?useSSL=false");
        config.setUsername(username);
        config.setPassword(password);
        config.setDriverClassName("com.mysql.cj.jdbc.Driver");

        // Optional: Configure HikariCP settings (e.g., pool size, timeout, etc.)
        config.setMaximumPoolSize(10);
        config.setConnectionTimeout(30000);
        config.setIdleTimeout(600000);
        config.setMaxLifetime(1800000);

        try {
            return new HikariDataSource(config);
        } catch (Exception e) {
            MineKoth.getInstance().getLogger().log(Level.SEVERE, "Error establishing HikariCP DataSource.", e);
            return null;
        }
    }

    public static PlayerDataManager initializeDatabase(MineKoth plugin) {
        // Get the storage type from config (mysql, yaml, memory).
        String storageType = plugin.getConfig().getString("playerdata.storage", "memory").toLowerCase();

        switch (storageType) {
            case "mysql":
                // MySQL configuration from config.yml.
                String host = plugin.getConfig().getString("mysql.host", "localhost");
                int port = plugin.getConfig().getInt("mysql.port", 3306);
                String database = plugin.getConfig().getString("mysql.database", "mydatabase");
                String username = plugin.getConfig().getString("mysql.username", "myusername");
                String password = plugin.getConfig().getString("mysql.password", "mypassword");

                // Attempt to create a HikariCP DataSource.
                plugin.getLogger().info("Using MySQLPlayerDataManager with HikariCP.");
                return new MySQLPlayerDataManager(createHikariDataSource(host, port, database, username, password), plugin.getLogger());
            case "yaml":
                // Use YAML-based persistence.
                File dataFolder = new File(plugin.getDataFolder(), plugin.getConfig().getString("yaml.directory", "playerdata"));
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