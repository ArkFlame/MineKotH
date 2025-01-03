package com.arkflame.minekoth.koth;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

import org.bukkit.Material;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

public class Rewards {
    private Collection<ItemStack> items;
    private Collection<String> commands;

    public Rewards(Collection<String> commands, ItemStack[] itemsArray) {
        this.items = new ArrayList<>();
        this.commands = new ArrayList<>();
        
        if (commands != null) {
            this.commands.addAll(commands);
        }
        
        if (itemsArray != null) {
            Collections.addAll(this.items, itemsArray);
        }
    }

    public Rewards() {
        this.items = new ArrayList<>();
        this.commands = new ArrayList<>();
    }

    public Collection<ItemStack> getRewardsItems() {
        return items;
    }

    public void updateRewardsItems(Inventory inventory) {
        items.clear();
        for (ItemStack item : inventory.getContents()) {
            if (item != null && item.getType() != Material.AIR) {
                items.add(item);
            }
        }
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
}
