package com.arkflame.minekoth.koth.events.random;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Skeleton;
import org.bukkit.inventory.ItemStack;

import com.arkflame.minekoth.koth.events.KothEvent;
import com.arkflame.minekoth.utils.FoliaAPI;
import com.arkflame.minekoth.utils.Sounds;
import com.arkflame.minekoth.utils.Titles;

import java.util.Random;

public class SkeletonHordeEvent extends RandomEvent {
    private final int minSkeletons = 3;
    private final int maxSkeletons = 7;

    public SkeletonHordeEvent(double chance) {
        super("SkeletonHorde", chance);
    }

    @Override
    public void execute(KothEvent event) {
        if (!hasPlayersInside(event))
            return;

        Location center = event.getKoth().getCenter();
        World world = center.getWorld();
        int skeletonCount = new Random().nextInt(maxSkeletons - minSkeletons + 1) + minSkeletons;

        for (int i = 0; i < skeletonCount; i++) {
            double offsetX = (Math.random() - 0.5) * 10;
            double offsetZ = (Math.random() - 0.5) * 10;
            Location spawnLoc = center.clone().add(offsetX, 0, offsetZ);

            FoliaAPI.runTaskForRegion(spawnLoc, () -> {
                Skeleton skeleton = (Skeleton) world.spawnEntity(spawnLoc, EntityType.SKELETON);
                skeleton.setCustomName(ChatColor.GRAY + "KOTH Archer");
                skeleton.setCustomNameVisible(true);

                // Give them better equipment
                ItemStack bow = new ItemStack(Material.BOW);
                bow.addEnchantment(Enchantment.getByName("ARROW_DAMAGE"), 2);
                skeleton.getEquipment().setItemInHand(bow);
                skeleton.getEquipment().setHelmet(new ItemStack(Material.CHAINMAIL_HELMET));
            });
        }

        Sounds.play(center, 1.0f, 0.5f, "ENTITY_SKELETON_AMBIENT");
        Titles.sendTitle(event.getPlayersInZone(), "§cSkeleton Horde", "§7A horde of skeletons has appeared!", 5, 20,
                5);
    }

    private boolean hasPlayersInside(KothEvent event) {
        return !event.getPlayersInZone().isEmpty();
    }
}