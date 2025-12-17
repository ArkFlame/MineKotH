package com.arkflame.minekoth.koth.events.random;

import java.util.Random;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import com.arkflame.minekoth.MineKoth;
import com.arkflame.minekoth.koth.events.KothEvent;
import com.arkflame.minekoth.particles.ParticleUtil;
import com.arkflame.minekoth.utils.FoliaAPI;
import com.arkflame.minekoth.utils.Sounds;
import com.arkflame.minekoth.utils.Titles;

public class ItemRainEvent extends RandomEvent {
    private final ItemStack itemToDrop;
    private final int amount;
    private final double radius;
    private final double altitude;

    public ItemRainEvent(double chance, String itemConfig, int amount, double radius, double altitude) {
        super("ItemRain", chance);
        this.itemToDrop = parseItem(itemConfig);
        this.amount = amount;
        this.radius = radius;
        this.altitude = altitude;
    }

    @Override
    public void execute(KothEvent event) {
        Location center = event.getKoth().getCenter();
        World world = center.getWorld();
        Random rand = new Random();

        for (int i = 0; i < amount; i++) {
            // Calculate random position within cylinder above center
            // Sqrt of random ensures uniform distribution within the circle
            double r = radius * Math.sqrt(rand.nextDouble());
            double theta = rand.nextDouble() * 2 * Math.PI;

            double offsetX = r * Math.cos(theta);
            double offsetZ = r * Math.sin(theta);

            Location dropLocation = center.clone().add(offsetX, altitude, offsetZ);

            FoliaAPI.runTaskForRegion(dropLocation, () -> {
                world.dropItemNaturally(dropLocation, itemToDrop.clone());
                ParticleUtil.generateCircle(dropLocation, "CLOUD", 0.5, 5);
                // Play sound with slight pitch variation
                Sounds.play(dropLocation, 0.5f, 1.0f + (rand.nextFloat() * 0.5f), "ENTITY_CHICKEN_EGG");
            });
        }

        // Notify players
        for (Player player : event.getPlayersInZone()) {
            Titles.sendTitle(player,
                    MineKoth.getInstance().getLangManager().getLang(player).getMessage("messages.item-rain-title"),
                    MineKoth.getInstance().getLangManager().getLang(player).getMessage("messages.item-rain-subtitle"),
                    5, 20, 5);
        }
    }

    private ItemStack parseItem(String itemConfig) {
        // Formats: "MATERIAL" or "MATERIAL: AMOUNT" (Amount inside stack)
        String[] parts = itemConfig.split(": ");
        Material material = Material.matchMaterial(parts[0]);
        if (material == null) {
            material = Material.DIRT; // Fallback
        }
        
        int stackAmount = 1;
        if (parts.length > 1) {
            try {
                stackAmount = Integer.parseInt(parts[1]);
            } catch (NumberFormatException ignored) {}
        }
        
        return new ItemStack(material, stackAmount);
    }
}