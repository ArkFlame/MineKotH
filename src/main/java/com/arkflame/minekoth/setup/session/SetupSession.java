package com.arkflame.minekoth.setup.session;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import org.bukkit.inventory.ItemStack;

import com.arkflame.minekoth.koth.Position;

public class SetupSession {
    private String name;
    private String times;
    private Position firstPosition;
    private Position secondPosition;
    private int timeLimit;
    private int captureTime;
    private ItemStack[] rewards;
    private List<String> rewardsCommands = new ArrayList<>();

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
        return isNameSet() && isTimesSet() && isFirstPositionSet() && isSecondPositionSet() && isTimeLimitSet() && isCaptureTimeSet() && isRewardsSet();
    }

    public void setRewards(ItemStack[] rewards) {
        this.rewards = rewards;
    }

    public void addRewardsCommand(Collection<String> commands) {
        this.rewardsCommands.addAll(commands);
    }
}
