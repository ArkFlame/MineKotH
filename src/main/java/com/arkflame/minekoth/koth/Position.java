package com.arkflame.minekoth.koth;

import org.bukkit.ChatColor;
import org.bukkit.Location;

public class Position {
    private double x;
    private double y;
    private double z;

    public Position(double x, double y, double z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public Position(Location pos) {
        this.x = pos.getX();
        this.y = pos.getY();
        this.z = pos.getZ();
    }

    public double getX() { return x; }
    public void setX(double x) { this.x = x; }

    public double getY() { return y; }
    public void setY(double y) { this.y = y; }

    public double getZ() { return z; }
    public void setZ(double z) { this.z = z; }

    public int getArea(Position secondPosition) {
        double x = Math.abs(this.x - secondPosition.getX());
        double y = 1 + Math.abs(this.y - secondPosition.getY());
        double z = Math.abs(this.z - secondPosition.getZ());
        return (int) (x * y * z);
    }

    public int getXLength(Position second) {
        return (int) Math.abs(this.x - second.getX());
    }

    public int getZLength(Position second) {
        return (int) Math.abs(this.z - second.getZ());
    }
}