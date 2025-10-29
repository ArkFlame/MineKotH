package com.arkflame.minekoth.koth.rewards;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.logging.Level;

/**
 * Manages the ItemStack collection for rewards, with robust serialization,
 * cloning, and error handling.
 */
public class RewardItems {
    private final Collection<ItemStack> items;

    public RewardItems() {
        this.items = new ArrayList<>();
    }

    /**
     * Returns the collection of reward items.
     *
     * @return A collection of ItemStacks.
     */
    public Collection<ItemStack> getItems() {
        Collection<ItemStack> items = new HashSet<>();
        for (ItemStack item : this.items) {
            items.add(item.clone());
        }
        return items;
    }

    /**
     * Adds a clone of an item to the collection if it is not null or AIR.
     * Cloning ensures the internal list holds a unique instance.
     *
     * @param item The ItemStack to add.
     */
    public void addItem(ItemStack item) {
        if (item != null && item.getType() != Material.AIR) {
            this.items.add(item.clone());
        }
    }

    /**
     * Clears all items from the collection.
     */
    public void clearItems() {
        this.items.clear();
    }

    /**
     * Saves the items into the provided configuration section. Each item is
     * cloned before saving and wrapped in a try-catch block to fail gracefully.
     *
     * @param section The ConfigurationSection where items will be saved.
     */
    public void save(ConfigurationSection section) {
        section.set("items", null);
        int i = 0;
        for (ItemStack item : items) {
            if (item == null || item.getType() == Material.AIR || item.getAmount() == 0) {
                continue;
            }
            try {
                // Clone the item before saving to prevent modification issues
                section.set("items." + i++, item.clone());
            } catch (Exception e) {
                Bukkit.getLogger().log(Level.SEVERE, "Failed to save a reward item: " + item.toString(), e);
            }
        }
    }

    /**
     * Loads items from a configuration section. Each item is loaded in a
     * try-catch block to gracefully handle corrupted or invalid data.
     *
     * @param section The ConfigurationSection to load items from.
     */
    public void load(ConfigurationSection section) {
        this.clearItems();
        if (section == null) {
            return;
        }

        ConfigurationSection itemsSection = section.getConfigurationSection("items");
        if (itemsSection != null) {
            for (String key : itemsSection.getKeys(false)) {
                try {
                    ItemStack item = itemsSection.getItemStack(key);
                    // The addItem method handles cloning and null/AIR checks
                    this.addItem(item);
                } catch (Exception e) {
                    Bukkit.getLogger().log(Level.SEVERE, "Failed to load reward item with key '" + key + "' from configuration.", e);
                }
            }
        }
    }
}