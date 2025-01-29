package com.arkflame.minekoth.setup.session;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.bukkit.Location;
import org.bukkit.inventory.ItemStack;

import com.arkflame.minekoth.koth.Koth;
import com.arkflame.minekoth.koth.Position;
import com.arkflame.minekoth.koth.Rewards.LootType;

public class SetupSession {
    private int id = -1;

    private String name;
    private String times;
    private String days;
    private Position firstPosition;
    private Position secondPosition;
    private int timeLimit;
    private int captureTime;
    private ItemStack[] rewards;
    private List<String> rewardsCommands = new ArrayList<>();
    private LootType lootType;
    private int lootAmount = -1;
    private String worldName;

    private boolean editing = false;

    public SetupSession() {
    }

    public SetupSession(Koth koth) {
        this.id = koth.getId();
        this.name = koth.getName();
        this.worldName = koth.getWorldName();
        this.firstPosition = new Position(koth.getFirstPosition());
        this.secondPosition = new Position(koth.getSecondPosition());
        this.timeLimit = koth.getTimeLimit();
        this.captureTime = koth.getTimeToCapture();
        this.rewards = koth.getRewards().getRewardsItems().toArray(new ItemStack[0]);
        this.times = koth.getTimes();
        this.days = koth.getDays();
        this.lootType = koth.getRewards().getLootType();

        this.editing = true;
    }

    public SetupSession(int id) {
        this.id = id;
    }

    // Getters
    public String getName() {
        return name;
    }

    public String getTimes() {
        return times;
    }

    public Position getFirstPosition() {
        return firstPosition;
    }

    public Position getSecondPosition() {
        return secondPosition;
    }

    public int getTimeLimit() {
        return timeLimit;
    }

    public int getCaptureTime() {
        return captureTime;
    }

    public ItemStack[] getRewards() {
        return rewards;
    }

    public List<String> getRewardsCommands() {
        return rewardsCommands;
    }

    public LootType getLootType() {
        return lootType;
    }

    public int getLootAmount() {
        return lootAmount <= 0 ? 1 : lootAmount;
    }

    // Setters and other methods
    public boolean isNameSet() {
        return name != null;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isTimesSet() {
        return times != null;
    }

    public void setTimes(String times) {
        this.times = times;
    }

    public boolean isFirstPositionSet() {
        return firstPosition != null;
    }

    public void setFirstPosition(Position position) {
        this.firstPosition = position;
    }

    public boolean isSecondPositionSet() {
        return secondPosition != null;
    }

    public void setSecondPosition(Position position) {
        this.secondPosition = position;
    }

    public boolean isTimeLimitSet() {
        return timeLimit > 0;
    }

    public void setTimeLimit(int timeLimit) {
        this.timeLimit = timeLimit;
    }

    public boolean isCaptureTimeSet() {
        return captureTime > 0;
    }

    public void setCaptureTime(int captureTime) {
        this.captureTime = captureTime;
    }

    public boolean isRewardsSet() {
        return rewards != null;
    }

    public boolean isComplete() {
        return isNameSet() && isTimesSet() && isFirstPositionSet() && isSecondPositionSet() && isTimeLimitSet()
                && isCaptureTimeSet() && isRewardsSet() && isDaysSet() && isLootTypeSet() && isLootAmountSet();
    }

    public void setRewards(ItemStack[] rewards) {
        this.rewards = rewards;
    }

    public void addRewardsCommand(Collection<String> commands) {
        this.rewardsCommands.addAll(commands);
    }

    public boolean isDaysSet() {
        return days != null;
    }

    public void setDays(String message) {
        this.days = message;
    }

    public String getDays() {
        return days;
    }

    public int getId() {
        return id;
    }

    public boolean isValidTimes(String message) {
        if (message == null || message.isEmpty()) {
            return false;
        }

        // Regular expression to match 12-hour (with am/pm), 24-hour time formats, and
        // multiple times separated by commas
        String timePattern = "^(?:(1[0-2]|[1-9])(am|pm)|([01]?[0-9]|2[0-3]):([0-5]?[0-9])|(24:00))(,\\s?(?:(1[0-2]|[1-9])(am|pm)|([01]?[0-9]|2[0-3]):([0-5]?[0-9])|(24:00)))*$";
        return message.toLowerCase().matches(timePattern);
    }

    public boolean isValidTimeLimit(String message) {
        if (message == null || message.isEmpty()) {
            return false;
        }

        // Regular expression to match valid time limits: number followed by s, m, h, or
        // their variations
        String timeLimitPattern = "^\\d+\\s?(s|sec|m|min|h|hour|hours|minute)$";
        return message.toLowerCase().matches(timeLimitPattern);
    }

    public boolean isValidCaptureTime(String message) {
        if (message == null || message.isEmpty()) {
            return false;
        }

        // Regular expression to match valid time limits: number followed by s, m, h, or
        // their variations
        String timeLimitPattern = "^\\d+\\s?(s|sec|m|min|h|hour|hours|minute)$";
        return message.toLowerCase().matches(timeLimitPattern);
    }

    public boolean isValidDays(String message) {
        if (message == null || message.isEmpty()) {
            return false;
        }

        // Regular expression to match valid days of the week or "ALL", with optional
        // commas and spaces
        String daysPattern = "^(MONDAY|TUESDAY|WEDNESDAY|THURSDAY|FRIDAY|SATURDAY|SUNDAY|ALL)(,\\s?(MONDAY|TUESDAY|WEDNESDAY|THURSDAY|FRIDAY|SATURDAY|SUNDAY|ALL))*$";
        return message.toUpperCase().matches(daysPattern);
    }

    public boolean isLootTypeSet() {
        return lootType != null;
    }

    public boolean isValidLootType(String message) {
        if (message == null || message.isEmpty()) {
            return false;
        }

        try {
            LootType.valueOf(message.toUpperCase());
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    public void setLootType(String message) {
        if (message == null) {
            this.lootType = null;
        } else if (isValidLootType(message)) {
            this.lootType = LootType.valueOf(message.toUpperCase());
        } else {
            throw new IllegalArgumentException("Invalid loot type: " + message);
        }
    }

    public boolean isLootAmountSet() {
        return lootAmount > -1 || getLootType() == LootType.DEFAULT;
    }

    public boolean isValidLootAmount(String message) {
        if (message == null || message.isEmpty()) {
            return false;
        }

        try {
            int amount = Integer.parseInt(message);
            return amount > -1;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    public void setLootAmount(int amount) {
        if (amount > -1) {
            this.lootAmount = amount;
        } else {
            throw new IllegalArgumentException("Loot amount must be greater than 0");
        }
    }

    public void unsetLootAmount() {
        this.lootAmount = -1;
    }

    public String getWorldName() {
        return worldName;
    }

    public void setWorldName(String worldName) {
        this.worldName = worldName;
    }

    public void setRewardsCommands(List<String> rewardsCommands) {
        this.rewardsCommands = rewardsCommands;
    }

    public boolean isRewardsCommandsSet() {
        return this.rewardsCommands != null && !this.rewardsCommands.isEmpty();
    }

    public boolean isEditing() {
        return editing;
    }

    public boolean isValidPosition(Location loc) {
        return loc != null && loc.getWorld().getName().equals(worldName);
    }
}
