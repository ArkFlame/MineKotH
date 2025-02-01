package com.arkflame.minekoth.koth;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import com.arkflame.minekoth.utils.FoliaAPI;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.stream.Collectors;

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

    public int giveRewards(Player topPlayer) {
        if (lootType == LootType.DEFAULT || lootType == LootType.MINECLANS_DEFAULT) {

            FoliaAPI.runTask(() -> {
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
            });

            return getRewardsItems().size() + getRewardsCommands().size();
        } else if (lootType == LootType.RANDOM || lootType == LootType.MINECLANS_RANDOM) {
            // Combine items and commands into a single collection
            ArrayList<Object> rewardsPool = new ArrayList<>();
            rewardsPool.addAll(getRewardsItems());
            rewardsPool.addAll(getRewardsCommands());

            // Shuffle and pick random rewards
            Collections.shuffle(rewardsPool);
            int rewardsToGive = Math.min(lootAmount, rewardsPool.size());

            FoliaAPI.runTask(() -> {
                for (int i = 0; i < rewardsToGive; i++) {
                    Object reward = rewardsPool.get(i);

                    if (reward instanceof ItemStack) {
                        ItemStack item = (ItemStack) reward;
                        if (item != null && item.getType() != Material.AIR && item.getAmount() > 0) {
                            topPlayer.getInventory().addItem(item);
                        }
                    } else if (reward instanceof String) {
                        String command = (String) reward;
                        Bukkit.dispatchCommand(Bukkit.getConsoleSender(),
                                command.replace("%player%", topPlayer.getName()));
                    }
                }
            });

            return rewardsToGive;
        }

        return 0;
    }

    public String serialize() {
        String serializedItems = items.stream()
                .map(item -> item.getType().name() + ":" + item.getAmount())
                .collect(Collectors.joining(","));
        String serializedCommands = String.join(",", commands);
        return lootType.name() + ";" + lootAmount + ";" + serializedItems + ";" + serializedCommands;
    }

    public static Rewards deserialize(String serializedData) {
        String[] parts = serializedData.split(";");
        LootType lootType = LootType.valueOf(parts[0]);
        int lootAmount = Integer.parseInt(parts[1]);

        Collection<ItemStack> items = new ArrayList<>();
        if (parts.length > 2 && !parts[2].isEmpty()) {
            String[] itemsArray = parts[2].split(",");
            for (String itemData : itemsArray) {
                String[] itemParts = itemData.split(":");
                ItemStack item = new ItemStack(Material.valueOf(itemParts[0]), Integer.parseInt(itemParts[1]));
                items.add(item);
            }
        }

        Collection<String> commands = new ArrayList<>();
        if (parts.length > 3 && !parts[3].isEmpty()) {
            String[] commandsArray = parts[3].split(",");
            Collections.addAll(commands, commandsArray);
        }

        return new Rewards(commands, items.toArray(new ItemStack[0]), lootType, lootAmount);
    }
}
