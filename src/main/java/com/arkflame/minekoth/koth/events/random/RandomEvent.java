package com.arkflame.minekoth.koth.events.random;

import com.arkflame.minekoth.koth.events.KothEvent;

public abstract class RandomEvent {
    private final double chance; // Chance per tick (0.0 to 1.0)
    private final String eventName;

    public RandomEvent(String eventName, double chance) {
        this.eventName = eventName;
        this.chance = chance;
    }

    // Method to be implemented by specific events
    public abstract void execute(KothEvent event);

    public double getChance() {
        return chance;
    }

    public String getEventName() {
        return eventName;
    }
}