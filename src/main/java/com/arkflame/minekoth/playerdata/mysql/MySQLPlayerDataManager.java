package com.arkflame.minekoth.playerdata.mysql;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.arkflame.minekoth.playerdata.PlayerData;
import com.arkflame.minekoth.playerdata.PlayerDataManager;

/**
 * Manager for MySQLPlayerData instances.
 */
public class MySQLPlayerDataManager extends PlayerDataManager {

    private final Connection connection;
    private final Logger logger;

    /**
     * Constructs a new MySQLPlayerDataManager.
     *
     * @param connection JDBC connection to the MySQL database.
     * @param logger     Logger for error messages.
     */
    public MySQLPlayerDataManager(Connection connection, Logger logger) {
        this.connection = connection;
        this.logger = logger;
        initializeTables(connection, logger);
    }

    /**
     * Initializes the required tables if they do not exist.
     *
     * @param connection JDBC connection to the database.
     * @param logger     Logger for logging messages.
     */
    public static void initializeTables(Connection connection, Logger logger) {
        // Example SQL DDL to create the player_data table.
        String sql = "CREATE TABLE IF NOT EXISTS player_data ("
                + "player_id VARCHAR(36) NOT NULL, "
                + "stat_key VARCHAR(255) NOT NULL, "
                + "is_total BOOLEAN NOT NULL, "
                + "koth_id INT NOT NULL, "
                + "value VARCHAR(255) NOT NULL, "
                + "PRIMARY KEY (player_id, stat_key, koth_id)"
                + ")";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.execute();
            logger.info("Initialized table 'player_data'.");
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Failed to initialize table 'player_data'.", e);
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
        return new MySQLPlayerData(connection, playerId, logger);
    }
}
