package com.arkflame.minekoth.playerdata.mysql;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.arkflame.minekoth.playerdata.PlayerData;
import com.arkflame.minekoth.playerdata.StatValue;
import com.zaxxer.hikari.HikariDataSource;

/**
 * Extends PlayerData to load and save player statistics to a MySQL database.
 * 
 * Assumes a table structure with columns:
 * - player_id (VARCHAR)
 * - stat_key (VARCHAR)
 * - is_total (BOOLEAN)
 * - koth_id (INTEGER, nullable)
 * - value (VARCHAR)
 */
public class MySQLPlayerData extends PlayerData {

    private final HikariDataSource dataSource;
    private final String playerId;
    private final Logger logger;

    public Connection getConnection() throws SQLException {
        return dataSource.getConnection();
    }

    public MySQLPlayerData(HikariDataSource dataSource, String playerId, Logger logger) {
        this.dataSource = dataSource;
        this.playerId = playerId;
        this.logger = logger;
    }

    @Override
    public void load() {
        // Clear any in-memory maps before loading new data.
        clearData();

        String query = "SELECT stat_key, is_total, koth_id, value FROM " + MySQLPlayerDataManager.PLAYER_DATA_TABLE_NAME
                + " WHERE player_id = ?";
        try (Connection connection = getConnection();
                PreparedStatement ps = connection.prepareStatement(query)) {
            ps.setString(1, playerId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    String key = rs.getString("stat_key");
                    boolean isTotal = rs.getBoolean("is_total");
                    int kothId = rs.getInt("koth_id");
                    String valueStr = rs.getString("value");
                    Number value = parseNumber(valueStr);

                    if (isTotal) {
                        super.setTotal(key, value);
                    } else {
                        super.setByKoth(key, kothId, value);
                    }
                }
            }
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Failed to load player data for " + playerId, e);
        }
    }

    @Override
    public void save() {
        // Delete previous entries for this player.
        String deleteQuery = "DELETE FROM " + MySQLPlayerDataManager.PLAYER_DATA_TABLE_NAME + " WHERE player_id = ?";
        try (Connection connection = getConnection();
                PreparedStatement deleteStmt = connection.prepareStatement(deleteQuery)) {
            deleteStmt.setString(1, playerId);
            deleteStmt.executeUpdate();
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Failed to clear existing data for " + playerId, e);
        }

        // Save total stats.
        String insertQuery = "INSERT INTO " + MySQLPlayerDataManager.PLAYER_DATA_TABLE_NAME
                + " (player_id, stat_key, is_total, koth_id, value) VALUES (?, ?, ?, ?, ?)";
        try (Connection connection = getConnection();
                PreparedStatement ps = connection.prepareStatement(insertQuery)) {
            // Save totals.
            for (Map.Entry<String, StatValue> entry : getTotalStats().entrySet()) {
                ps.setString(1, playerId);
                ps.setString(2, entry.getKey());
                ps.setBoolean(3, true);
                ps.setInt(4, -1);
                ps.setString(5, entry.getValue().getValue().toString());
                ps.addBatch();
            }
            // Save per-Koth stats.
            for (Map.Entry<String, Map<Integer, StatValue>> mapEntry : getStatsByKoth().entrySet()) {
                String key = mapEntry.getKey();
                for (Map.Entry<Integer, StatValue> kothEntry : mapEntry.getValue().entrySet()) {
                    ps.setString(1, playerId);
                    ps.setString(2, key);
                    ps.setBoolean(3, false);
                    ps.setInt(4, kothEntry.getKey());
                    ps.setString(5, kothEntry.getValue().getValue().toString());
                    ps.addBatch();
                }
            }
            ps.executeBatch();
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Failed to save player data for " + playerId, e);
        }
    }

    /**
     * Helper method to parse a numeric value from its string representation.
     * Uses a simple heuristic based on the presence of a decimal point.
     */
    private Number parseNumber(String str) {
        try {
            return str.contains(".") ? Double.valueOf(str) : Integer.valueOf(str);
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    /**
     * Clears the in-memory maps. Uses protected getters from PlayerData.
     */
    private void clearData() {
        getTotalStats().clear();
        getStatsByKoth().clear();
    }
}
