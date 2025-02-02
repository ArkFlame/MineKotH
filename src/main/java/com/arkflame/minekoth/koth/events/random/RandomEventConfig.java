package com.arkflame.minekoth.koth.events.random;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.List;

public class RandomEventConfig {

    private double lootDropChance;
    private List<String> lootDropItems;
    private int lootDropCount;

    private double zombieHordeChance;
    private int zombieHordeSpawnCount;

    private double lightningStrikeChance;
    private int lightningStrikeDamage;
    private int lightningStrikeFireTicks;

    private double skeletonHordeChance;
    private int skeletonHordeSpawnCount;

    private double potionEffectChance;
    private List<String> potionEffects;
    private int potionEffectMin;
    private int potionEffectMax;

    public RandomEventConfig(FileConfiguration config) {
        loadConfig(config);
    }

    private void loadConfig(FileConfiguration config) {
        ConfigurationSection randomEventsSection = config.getConfigurationSection("random-events");

        if (randomEventsSection == null) return;

        // Load loot-drop event
        ConfigurationSection lootDropSection = randomEventsSection.getConfigurationSection("loot-drop");
        if (lootDropSection != null) {
            lootDropChance = lootDropSection.getDouble("chance", 0.001);
            lootDropItems = lootDropSection.getStringList("loot");
            lootDropCount = lootDropSection.getInt("drop-count", 5);
        }

        // Load zombie-horde event
        ConfigurationSection zombieHordeSection = randomEventsSection.getConfigurationSection("zombie-horde");
        if (zombieHordeSection != null) {
            zombieHordeChance = zombieHordeSection.getDouble("chance", 0.0008);
            zombieHordeSpawnCount = zombieHordeSection.getInt("spawn-count", 10);
        }

        // Load lightning-strike event
        ConfigurationSection lightningStrikeSection = randomEventsSection.getConfigurationSection("lightning-strike");
        if (lightningStrikeSection != null) {
            lightningStrikeChance = lightningStrikeSection.getDouble("chance", 0.0005);
            lightningStrikeDamage = lightningStrikeSection.getInt("damage", 6);
            lightningStrikeFireTicks = lightningStrikeSection.getInt("fire-ticks", 100);
        }

        // Load skeleton-horde event
        ConfigurationSection skeletonHordeSection = randomEventsSection.getConfigurationSection("skeleton-horde");
        if (skeletonHordeSection != null) {
            skeletonHordeChance = skeletonHordeSection.getDouble("chance", 0.0008);
            skeletonHordeSpawnCount = skeletonHordeSection.getInt("spawn-count", 10);
        }

        // Load potion-effect event
        ConfigurationSection potionEffectSection = randomEventsSection.getConfigurationSection("potion-effect");
        if (potionEffectSection != null) {
            potionEffectChance = potionEffectSection.getDouble("chance", 0.001);
            potionEffects = potionEffectSection.getStringList("effects");
            potionEffectMin = potionEffectSection.getInt("min-effects", 1);
            potionEffectMax = potionEffectSection.getInt("max-effects", 3);
        }
    }

    public double getLootDropChance() {
        return lootDropChance;
    }

    public List<String> getLootDropItems() {
        return lootDropItems;
    }

    public int getLootDropCount() {
        return lootDropCount;
    }

    public double getZombieHordeChance() {
        return zombieHordeChance;
    }

    public int getZombieHordeSpawnCount() {
        return zombieHordeSpawnCount;
    }

    public double getLightningStrikeChance() {
        return lightningStrikeChance;
    }

    public int getLightningStrikeDamage() {
        return lightningStrikeDamage;
    }

    public int getLightningStrikeFireTicks() {
        return lightningStrikeFireTicks;
    }

    public double getSkeletonHordeChance() {
        return skeletonHordeChance;
    }

    public int getSkeletonHordeSpawnCount() {
        return skeletonHordeSpawnCount;
    }

    public double getPotionEffectChance() {
        return potionEffectChance;
    }

    public List<String> getPotionEffects() {
        return potionEffects;
    }

    public int getPotionEffectMin() {
        return potionEffectMin;
    }

    public int getPotionEffectMax() {    
        return potionEffectMax;
    }
}
