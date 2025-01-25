package com.arkflame.minekoth.koth;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import com.arkflame.minekoth.utils.FoliaAPI;

public class Rewards {
    public enum LootType {
        // Give all items to the winner
        DEFAULT,
        // Give a random item to the winner
        RANDOM
    }

    private Collection<ItemStack> items;
    private Collection<String> commands;
    private LootType lootType;
    private int lootAmount;

    public Rewards(Collection<String> commands, ItemStack[] itemsArray, LootType lootType, int lootAmount) {
        this.items = new ArrayList<>();
        this.commands = new ArrayList<>();
        
        if (commands != null) {
            this.commands.addAll(commands);
        }
        
        if (itemsArray != null) {
            Collections.addAll(this.items, itemsArray);
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

    public void giveRewards(Player topPlayer) {
        System.out.println("Giving rewards to " + topPlayer.getName());
        FoliaAPI.runTask(() -> {
            if (lootType == LootType.DEFAULT) {
                System.out.println("Give all rewards to " + topPlayer.getName());
                // Execute all reward commands
                for (String command : getRewardsCommands()) {
                    Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command.replace("%player%", topPlayer.getName()));
                }
                // Give all reward items
                for (ItemStack item : getRewardsItems()) {
                    if (item != null && item.getType() != Material.AIR && item.getAmount() > 0) {
                        topPlayer.getInventory().addItem(item);
                    }
                }
            } else if (lootType == LootType.RANDOM) {
                System.out.println("Give random rewards to " + topPlayer.getName());
                // Combine items and commands into a single collection
                ArrayList<Object> rewardsPool = new ArrayList<>();
                rewardsPool.addAll(getRewardsItems());
                rewardsPool.addAll(getRewardsCommands());
    
                // Shuffle and pick random rewards
                Collections.shuffle(rewardsPool);
                int rewardsToGive = Math.min(lootAmount, rewardsPool.size());
    
                System.out.println("Giving " + rewardsToGive + " rewards to " + topPlayer.getName());

                for (int i = 0; i < rewardsToGive; i++) {
                    Object reward = rewardsPool.get(i);
    
                    if (reward instanceof ItemStack) {
                        ItemStack item = (ItemStack) reward;
                        if (item != null && item.getType() != Material.AIR && item.getAmount() > 0) {
                            topPlayer.getInventory().addItem(item);
                            System.out.println("Giving " + item.getAmount() + " " + item.getType().name() + " to " + topPlayer.getName());
                        } else{
                            System.out.println("Item is null");
                        }
                    } else if (reward instanceof String) {
                        String command = (String) reward;
                        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command.replace("%player%", topPlayer.getName()));
                        System.out.println("Executing command: " + command);
                    } else {
                        System.out.println("Unknown reward type: " + reward == null ? "null" : reward.getClass().getName());
                    }
                }
            }
        });
    }    
}
