package com.arkflame.minekoth.setup.session;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.entity.Player;

public class SetupSessionManager {
    // Private map to store sessions
    private final Map<Player, SetupSession> sessions = new HashMap<>();

    /**
     * Adds a setup session for the specified player.
     *
     * @param player the player
     * @param session the setup session to associate with the player
     */
    public void addSession(Player player, SetupSession session) {
        sessions.put(player, session);
    }

    /**
     * Retrieves the setup session for the specified player, if present.
     *
     * @param player the player
     * @return the setup session, or null if the player does not have a session
     */
    public SetupSession getSession(Player player) {
        return sessions.get(player);
    }

    /**
     * Checks if a player has an active setup session.
     *
     * @param player the player
     * @return true if the player has a setup session, false otherwise
     */
    public boolean hasSession(Player player) {
        return sessions.containsKey(player);
    }

    /**
     * Removes the setup session for the specified player.
     *
     * @param player the player
     */
    public void removeSession(Player player) {
        sessions.remove(player);
    }

    /**
     * Clears all setup sessions.
     */
    public void clearSessions() {
        sessions.clear();
    }

    /**
     * Retrieves the number of active setup sessions.
     *
     * @return the size of the sessions map
     */
    public int getSessionCount() {
        return sessions.size();
    }
}
