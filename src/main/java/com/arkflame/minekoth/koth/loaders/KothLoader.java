package com.arkflame.minekoth.koth.loaders;

import com.arkflame.minekoth.koth.Koth;
import com.arkflame.minekoth.koth.Position;
import com.arkflame.minekoth.koth.Rewards;
import com.arkflame.minekoth.utils.ConfigUtil;

import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

public class KothLoader {
    private final JavaPlugin plugin;
    private final File kothsDir;
    private final ConfigUtil configUtil;

    public KothLoader(JavaPlugin plugin) {
        this.plugin = plugin;
        this.kothsDir = new File(plugin.getDataFolder(), "koths");
        this.configUtil = new ConfigUtil(plugin);
        configUtil.createDirectory(kothsDir);
    }

    public void save(Koth koth) {
        File kothFile = new File(kothsDir, koth.getId() + ".yml");
        YamlConfiguration config = new YamlConfiguration();

        // Set KOTH properties
        config.set("id", koth.getId());
        config.set("name", koth.getName());
        config.set("worldName", koth.getWorldName());
        config.set("timeLimit", koth.getTimeLimit());
        config.set("timeToCapture", koth.getTimeToCapture());
        config.set("times", koth.getTimes());
        config.set("days", koth.getDays());
        config.set("rewards", koth.getRewards().serialize());
        config.set("firstPosition", koth.getFirstPosition().serialize());
        config.set("secondPosition", koth.getSecondPosition().serialize());
        // Save using ConfigUtil
        configUtil.saveConfig(config, kothFile);
    }

    public Koth load(int kothId) {
        File kothFile = new File(kothsDir, kothId + ".yml");
        YamlConfiguration config = configUtil.loadConfig(kothFile);
        
        if (config == null) return null; // Early exit if loading failed

        try {
            int id = config.getInt("id");
            String name = config.getString("name");
            String worldName = config.getString("worldName");
            int timeLimit = config.getInt("timeLimit");
            int timeToCapture = config.getInt("timeToCapture");
            String times = config.getString("times");
            String days = config.getString("days");
            Position firstPosition = Position.deserialize(config.getString("firstPosition"));
            Position secondPosition = Position.deserialize(config.getString("secondPosition"));
            Rewards rewards = Rewards.deserialize(config.getString("rewards"));
            return new Koth(id, name, worldName, firstPosition, secondPosition, timeLimit, timeToCapture, rewards, times, days);
        } catch (Exception e) {
            plugin.getLogger().log(Level.SEVERE, "Failed to load KOTH " + kothId, e);
            return null;
        }
    }

    public boolean delete(int kothId) {
        File kothFile = new File(kothsDir, kothId + ".yml");
        return configUtil.deleteConfig(kothFile);
    }

    public List<Koth> loadAll() {
        List<Koth> koths = new ArrayList<>();
        File[] files = kothsDir.listFiles((dir, name) -> name.endsWith(".yml"));

        if (files == null) return koths;

        for (File file : files) {
            String fileName = file.getName().replace(".yml", "");
            try {
                int kothId = Integer.parseInt(fileName);
                Koth koth = load(kothId);
                if (koth != null) {
                    koths.add(koth);
                }
            } catch (NumberFormatException e) {
                plugin.getLogger().warning("Invalid KOTH file name: " + file.getName());
            }
        }

        return koths;
    }
}
