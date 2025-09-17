package com.arkflame.minekoth.koth.events.config;

import com.arkflame.minekoth.MineKoth;
import org.bukkit.Material;
import org.bukkit.Server;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitScheduler;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

public class KothEventsCaptureRewardsConfig {

    private static final String ITEM_PREFIX = "item: ";
    private static final String COMMAND_PREFIX = "command: ";
    private static final String MESSAGE_KEY_PREFIX = "message-key: ";

    private final MineKoth plugin = MineKoth.getInstance();
    private boolean captureRewardsEnabled;
    private final Map<Integer, List<String>> oneTimeRewards = new HashMap<>();
    private final Map<Integer, List<String>> repeatingRewards = new HashMap<>();

    public KothEventsCaptureRewardsConfig(FileConfiguration config) {
        loadCaptureRewards(config);
    }

    private void loadCaptureRewards(FileConfiguration config) {
        ConfigurationSection captureRewardsSection = config.getConfigurationSection("capture-rewards");
        if (captureRewardsSection == null) {
            this.captureRewardsEnabled = false;
            return;
        }

        this.captureRewardsEnabled = captureRewardsSection.getBoolean("enabled", false);
        if (!captureRewardsEnabled) {
            return;
        }

        loadRewardsFromSection(captureRewardsSection.getConfigurationSection("one-time"), oneTimeRewards);
        loadRewardsFromSection(captureRewardsSection.getConfigurationSection("repeating"), repeatingRewards);
    }

    private void loadRewardsFromSection(ConfigurationSection section, Map<Integer, List<String>> targetMap) {
        if (section == null) {
            return;
        }
        Logger logger = plugin.getLogger();
        for (String key : section.getKeys(false)) {
            try {
                int time = Integer.parseInt(key);
                List<String> rewards = section.getStringList(key);
                targetMap.put(time, rewards);
            } catch (NumberFormatException e) {
                logger.warning("Invalid time key '" + key + "' in capture-rewards section. It must be a number.");
            }
        }
    }

    public void giveRewards(Player player, long timeCaptured) {
        if (!isCaptureRewardsEnabled()) {
            return;
        }

        Collection<String> rewards = getRewardsForTime((int) timeCaptured);
        for (String rewardString : rewards) {
            processReward(player, rewardString);
        }
    }

    private void processReward(Player player, String rewardString) {
        if (rewardString == null || rewardString.isEmpty()) {
            return;
        }

        if (rewardString.startsWith(ITEM_PREFIX)) {
            giveItemReward(player, rewardString);
        } else if (rewardString.startsWith(COMMAND_PREFIX)) {
            executeCommandReward(player, rewardString);
        } else if (rewardString.startsWith(MESSAGE_KEY_PREFIX)) {
            sendMessageReward(player, rewardString);
        }
    }

    private void giveItemReward(Player player, String rewardString) {
        String itemData = rewardString.substring(ITEM_PREFIX.length());
        String[] parts = itemData.split(",", 2);
        Logger logger = plugin.getLogger();

        if (parts.length != 2) {
            logger.warning("Invalid item reward format: '" + rewardString + "'. Expected format: 'item: MATERIAL_NAME,QUANTITY'");
            return;
        }

        String materialName = parts[0].trim().toUpperCase();
        Material material = Material.matchMaterial(materialName);

        if (material == null) {
            logger.warning("Invalid material in item reward: '" + materialName + "' in reward string '" + rewardString + "'");
            return;
        }

        try {
            int quantity = Integer.parseInt(parts[1].trim());
            if (quantity > 0) {
                ItemStack itemStack = new ItemStack(material, quantity);
                BukkitScheduler scheduler = plugin.getServer().getScheduler();
                scheduler.runTask(plugin, () -> player.getInventory().addItem(itemStack));
            }
        } catch (NumberFormatException e) {
            logger.warning("Invalid quantity in item reward: '" + parts[1].trim() + "' in reward string '" + rewardString + "'");
        }
    }

    private void executeCommandReward(Player player, String rewardString) {
        String command = rewardString.substring(COMMAND_PREFIX.length())
                                     .replace("%player%", player.getName());

        Server server = plugin.getServer();
        BukkitScheduler scheduler = server.getScheduler();
        ConsoleCommandSender console = server.getConsoleSender();

        scheduler.runTask(plugin, () -> server.dispatchCommand(console, command));
    }



    private void sendMessageReward(Player player, String rewardString) {
        String messageKey = rewardString.substring(MESSAGE_KEY_PREFIX.length());
        plugin.getLangManager().sendMessage(player, messageKey);
    }

    private Collection<String> getRewardsForTime(int time) {
        Collection<String> rewards = new HashSet<>();
        List<String> oneTime = oneTimeRewards.get(time);
        if (oneTime != null) {
            rewards.addAll(oneTime);
        }

        for (Map.Entry<Integer, List<String>> entry : repeatingRewards.entrySet()) {
            int interval = entry.getKey();
            if (interval > 0 && time % interval == 0) {
                rewards.addAll(entry.getValue());
            }
        }
        return rewards;
    }

    public boolean isCaptureRewardsEnabled() {
        return captureRewardsEnabled;
    }
}