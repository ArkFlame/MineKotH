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
        // Initialize all random events with their chances from config
        addRandomEvent(new LootDropEvent(config.getLootDropChance(), config.getLootDropItems(), config.getLootDropCount())); // Loot drop event chance from config
        addRandomEvent(new ZombieHordeEvent(config.getZombieHordeChance(), config.getZombieHordeSpawnCount())); // Zombie horde event chance from config
        addRandomEvent(new LightningStrikeEvent(config.getLightningStrikeChance(), config.getLightningStrikeDamage(), config.getLightningStrikeFireTicks())); // Lightning strike event chance from config
        addRandomEvent(new SkeletonHordeEvent(config.getSkeletonHordeChance(), config.getSkeletonHordeSpawnCount())); // Skeleton horde event chance from config
        addRandomEvent(new PotionEffectEvent(config.getPotionEffectChance(), config.getPotionEffects(), config.getPotionEffectMin(), config.getPotionEffectMax())); // Potion effect event chance from config
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
