package com.arkflame.minekoth.utils;

import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.util.logging.Level;

public class ConfigUtil {
    private final JavaPlugin plugin;

    public ConfigUtil(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    public void saveConfig(YamlConfiguration config, File file) {
        try {
            config.save(file);
        } catch (IOException e) {
            plugin.getLogger().log(Level.SEVERE, "Failed to save config file: " + file.getName(), e);
        }
    }

    public YamlConfiguration loadConfig(File file) {
        if (!file.exists()) {
            plugin.getLogger().warning("Config file does not exist: " + file.getName());
            return null;
        }
        return YamlConfiguration.loadConfiguration(file);
    }

    public boolean deleteConfig(File file) {
        if (file.exists()) {
            return file.delete();
        }
        plugin.getLogger().warning("Attempted to delete a non-existent config file: " + file.getName());
        return false;
    }

    public void createDirectory(File dir) {
        if (!dir.exists() && !dir.mkdirs()) {
            plugin.getLogger().log(Level.SEVERE, "Failed to create directory: {0}", dir.getAbsolutePath());
        }
    }
}
