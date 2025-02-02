package com.arkflame.minekoth.koth.events.config;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.Material;
import com.arkflame.minekoth.MineKoth;

import java.util.*;

public class KothEventsCaptureRewardsConfig {

    private boolean captureRewardsEnabled;
    private final Map<Integer, List<String>> oneTimeRewards = new HashMap<>();
    private final Map<Integer, List<String>> repeatingRewards = new HashMap<>();

    public KothEventsCaptureRewardsConfig(FileConfiguration config) {
        // Load the capture-rewards section
        loadCaptureRewards(config);
    }

    private void loadCaptureRewards(FileConfiguration config) {
        // Check if the capture-rewards section exists
        if (!config.isConfigurationSection("capture-rewards")) {
            return;
        }

        ConfigurationSection captureRewardsSection = config.getConfigurationSection("capture-rewards");

        // Load the enabled flag
        captureRewardsEnabled = captureRewardsSection.getBoolean("enabled", false);

        // Load one-time rewards
        ConfigurationSection oneTimeSection = captureRewardsSection.getConfigurationSection("one-time");
        if (oneTimeSection != null) {
            for (String key : oneTimeSection.getKeys(false)) {
                try {
                    int time = Integer.parseInt(key);
                    List<String> rewards = oneTimeSection.getStringList(key);
                    oneTimeRewards.put(time, rewards);
                } catch (NumberFormatException e) {
                    // Ignore invalid time keys
                }
            }
        }

        // Load repeating rewards
        ConfigurationSection repeatingSection = captureRewardsSection.getConfigurationSection("repeating");
        if (repeatingSection != null) {
            for (String key : repeatingSection.getKeys(false)) {
                try {
                    int time = Integer.parseInt(key);
                    List<String> rewards = repeatingSection.getStringList(key);
                    repeatingRewards.put(time, rewards);
                } catch (NumberFormatException e) {
                    // Ignore invalid time keys
                }
            }
        }
    }

    public boolean isCaptureRewardsEnabled() {
        return captureRewardsEnabled;
    }

    public Map<Integer, List<String>> getOneTimeRewards() {
        return oneTimeRewards;
    }

    public Map<Integer, List<String>> getRepeatingRewards() {
        return repeatingRewards;
    }

    public Collection<String> getReward(int time) {
        Collection<String> rewards = new HashSet<>();
        if (oneTimeRewards.containsKey(time)) {
            rewards.addAll(oneTimeRewards.get(time));
        }

        for (Map.Entry<Integer, List<String>> entry : repeatingRewards.entrySet()) {
            if (time % entry.getKey() == 0) {
                rewards.addAll(entry.getValue());
            }
        }

        return rewards;
    }

    public void giveRewards(Player player, long timeCaptured) {
        for (String reward : getReward((int) timeCaptured)) {
            if (reward.startsWith("item: ")) {
                String item = reward.substring("item: ".length()).split(",")[0];
                int quantity = Integer.parseInt(reward.substring("item: ".length()).split(",")[1]);
                player.getInventory().addItem(new ItemStack(Material.matchMaterial(item), quantity));
            } else if (reward.startsWith("command: ")) {
                String command = reward.substring("command: ".length()).replace("%player%", player.getName());
                MineKoth.getInstance().getServer().dispatchCommand(MineKoth.getInstance().getServer().getConsoleSender(), command);
            } else if (reward.startsWith("message-key: ")) {
                String messageKey = reward.substring("message-key: ".length());
                player.sendMessage(MineKoth.getInstance().getLangManager().getLang(player).getMessage(messageKey));
            }
        }
    }
}