package com.arkflame.minekoth.koth.events.config;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import com.arkflame.minekoth.MineKoth;

import org.bukkit.Material;

import java.util.*;

public class KillStreaksConfig {

    private boolean killstreaksEnabled;
    private final Map<Integer, List<String>> firstKillRewards = new HashMap<>();
    private final Map<Integer, List<String>> killstreakRewards = new HashMap<>();

    public KillStreaksConfig(FileConfiguration config) {
        // Load the killstreaks section
        loadKillstreaks(config);
    }

    private void loadKillstreaks(FileConfiguration config) {
        // Check if the killstreaks section exists
        if (!config.isConfigurationSection("killstreaks")) {
            return;
        }

        ConfigurationSection killstreaksSection = config.getConfigurationSection("killstreaks");

        // Load the enabled flag
        killstreaksEnabled = killstreaksSection.getBoolean("enabled", false);

        // Load first kill rewards
        ConfigurationSection firstKillSection = killstreaksSection.getConfigurationSection("first-kill");
        if (firstKillSection != null) {
            for (String key : firstKillSection.getKeys(false)) {
                try {
                    int killstreak = Integer.parseInt(key);
                    List<String> rewards = firstKillSection.getStringList(key);
                    firstKillRewards.put(killstreak, rewards);
                } catch (NumberFormatException e) {
                    // Ignore invalid killstreak keys
                }
            }
        }

        // Load killstreak rewards
        ConfigurationSection killstreakSection = killstreaksSection.getConfigurationSection("killstreak-rewards");
        if (killstreakSection != null) {
            for (String key : killstreakSection.getKeys(false)) {
                try {
                    int killstreak = Integer.parseInt(key);
                    List<String> rewards = killstreakSection.getStringList(key);
                    killstreakRewards.put(killstreak, rewards);
                } catch (NumberFormatException e) {
                    // Ignore invalid killstreak keys
                }
            }
        }
    }

    public boolean isKillstreaksEnabled() {
        return killstreaksEnabled;
    }

    public Map<Integer, List<String>> getFirstKillRewards() {
        return firstKillRewards;
    }

    public Map<Integer, List<String>> getKillstreakRewards() {
        return killstreakRewards;
    }

    public Collection<String> getRewards(int killstreak) {
        Collection<String> rewards = new HashSet<>();
        if (firstKillRewards.containsKey(killstreak)) {
            rewards.addAll(firstKillRewards.get(killstreak));
        }

        if (killstreakRewards.containsKey(killstreak)) {
            rewards.addAll(killstreakRewards.get(killstreak));
        }

        return rewards;
    }

    public void giveRewards(Player player, int killstreak) {
        for (String reward : getRewards(killstreak)) {
            if (reward.startsWith("item: ")) {
                String item = reward.substring("item: ".length()).split(",")[0];
                int quantity = Integer.parseInt(reward.substring("item: ".length()).split(",")[1]);
                player.getInventory().addItem(new ItemStack(Material.matchMaterial(item), quantity));
            } else if (reward.startsWith("command: ")) {
                String command = reward.substring("command: ".length()).replace("%player%", player.getName());
                MineKoth.getInstance().getServer().dispatchCommand(MineKoth.getInstance().getServer().getConsoleSender(), command);
            } else if (reward.startsWith("message-key: ")) {
                String messageKey = reward.substring("message-key: ".length());
                MineKoth.getInstance().getLangManager().sendMessage(player, messageKey);
            }
        }
    }
}