package com.arkflame.minekoth.koth.events.random;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.List;

public class RandomEventConfig {

    // Loot Drop
    private boolean lootDropEnabled;
    private double lootDropChance;
    private List<String> lootDropItems;
    private int lootDropCount;

    // Zombie Horde
    private boolean zombieHordeEnabled;
    private double zombieHordeChance;
    private int zombieHordeSpawnCount;

    // Lightning Strike
    private boolean lightningStrikeEnabled;
    private double lightningStrikeChance;
    private int lightningStrikeDamage;
    private int lightningStrikeFireTicks;

    // Skeleton Horde
    private boolean skeletonHordeEnabled;
    private double skeletonHordeChance;
    private int skeletonHordeSpawnCount;

    // Potion Effect
    private boolean potionEffectEnabled;
    private double potionEffectChance;
    private List<String> potionEffects;
    private int potionEffectMin;
    private int potionEffectMax;

    // Item Rain (New Event)
    private boolean itemRainEnabled;
    private double itemRainChance;
    private String itemRainItem; // e.g. "DIAMOND" or "DIAMOND: 1"
    private int itemRainAmount; // How many items drop
    private double itemRainRadius;
    private double itemRainAltitude;

    public RandomEventConfig(FileConfiguration config) {
        loadConfig(config);
    }

    private void loadConfig(FileConfiguration config) {
        ConfigurationSection randomEventsSection = config.getConfigurationSection("random-events");

        if (randomEventsSection == null) return;

        // Load loot-drop event
        ConfigurationSection lootDropSection = randomEventsSection.getConfigurationSection("loot-drop");
        if (lootDropSection != null) {
            lootDropEnabled = lootDropSection.getBoolean("enabled", true);
            lootDropChance = lootDropSection.getDouble("chance", 0.001);
            lootDropItems = lootDropSection.getStringList("loot");
            lootDropCount = lootDropSection.getInt("drop-count", 5);
        }

        // Load zombie-horde event
        ConfigurationSection zombieHordeSection = randomEventsSection.getConfigurationSection("zombie-horde");
        if (zombieHordeSection != null) {
            zombieHordeEnabled = zombieHordeSection.getBoolean("enabled", true);
            zombieHordeChance = zombieHordeSection.getDouble("chance", 0.0008);
            zombieHordeSpawnCount = zombieHordeSection.getInt("spawn-count", 10);
        }

        // Load lightning-strike event
        ConfigurationSection lightningStrikeSection = randomEventsSection.getConfigurationSection("lightning-strike");
        if (lightningStrikeSection != null) {
            lightningStrikeEnabled = lightningStrikeSection.getBoolean("enabled", true);
            lightningStrikeChance = lightningStrikeSection.getDouble("chance", 0.0005);
            lightningStrikeDamage = lightningStrikeSection.getInt("damage", 6);
            lightningStrikeFireTicks = lightningStrikeSection.getInt("fire-ticks", 100);
        }

        // Load skeleton-horde event
        ConfigurationSection skeletonHordeSection = randomEventsSection.getConfigurationSection("skeleton-horde");
        if (skeletonHordeSection != null) {
            skeletonHordeEnabled = skeletonHordeSection.getBoolean("enabled", true);
            skeletonHordeChance = skeletonHordeSection.getDouble("chance", 0.0008);
            skeletonHordeSpawnCount = skeletonHordeSection.getInt("spawn-count", 10);
        }

        // Load potion-effect event
        ConfigurationSection potionEffectSection = randomEventsSection.getConfigurationSection("potion-effect");
        if (potionEffectSection != null) {
            potionEffectEnabled = potionEffectSection.getBoolean("enabled", true);
            potionEffectChance = potionEffectSection.getDouble("chance", 0.001);
            potionEffects = potionEffectSection.getStringList("effects");
            potionEffectMin = potionEffectSection.getInt("min-effects", 1);
            potionEffectMax = potionEffectSection.getInt("max-effects", 3);
        }

        // Load item-rain event (New)
        ConfigurationSection itemRainSection = randomEventsSection.getConfigurationSection("item-rain");
        if (itemRainSection != null) {
            itemRainEnabled = itemRainSection.getBoolean("enabled", true);
            itemRainChance = itemRainSection.getDouble("chance", 0.001);
            itemRainItem = itemRainSection.getString("item", "GOLD_NUGGET");
            itemRainAmount = itemRainSection.getInt("amount", 20);
            itemRainRadius = itemRainSection.getDouble("radius", 5.0);
            itemRainAltitude = itemRainSection.getDouble("altitude", 3.0);
        }
    }

    // Getters for Loot Drop
    public boolean isLootDropEnabled() { return lootDropEnabled; }
    public double getLootDropChance() { return lootDropChance; }
    public List<String> getLootDropItems() { return lootDropItems; }
    public int getLootDropCount() { return lootDropCount; }

    // Getters for Zombie Horde
    public boolean isZombieHordeEnabled() { return zombieHordeEnabled; }
    public double getZombieHordeChance() { return zombieHordeChance; }
    public int getZombieHordeSpawnCount() { return zombieHordeSpawnCount; }

    // Getters for Lightning Strike
    public boolean isLightningStrikeEnabled() { return lightningStrikeEnabled; }
    public double getLightningStrikeChance() { return lightningStrikeChance; }
    public int getLightningStrikeDamage() { return lightningStrikeDamage; }
    public int getLightningStrikeFireTicks() { return lightningStrikeFireTicks; }

    // Getters for Skeleton Horde
    public boolean isSkeletonHordeEnabled() { return skeletonHordeEnabled; }
    public double getSkeletonHordeChance() { return skeletonHordeChance; }
    public int getSkeletonHordeSpawnCount() { return skeletonHordeSpawnCount; }

    // Getters for Potion Effect
    public boolean isPotionEffectEnabled() { return potionEffectEnabled; }
    public double getPotionEffectChance() { return potionEffectChance; }
    public List<String> getPotionEffects() { return potionEffects; }
    public int getPotionEffectMin() { return potionEffectMin; }
    public int getPotionEffectMax() { return potionEffectMax; }

    // Getters for Item Rain
    public boolean isItemRainEnabled() { return itemRainEnabled; }
    public double getItemRainChance() { return itemRainChance; }
    public String getItemRainItem() { return itemRainItem; }
    public int getItemRainAmount() { return itemRainAmount; }
    public double getItemRainRadius() { return itemRainRadius; }
    public double getItemRainAltitude() { return itemRainAltitude; }
}