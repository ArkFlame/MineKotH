package com.arkflame.minekoth.player;

/**
 * Represents a statistical value.
 */
public class StatValue {
    private Number value;

    public StatValue(Number initialValue) {
        this.value = initialValue;
    }

    public Number getValue() {
        return value;
    }

    public void setValue(Number value) {
        this.value = value;
    }

    public void add(Number amount) {
        if (value instanceof Integer) {
            value = value.intValue() + amount.intValue();
        } else if (value instanceof Long) {
            value = value.longValue() + amount.longValue();
        } else if (value instanceof Double) {
            value = value.doubleValue() + amount.doubleValue();
        } else {
            // Handle other Number types if necessary.
        }
    }
}
