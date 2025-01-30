package com.arkflame.minekoth.koth.events.random;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Zombie;
import org.bukkit.inventory.ItemStack;

import com.arkflame.minekoth.koth.events.KothEvent;
import com.arkflame.minekoth.utils.FoliaAPI;
import com.arkflame.minekoth.utils.Sounds;
import com.arkflame.minekoth.utils.Titles;

import java.util.Random;

public class ZombieHordeEvent extends RandomEvent {
    private final int minZombies = 3;
    private final int maxZombies = 8;

    public ZombieHordeEvent(double chance) {
        super("ZombieHorde", chance);
    }

    @Override
    public void execute(KothEvent event) {
        if (!hasPlayersInside(event))
            return;

        Location center = event.getKoth().getCenter();
        World world = center.getWorld();
        int zombieCount = new Random().nextInt(maxZombies - minZombies + 1) + minZombies;

        for (int i = 0; i < zombieCount; i++) {
            double offsetX = (Math.random() - 0.5) * 10;
            double offsetZ = (Math.random() - 0.5) * 10;
            Location spawnLoc = center.clone().add(offsetX, 0, offsetZ);

            FoliaAPI.runTaskForRegion(spawnLoc, () -> {
                Zombie zombie = (Zombie) world.spawnEntity(spawnLoc, EntityType.ZOMBIE);
                zombie.setCustomName(ChatColor.RED + "KOTH Zombie");
                zombie.setCustomNameVisible(true);

                // Give them basic equipment
                zombie.getEquipment().setHelmet(new ItemStack(Material.IRON_HELMET));
                zombie.getEquipment().setChestplate(new ItemStack(Material.IRON_CHESTPLATE));
            });
        }

        Sounds.play(world, center, 1.0f, 1.0f, "ENTITY_ZOMBIE_AMBIENT");
        Titles.sendTitle(event.getPlayersInZone(), "§cZombie Horde", "§7A horde of zombies has appeared!", 5, 20, 5);
    }

    private boolean hasPlayersInside(KothEvent event) {
        return !event.getPlayersInZone().isEmpty();
    }
}