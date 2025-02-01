package com.arkflame.minekoth.playerdata.mysql;

import java.sql.Connection;
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
