package com.arkflame.minekoth.koth;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.Configuration;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import com.arkflame.minekoth.MineKoth;
import com.arkflame.minekoth.utils.FoliaAPI;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;

public class Rewards {
    public enum LootType {
        DEFAULT,
        RANDOM,
        MINECLANS_DEFAULT,
        MINECLANS_RANDOM
    }

    private Collection<ItemStack> items;
    private Collection<String> commands;
    private LootType lootType;
    private int lootAmount;

    public Rewards(Collection<String> commands, Collection<ItemStack> itemsArray, LootType lootType, int lootAmount) {
        this.items = new ArrayList<>();
        this.commands = new ArrayList<>();

        if (commands != null) {
            this.commands.addAll(commands);
        }

        if (itemsArray != null) {
            this.items.addAll(itemsArray);
        }

        this.lootType = lootType;
        this.lootAmount = lootAmount;
    }

    public Rewards() {
        this.items = new ArrayList<>();
        this.commands = new ArrayList<>();
        this.lootType = LootType.DEFAULT;
        this.lootAmount = 1;
    }

    public Collection<ItemStack> getRewardsItems() {
        return items;
    }

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

    public int giveRewards(Player topPlayer) {
        int multiplier = MineKoth.getInstance().getLootMultiplier(topPlayer);
        
        if (lootType == LootType.DEFAULT || lootType == LootType.MINECLANS_DEFAULT) {
            FoliaAPI.runTaskForRegion(topPlayer.getLocation(), () -> {
                // Execute all reward commands
                executeCommands(topPlayer, getRewardsCommands());
                
                // Give all reward items
                giveItems(topPlayer, getRewardsItems(), multiplier);
            });
    
            return getRewardsItems().size() + getRewardsCommands().size();
        } 
        else if (lootType == LootType.RANDOM || lootType == LootType.MINECLANS_RANDOM) {
            // Combine items and commands into a single collection
            ArrayList<Object> rewardsPool = new ArrayList<>();
            rewardsPool.addAll(getRewardsItems());
            rewardsPool.addAll(getRewardsCommands());
    
            // Shuffle and pick random rewards
            Collections.shuffle(rewardsPool);
            int rewardsToGive = Math.min(lootAmount, rewardsPool.size());
    
            FoliaAPI.runTaskForRegion(topPlayer.getLocation(), () -> {
                for (int i = 0; i < rewardsToGive; i++) {
                    Object reward = rewardsPool.get(i);
    
                    if (reward instanceof ItemStack) {
                        giveItem(topPlayer, (ItemStack) reward, multiplier);
                    } else if (reward instanceof String) {
                        executeCommand(topPlayer, (String) reward);
                    }
                }
            });
    
            return rewardsToGive;
        }
    
        return 0;
    }
    
    /**
     * Executes a command with player placeholder replaced
     */
    private void executeCommand(Player player, String command) {
        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), 
                command.replace("%player%", player.getName()));
    }
    
    /**
     * Executes multiple commands for a player
     */
    private void executeCommands(Player player, Collection<String> commands) {
        for (String command : commands) {
            executeCommand(player, command);
        }
    }
    
    /**
     * Gives a single item to a player with multiplier, handling inventory overflow
     */
    private void giveItem(Player player, ItemStack item, int multiplier) {
        if (item != null && item.getType() != Material.AIR && item.getAmount() > 0) {
            for (int i = 0; i < multiplier; i++) {
                HashMap<Integer, ItemStack> remainingItems = player.getInventory().addItem(item);
                // Drop items that don't fit in the inventory
                for (ItemStack remainingItem : remainingItems.values()) {
                    player.getWorld().dropItem(player.getLocation(), remainingItem);
                }
            }
        }
    }
    
    /**
     * Gives multiple items to a player with multiplier
     */
    private void giveItems(Player player, Collection<ItemStack> items, int multiplier) {
        for (ItemStack item : items) {
            giveItem(player, item, multiplier);
        }
    }

    public void save(Configuration config) {
        if (config.isString("rewards")) {
            config.set("rewards", null);
        }
        for (String command : commands) {
            config.set("rewards.commands." + command, command);
        }
        int i = 0;
        for (ItemStack item : items) {
            config.set("rewards.items." + i++, item);
        }
        config.set("rewards.lootType", lootType.name());
    }

    public Rewards load(Configuration config) {
        items.clear();
        commands.clear();
        if (config.isString("rewards")) {
            config.set("rewards", null);
            return this;
        }
        ConfigurationSection section = config.getConfigurationSection("rewards");
        if (section != null) {
            ConfigurationSection commandsSection = section.getConfigurationSection("commands");
            if (commandsSection != null) {
                for (String key : commandsSection.getKeys(false)) {
                    String command = commandsSection.getString(key);
                    if (command != null) {
                        commands.add(command);
                    }
                }
            }
            ConfigurationSection itemsSection = section.getConfigurationSection("items");
            if (itemsSection != null) {
                for (String key : itemsSection.getKeys(false)) {
                    ItemStack item = itemsSection.getItemStack(key);
                    if (item != null && item.getType() != Material.AIR) {
                        items.add(item);
                    }
                }
            }
            String rawLootType = section.getString("lootType", LootType.DEFAULT.name());
            if (rawLootType != null) {
                try {
                    lootType = LootType.valueOf(rawLootType);
                } catch (IllegalArgumentException e) {
                    lootType = LootType.DEFAULT;
                }
            } else {
                lootType = LootType.DEFAULT;
            }
        }
        return this;
    }
}
