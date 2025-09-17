package com.arkflame.minekoth.bossbarapi;

import org.bukkit.entity.Player;

import com.arkflame.minekoth.bossbarapi.bridge.BossBarBridge;
import com.arkflame.minekoth.bossbarapi.enums.BarColor;
import com.arkflame.minekoth.bossbarapi.enums.BarStyle;

import java.util.Collection;
import java.util.Collections;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * A modern, fluent API for creating and managing Boss Bars.
 * This API is version-agnostic, working from 1.8 to 1.21+.
 */
public class BossBarAPI {
    private final BossBarBridge bridge;
    private final Set<Player> players = Collections.newSetFromMap(new ConcurrentHashMap<>());
    private boolean visible = true;

    /**
     * Internal constructor. A BossBarAPI should be created via the static create()
     * method.
     * 
     * @param bridge The version-specific implementation for this boss bar.
     */
    private BossBarAPI(BossBarBridge bridge) {
        this.bridge = bridge;
    }

    /**
     * Creates a new BossBar.
     * 
     * @return A new BossBarAPI instance.
     */
    public static BossBarAPI create() {
        // This is the FIX: We get the bridge first, then create the BossBarAPI. No
        // recursion.
        BossBarBridge bridge = BossBarManager.createBridge();
        BossBarAPI bar = new BossBarAPI(bridge);
        BossBarManager.addBar(bar);
        return bar;
    }

    public BossBarAPI text(String text) {
        bridge.setText(text);
        return this;
    }

    public BossBarAPI progress(double progress) {
        bridge.setProgress(progress);
        return this;
    }

    public BossBarAPI color(BarColor color) {
        bridge.setColor(color);
        return this;
    }

    public BossBarAPI style(BarStyle style) {
        bridge.setStyle(style);
        return this;
    }

    public BossBarAPI addPlayer(Player player) {
        if (player != null && players.add(player)) {
            bridge.addPlayer(player);
        }
        return this;
    }

    public BossBarAPI removePlayer(Player player) {
        if (player != null && players.remove(player)) {
            bridge.removePlayer(player);
        }
        return this;
    }

    public BossBarAPI addPlayers(Collection<Player> players) {
        players.forEach(this::addPlayer);
        return this;
    }

    public BossBarAPI removeAll() {
        // Create a snapshot to avoid ConcurrentModificationException
        Set<Player> snapshot = ConcurrentHashMap.newKeySet();
        snapshot.addAll(players);
        snapshot.forEach(this::removePlayer);
        return this;
    }

    public BossBarAPI setVisible(boolean visible) {
        if (this.visible != visible) {
            this.visible = visible;
            bridge.setVisible(visible);
        }
        return this;
    }

    /**
     * Permanently destroys this boss bar, removing it for all players
     * and cleaning up any associated resources.
     */
    public void destroy() {
        removeAll();
        bridge.destroy();
        BossBarManager.removeBar(this);
    }

    // Internal methods for the manager
    Set<Player> getPlayers() {
        return players;
    }

    BossBarBridge getBridge() {
        return bridge;
    }
}