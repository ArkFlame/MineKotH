package com.arkflame.minekoth.bossbarapi.bridge;

import org.bukkit.Bukkit;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.Player;

import com.arkflame.minekoth.colorapi.ColorAPI;
import com.arkflame.minekoth.bossbarapi.enums.BarColor;
import com.arkflame.minekoth.bossbarapi.enums.BarStyle;

/**
 * Implementation for 1.9+ servers using the native Bukkit BossBar API.
 */
public class ModernBossBar implements BossBarBridge { // <--- THIS IS NOW PUBLIC
    private final BossBar bossBar;

    public ModernBossBar() {
        this.bossBar = Bukkit.createBossBar("", org.bukkit.boss.BarColor.WHITE, org.bukkit.boss.BarStyle.SOLID);
    }

    @Override
    public void setText(String text) {
        bossBar.setTitle(ColorAPI.colorize(text).toLegacyText());
    }

    @Override
    public void setProgress(double progress) {
        bossBar.setProgress(Math.max(0.0, Math.min(1.0, progress)));
    }

    @Override
    public void setColor(BarColor color) {
        bossBar.setColor(org.bukkit.boss.BarColor.valueOf(color.name()));
    }

    @Override
    public void setStyle(BarStyle style) {
        bossBar.setStyle(org.bukkit.boss.BarStyle.valueOf(style.name()));
    }

    @Override
    public void setVisible(boolean visible) {
        bossBar.setVisible(visible);
    }
    
    @Override
    public void addPlayer(Player player) {
        bossBar.addPlayer(player);
    }

    @Override
    public void removePlayer(Player player) {
        bossBar.removePlayer(player);
    }

    @Override
    public void destroy() {
        bossBar.removeAll();
    }
}