package com.arkflame.minekoth.player;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.logging.Level;

import com.arkflame.minekoth.MineKoth;
import com.arkflame.minekoth.player.mysql.MySQLPlayerDataManager;
import com.arkflame.minekoth.player.yaml.YamlPlayerDataManager;
import com.arkflame.minekoth.utils.ConfigUtil;

public class PlayerDataInitializer {
    

    /**
     * Creates and returns a JDBC Connection to the MySQL database.
     * Returns null if connection creation fails.
     */
    public static Connection createMySQLConnection(String host, int port, String database, String username, String password) {
        String url = "jdbc:mysql://" + host + ":" + port + "/" + database + "?useSSL=false";
        try {
            // Load the JDBC driver if necessary.
            Class.forName("com.mysql.cj.jdbc.Driver");
            return DriverManager.getConnection(url, username, password);
        } catch (ClassNotFoundException | SQLException e) {
            MineKoth.getInstance().getLogger().log(Level.SEVERE, "Error establishing MySQL connection.", e);
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

                // Attempt to create a MySQL connection.
                Connection connection = createMySQLConnection(host, port, database, username, password);
                if (connection == null) {
                    plugin.getLogger().severe("Failed to create MySQL connection. Falling back to memory.");
                    return new PlayerDataManager();
                }
                plugin.getLogger().info("Using MySQLPlayerDataManager.");
                return new MySQLPlayerDataManager(connection, plugin.getLogger());
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
