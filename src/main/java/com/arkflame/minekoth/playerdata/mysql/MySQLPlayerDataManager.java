package com.arkflame.minekoth.playerdata.mysql;

import com.arkflame.minekoth.playerdata.PlayerData;
import com.arkflame.minekoth.playerdata.PlayerDataManager;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Manager for MySQLPlayerData instances using HikariCP.
 */
public class MySQLPlayerDataManager extends PlayerDataManager {
    public static final String PLAYER_DATA_TABLE_NAME = "minekoth_player_data";

    private HikariDataSource dataSource;
    private HikariConfig config;
    private final Logger logger;

    public Connection getConnection() throws SQLException {
        return dataSource.getConnection();
    }

    /**
     * Creates and returns a HikariCP DataSource for the MySQL database.
     * Returns null if DataSource creation fails.
     */
    public void generateHikariConfig(String url, String username, String password) {
        config = new HikariConfig();
        config.setJdbcUrl(url);
        config.setUsername(username);
        config.setPassword(password);
        config.setMaximumPoolSize(10);
        config.setMinimumIdle(5);
        config.setConnectionTestQuery("SELECT 1");
        config.addDataSourceProperty("cachePrepStmts", "true");
        config.addDataSourceProperty("prepStmtCacheSize", "250");
        config.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");
    }

    /**
     * Creates a new MySQLPlayerDataManager instance.
     *
     * @param host     The MySQL database host.
     * @param port     The MySQL database port.
     * @param database The MySQL database name.
     * @param username The MySQL database username.
     * @param password The MySQL database password.
     * @param logger   The logger for logging messages.
     */
    public MySQLPlayerDataManager(String url, String username, String password,
            Logger logger) {
        this.logger = logger;
        generateHikariConfig(url, username, password);
        dataSource = new HikariDataSource(config);
        while (true) {
            try {
                initializeTables();
                break;
            } catch (SQLException e) {
                logger.severe("Failed to initialize MySQL tables. Retrying...");
                try {
                    Thread.sleep(5000); // Retry after 5 seconds
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        }
    }

    /**
     * Initializes the required tables if they do not exist.
     */
    private void initializeTables() throws SQLException {
        String sql = "CREATE TABLE IF NOT EXISTS " + PLAYER_DATA_TABLE_NAME + " ("
                + "player_id VARCHAR(36) NOT NULL, "
                + "stat_key VARCHAR(255) NOT NULL, "
                + "is_total BOOLEAN NOT NULL, "
                + "koth_id INT NOT NULL, "
                + "value VARCHAR(255) NOT NULL, "
                + "PRIMARY KEY (player_id, stat_key, koth_id))";
        try (Connection connection = getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.execute();
            logger.info("Initialized table '" + PLAYER_DATA_TABLE_NAME + "'.");
        }
    }

    /**
     * Creates a new MySQLPlayerData instance.
     *
     * @param playerId The unique identifier of the player.
     * @return A new MySQLPlayerData instance.
     */
    @Override
    protected PlayerData createPlayerDataInstance(String playerId) {
        return new MySQLPlayerData(dataSource, playerId, logger);
    }

    /**
     * Closes the database connection pool.
     */
    @Override
    public void close() {
        if (dataSource != null) {
            dataSource.close();
            logger.info("Database connection pool closed.");
        }
    }
}
