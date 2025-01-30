package com.arkflame.minekoth.koth.events.random;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import com.arkflame.minekoth.koth.events.KothEvent;

public class RandomEventsManager {
    private final List<RandomEvent> randomEvents;
    private final Random random;

    public RandomEventsManager() {
        this.randomEvents = new ArrayList<>();
        this.random = new Random();
        initializeEvents();
    }

    private void initializeEvents() {
        // Initialize all random events with their chances
        addRandomEvent(new LootDropEvent(0.001)); // 0.1% chance per tick
        addRandomEvent(new ZombieHordeEvent(0.0008)); // 0.08% chance per tick
        addRandomEvent(new LightningStrikeEvent(0.0005)); // 0.05% chance per tick
        addRandomEvent(new SkeletonHordeEvent(0.0008)); // 0.08% chance per tick
        addRandomEvent(new PotionEffectEvent(0.001)); // 0.1% chance per tick
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
