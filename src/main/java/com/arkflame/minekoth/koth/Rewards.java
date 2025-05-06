package com.arkflame.minekoth.koth;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import com.arkflame.minekoth.utils.FoliaAPI;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;

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
    
    private static final String SECTION_SEPARATOR = "\u2588";
    private static final String ITEM_SEPARATOR = "\u25CB";
    private static final String KEY_VALUE_SEPARATOR = ":";
    private static final String META_SEPARATOR = "\u25B6";   
    
    public String serialize() {
        StringBuilder builder = new StringBuilder();
        
        // Serialize loot details
        builder.append(lootType.name())
              .append(KEY_VALUE_SEPARATOR)
              .append(lootAmount)
              .append(SECTION_SEPARATOR);
        
        // Serialize items with metadata
        if (!items.isEmpty()) {
            boolean first = true;
            for (ItemStack item : items) {
                if (!first) {
                    builder.append(ITEM_SEPARATOR);
                }
                first = false;
                
                // Basic item data
                builder.append(item.getType().name())
                      .append(KEY_VALUE_SEPARATOR)
                      .append(item.getAmount());
                
                // Item metadata
                if (item.hasItemMeta()) {
                    builder.append(META_SEPARATOR)
                          .append(serializeItemMeta(item.getItemMeta()));
                }
            }
        }
        builder.append(SECTION_SEPARATOR);
        
        // Serialize commands
        if (!commands.isEmpty()) {
            builder.append(String.join(ITEM_SEPARATOR, commands));
        }
        return builder.toString();
    }
    
    private String serializeItemMeta(ItemMeta meta) {
        StringBuilder metaBuilder = new StringBuilder();
        
        // Display name
        if (meta.hasDisplayName()) {
            metaBuilder.append("name").append(KEY_VALUE_SEPARATOR)
                      .append(meta.getDisplayName()
                        .replace(KEY_VALUE_SEPARATOR, "")
                        .replace(META_SEPARATOR, "")
                        .replace(ITEM_SEPARATOR, "")
                        .replace(SECTION_SEPARATOR, ""))
                      .append(META_SEPARATOR);
        }
        
        // Lore
        if (meta.hasLore()) {
            metaBuilder.append("lore").append(KEY_VALUE_SEPARATOR)
                      .append(String.join("\n", meta.getLore())
                        .replace(KEY_VALUE_SEPARATOR, "")
                        .replace(META_SEPARATOR, "")
                        .replace(ITEM_SEPARATOR, "")
                        .replace(SECTION_SEPARATOR, ""))
                      .append(META_SEPARATOR);
        }
        
        // Enchantments (1.8 compatible)
        if (meta.hasEnchants()) {
            for (Map.Entry<Enchantment, Integer> entry : meta.getEnchants().entrySet()) {
                metaBuilder.append("ench").append(KEY_VALUE_SEPARATOR)
                          .append(entry.getKey().getName())
                          .append(KEY_VALUE_SEPARATOR)
                          .append(entry.getValue())
                          .append(META_SEPARATOR);
            }
        }
        
        return metaBuilder.toString();
    }
    
    public static Rewards deserialize(String serializedData) {
        String[] sections = serializedData.split(SECTION_SEPARATOR, -1);
        
        // Deserialize loot details
        String[] lootDetails = sections[0].split(KEY_VALUE_SEPARATOR);
        if (lootDetails.length < 2) {
            return new Rewards();
        }
        LootType lootType = LootType.valueOf(lootDetails[0]);
        int lootAmount = Integer.parseInt(lootDetails[1]);
        
        // Deserialize items
        Collection<ItemStack> items = new ArrayList<>();
        if (sections.length > 1 && !sections[1].isEmpty()) {
            String[] itemEntries = sections[1].split(ITEM_SEPARATOR, -1);
            for (String entry : itemEntries) {
                String[] parts = entry.split(META_SEPARATOR, 2); // Split into base item and meta
                String[] itemData = parts[0].split(KEY_VALUE_SEPARATOR);
                
                if (itemData.length < 2) {
                    continue; // Skip invalid items
                }
                
                Material material = Material.valueOf(itemData[0]);
                int amount = Integer.parseInt(itemData[1]);
                ItemStack item = new ItemStack(material, amount);
                
                // Handle item metadata if present
                if (parts.length > 1 && !parts[1].isEmpty()) {
                    applyItemMeta(item, parts[1]);
                }
                
                items.add(item);
            }
        }
        
        // Deserialize commands
        Collection<String> commands = new ArrayList<>();
        if (sections.length > 2 && !sections[2].isEmpty()) {
            String[] commandEntries = sections[2].split(ITEM_SEPARATOR, -1);
            Collections.addAll(commands, commandEntries);
        }
        
        return new Rewards(commands, items.toArray(new ItemStack[0]), lootType, lootAmount);
    }
    
    private static void applyItemMeta(ItemStack item, String metaString) {
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return;
        
        String[] metaParts = metaString.split(META_SEPARATOR);
        for (String part : metaParts) {
            if (part.isEmpty()) continue;
            
            String[] keyValue = part.split(KEY_VALUE_SEPARATOR, 2);
            if (keyValue.length < 2) continue;
            
            switch (keyValue[0]) {
                case "name":
                    meta.setDisplayName(keyValue[1]);
                    break;
                case "lore":
                    meta.setLore(Arrays.asList(keyValue[1].split("\n")));
                    break;
                case "ench":
                    String[] enchData = keyValue[1].split(KEY_VALUE_SEPARATOR);
                    if (enchData.length == 2) {
                        Enchantment ench = Enchantment.getByName(enchData[0]);
                        if (ench != null) {
                            meta.addEnchant(ench, Integer.parseInt(enchData[1]), true);
                        }
                    }
                    break;
            }
        }
        
        item.setItemMeta(meta);
    }
}
