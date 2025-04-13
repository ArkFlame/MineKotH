package com.arkflame.minekoth.playerdata.mysql;

import com.arkflame.minekoth.playerdata.PlayerData;
import com.arkflame.minekoth.playerdata.PlayerDataManager;
import com.zaxxer.hikari.HikariDataSource;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.bukkit.Bukkit;

/**
 * Manager for MySQLPlayerData instances using HikariCP.
 */
public class MySQLPlayerDataManager extends PlayerDataManager {
    public static final String PLAYER_DATA_TABLE_NAME = "minekoth_player_data";

    private final HikariDataSource dataSource;
    private final Logger logger;

    public Connection getConnection() throws SQLException {
        return dataSource.getConnection();
    }

    /**
     * Constructs a new MySQLPlayerDataManager.
     *
     * @param databaseUrl The JDBC URL of the MySQL database.
     * @param user        The database username.
     * @param password    The database password.
     * @param logger      Logger for error messages.
     */
    public MySQLPlayerDataManager(HikariDataSource dataSource, Logger logger) {
        this.logger = logger;
        this.dataSource = dataSource;
        initializeTables();
    }

    /**
     * Initializes the required tables if they do not exist.
     */
    private void initializeTables() {
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
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Failed to initialize table '" + PLAYER_DATA_TABLE_NAME + "'.", e);
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
