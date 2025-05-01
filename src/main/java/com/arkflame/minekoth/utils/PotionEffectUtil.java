package com.arkflame.minekoth.utils;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Utility class for handling potion effects with version compatibility.
 * Allows applying effects using multiple possible names to support different
 * Bukkit versions.
 */
public final class PotionEffectUtil {

    private PotionEffectUtil() {
        // Private constructor to prevent instantiation of utility class
    }

    /**
     * Attempts to apply the first valid potion effect from the given names to a
     * player.
     * 
     * @param player        The player to receive the effect
     * @param amplifier     The amplifier level of the effect (0 = level 1)
     * @param durationTicks Duration of the effect in ticks (20 ticks = 1 second)
     * @param names         Variable number of possible effect names to try
     * @return true if any effect was successfully applied, false otherwise
     */
    public static boolean tryApplyEffect(Player player, int amplifier, int durationTicks, String... names) {
        if (player == null || names == null || names.length == 0) {
            return false;
        }

        PotionEffectType effectType = findFirstValidEffect(names);
        if (effectType != null) {
            applyEffect(player, effectType, amplifier, durationTicks);
            return true;
        }
        return false;
    }

    /**
     * Applies all valid potion effects from the given names to a player.
     * 
     * @param player        The player to receive the effects
     * @param amplifier     The amplifier level for all effects (0 = level 1)
     * @param durationTicks Duration for all effects in ticks (20 ticks = 1 second)
     * @param names         Variable number of possible effect names to apply
     * @return The number of effects successfully applied
     */
    public static int applyAllValidEffects(Player player, int amplifier, int durationTicks, String... names) {
        if (player == null || names == null || names.length == 0) {
            return 0;
        }

        List<PotionEffectType> validEffects = findAllValidEffects(names);
        if (!validEffects.isEmpty()) {
            validEffects.forEach(effect -> applyEffect(player, effect, amplifier, durationTicks));
        }
        return validEffects.size();
    }

    public static void removeAllValidEffects(Player player, String... effects) {
        if (player == null || effects == null || effects.length == 0) {
            return;
        }

        List<PotionEffectType> validEffects = findAllValidEffects(effects);
        if (!validEffects.isEmpty()) {
            FoliaAPI.runTaskForEntity(player, () -> {
                validEffects.forEach(effect -> player.removePotionEffect(effect));
            });
        }
    }

    /**
     * Finds the first valid PotionEffectType from the given names.
     * 
     * @param names Array of effect names to try
     * @return The first valid PotionEffectType found, or null if none are valid
     */
    private static PotionEffectType findFirstValidEffect(String... names) {
        return Arrays.stream(names)
                .filter(Objects::nonNull)
                .map(PotionEffectUtil::getPotionEffectType)
                .filter(Objects::nonNull)
                .findFirst()
                .orElse(null);
    }

    /**
     * Finds all valid PotionEffectTypes from the given names.
     * 
     * @param names Array of effect names to check
     * @return List of valid PotionEffectTypes
     */
    private static List<PotionEffectType> findAllValidEffects(String... names) {
        return Arrays.stream(names)
                .filter(Objects::nonNull)
                .map(PotionEffectUtil::getPotionEffectType)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    /**
     * Safely attempts to get a PotionEffectType from a name.
     * 
     * @param name The name of the effect to try
     * @return The PotionEffectType if valid, null otherwise
     */
    private static PotionEffectType getPotionEffectType(String name) {
        try {
            if (name != null) {
                return PotionEffectType.getByName(name.toUpperCase());
            }
        } catch (Exception e) {
            // Skip
        }
        return null;
    }

    /**
     * Applies a potion effect to a player with the specified parameters.
     * 
     * @param player        The player to receive the effect
     * @param effectType    The type of effect to apply
     * @param amplifier     The amplifier level (0 = level 1)
     * @param durationTicks Duration in ticks (20 ticks = 1 second)
     */
    private static void applyEffect(Player player, PotionEffectType effectType, int amplifier, int durationTicks) {
        FoliaAPI.runTaskForEntity(player, () -> {
            PotionEffect effect = new PotionEffect(effectType, durationTicks, amplifier);
            player.addPotionEffect(effect, true);
        }, () -> {
        }, 1L);
    }

    public static boolean removeEffect(Player player, PotionEffectType effect) {
        if (player.hasPotionEffect(effect)) {
            FoliaAPI.runTaskForRegion(player.getLocation(), () -> {
                player.removePotionEffect(effect);
            });
            return true;
        }
        return false;
    }

    public static List<PotionEffect> readEffectsFromConfig(ConfigurationSection section) {
        // Read from the configuration and create effects
        List<PotionEffect> effects = section.getKeys(false).stream()
                .map(key -> {
                    String type = section.getString(key + ".type");
                    int amplifier = section.getInt(key + ".amplifier");
                    int duration = section.getInt(key + ".duration");
                    PotionEffectType effectType = getPotionEffectType(type);
                    if (effectType != null) {
                        return new PotionEffect(effectType, duration, amplifier);
                    }
                    return null;
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        return effects;
    }
}