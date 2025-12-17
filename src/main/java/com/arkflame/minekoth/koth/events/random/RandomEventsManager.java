package com.arkflame.minekoth.koth.events.random;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import com.arkflame.minekoth.MineKoth;
import com.arkflame.minekoth.koth.events.KothEvent;

public class RandomEventsManager {
    private final List<RandomEvent> randomEvents;
    private final Random random;
    private final RandomEventConfig config;

    public RandomEventsManager() {
        this.randomEvents = new ArrayList<>();
        this.random = new Random();
        this.config = new RandomEventConfig(MineKoth.getInstance().getConfig());
        initializeEvents();
    }

    private void initializeEvents() {
        // Initialize random events if they are enabled in config

        if (config.isLootDropEnabled()) {
            addRandomEvent(new LootDropEvent(config.getLootDropChance(), config.getLootDropItems(), config.getLootDropCount()));
        }

        if (config.isZombieHordeEnabled()) {
            addRandomEvent(new ZombieHordeEvent(config.getZombieHordeChance(), config.getZombieHordeSpawnCount()));
        }

        if (config.isLightningStrikeEnabled()) {
            addRandomEvent(new LightningStrikeEvent(config.getLightningStrikeChance(), config.getLightningStrikeDamage(), config.getLightningStrikeFireTicks()));
        }

        if (config.isSkeletonHordeEnabled()) {
            addRandomEvent(new SkeletonHordeEvent(config.getSkeletonHordeChance(), config.getSkeletonHordeSpawnCount()));
        }

        if (config.isPotionEffectEnabled()) {
            addRandomEvent(new PotionEffectEvent(config.getPotionEffectChance(), config.getPotionEffects(), config.getPotionEffectMin(), config.getPotionEffectMax()));
        }

        if (config.isItemRainEnabled()) {
            addRandomEvent(new ItemRainEvent(
                config.getItemRainChance(), 
                config.getItemRainItem(), 
                config.getItemRainAmount(), 
                config.getItemRainRadius(), 
                config.getItemRainAltitude()
            ));
        }
    }

    public void addRandomEvent(RandomEvent event) {
        randomEvents.add(event);
    }

    // Called every tick to potentially trigger events
    public void tick(KothEvent event) {
        if (event == null) return;

        for (RandomEvent randomEvent : randomEvents) {
            if (random.nextDouble() <= randomEvent.getChance()) {
                randomEvent.execute(event);
            }
        }
    }
}