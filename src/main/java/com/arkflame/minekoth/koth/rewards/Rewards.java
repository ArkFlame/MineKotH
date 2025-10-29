package com.arkflame.minekoth.koth.rewards;

import com.arkflame.minekoth.MineKoth;
import com.arkflame.minekoth.utils.FoliaAPI;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.Configuration;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;

public class Rewards {
    public enum LootType {
        DEFAULT,
        RANDOM,
        MINECLANS_DEFAULT,
        MINECLANS_RANDOM
    }

    private final RewardItems rewardItems;
    private final Collection<String> commands;
    private LootType lootType;
    private int lootAmount;

    public Rewards(Collection<String> commands, Collection<ItemStack> itemsArray, LootType lootType, int lootAmount) {
        this.rewardItems = new RewardItems();
        this.commands = new ArrayList<>();

        if (commands != null) {
            this.commands.addAll(commands);
        }

        if (itemsArray != null) {
            for (ItemStack item : itemsArray) {
                // Delegate to the new class's method
                this.rewardItems.addItem(item);
            }
        }

        this.lootType = lootType;
        this.lootAmount = lootAmount;
    }

    public Rewards() {
        this.rewardItems = new RewardItems();
        this.commands = new ArrayList<>();
        this.lootType = LootType.DEFAULT;
        this.lootAmount = 1;
    }

    // Item-related methods now delegate to RewardItems
    public Collection<ItemStack> getRewardsItems() {
        return rewardItems.getItems();
    }

    // Command-related methods remain unchanged
    public Collection<String> getRewardsCommands() {
        return commands;
    }

    public void addRewardsCommand(String command) {
        commands.add(command);
    }

    public void removeRewardsCommand(String command) {
        commands.remove(command);
    }

    public void clearRewardsCommands() {
        commands.clear();
    }

    // Other getters/setters remain unchanged
    public LootType getLootType() {
        return lootType;
    }

    public void setLootType(LootType lootType) {
        this.lootType = lootType;
    }

    public int getLootAmount() {
        return lootAmount;
    }

    public void setLootAmount(int lootAmount) {
        this.lootAmount = lootAmount;
    }
    
    // The core reward-giving logic remains identical
    public int giveRewards(Player topPlayer) {
        int multiplier = MineKoth.getInstance().getLootMultiplier(topPlayer);

        if (lootType == LootType.DEFAULT || lootType == LootType.MINECLANS_DEFAULT) {
            FoliaAPI.runTaskForRegion(topPlayer.getLocation(), () -> {
                for (int i = 0; i < lootAmount * multiplier; i++) {
                    executeCommands(topPlayer, getRewardsCommands());
                    giveItems(topPlayer, getRewardsItems());
                }
            });

            return getRewardsItems().size() * multiplier + getRewardsCommands().size();
        } else if (lootType == LootType.RANDOM || lootType == LootType.MINECLANS_RANDOM) {
            ArrayList<Object> rewardsPool = new ArrayList<>();
            rewardsPool.addAll(getRewardsItems());
            rewardsPool.addAll(getRewardsCommands());
            
            if (rewardsPool.isEmpty()) {
                return 0;
            }
            
            int rewardsToGive = Math.min(lootAmount, rewardsPool.size());
            FoliaAPI.runTaskForRegion(topPlayer.getLocation(), () -> {
                for (int i = 0; i < rewardsToGive * multiplier; i++) {
                    int index = (int) (Math.random() * rewardsPool.size());
                    Object reward = rewardsPool.get(index);
                    if (reward instanceof ItemStack) {
                        giveItem(topPlayer, (ItemStack) reward);
                    } else if (reward instanceof String) {
                        executeCommand(topPlayer, (String) reward);
                    }
                }
            });

            return rewardsToGive;
        }

        return 0;
    }

    // All private helper methods for giving rewards are identical
    private void executeCommand(Player player, String command) {
        Bukkit.dispatchCommand(Bukkit.getConsoleSender(),
                command.replace("%player%", player.getName()));
    }

    private void executeCommands(Player player, Collection<String> commands) {
        for (String command : commands) {
            executeCommand(player, command);
        }
    }

    private void giveItem(Player player, ItemStack item) {
        if (item != null && item.getType() != Material.AIR && item.getAmount() > 0) {
            HashMap<Integer, ItemStack> remainingItems = player.getInventory().addItem(item);
            for (ItemStack remainingItem : remainingItems.values()) {
                player.getWorld().dropItem(player.getLocation(), remainingItem);
            }
        }
    }

    private void giveItems(Player player, Collection<ItemStack> items) {
        for (ItemStack item : items) {
            giveItem(player, item);
        }
    }
    
    // The save and load methods are now cleaner but produce the exact same YAML structure.
    public void save(Configuration config) {
        // This check is from the original code.
        if (config.isString("rewards")) {
            config.set("rewards", null);
        }
        
        // Ensure the 'rewards' section exists for saving.
        ConfigurationSection rewardsSection = config.getConfigurationSection("rewards");
        if (rewardsSection == null) {
            rewardsSection = config.createSection("rewards");
        }
        
        // Reverted to original logic for saving commands.
        rewardsSection.set("commands", null); // Clear old commands
        for (String command : commands) {
            rewardsSection.set("commands." + command, command);
        }

        // Delegate item saving to the new class.
        rewardItems.save(rewardsSection);
        
        // Save other properties.
        rewardsSection.set("lootType", lootType.name());
    }

    public Rewards load(Configuration config) {
        // Clear current state.
        rewardItems.clearItems();
        commands.clear();
        
        // This check is from the original code.
        if (config.isString("rewards")) {
            config.set("rewards", null);
            return this;
        }
        
        ConfigurationSection section = config.getConfigurationSection("rewards");
        if (section != null) {
            // Reverted to original logic for loading commands.
            ConfigurationSection commandsSection = section.getConfigurationSection("commands");
            if (commandsSection != null) {
                for (String key : commandsSection.getKeys(false)) {
                    String command = commandsSection.getString(key);
                    if (command != null) {
                        commands.add(command);
                    }
                }
            }

            // Delegate item loading to the new class.
            rewardItems.load(section);

            // Logic for loading lootType remains the same.
            String rawLootType = section.getString("lootType", LootType.DEFAULT.name());
            try {
                lootType = LootType.valueOf(rawLootType);
            } catch (IllegalArgumentException e) {
                lootType = LootType.DEFAULT;
            }
        }
        return this;
    }
}