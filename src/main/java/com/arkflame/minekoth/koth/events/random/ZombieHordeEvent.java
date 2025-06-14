package com.arkflame.minekoth.koth.events.random;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.Zombie;
import org.bukkit.inventory.ItemStack;

import com.arkflame.minekoth.MineKoth;
import com.arkflame.minekoth.koth.events.KothEvent;
import com.arkflame.minekoth.lang.Lang;
import com.arkflame.minekoth.utils.FoliaAPI;
import com.arkflame.minekoth.utils.Sounds;
import com.arkflame.minekoth.utils.Titles;

public class ZombieHordeEvent extends RandomEvent {
    private final int spawnCount;

    public ZombieHordeEvent(double chance, int spawnCount) {
        super("ZombieHorde", chance);
        this.spawnCount = spawnCount;
    }

    @Override
    public void execute(KothEvent event) {
        if (!hasPlayersInside(event))
            return;

        Location center = event.getKoth().getCenter();
        World world = center.getWorld();

        for (int i = 0; i < spawnCount; i++) {
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

        Sounds.play(center, 1.0f, 1.0f, "ENTITY_ZOMBIE_AMBIENT");
        for (Player player : event.getPlayersInZone()) {
            Lang lang = MineKoth.getInstance().getLangManager().getLang(player);
            Titles.sendTitle(player,
                    lang.getMessage("messages.zombie-horde-title"),
                    lang.getMessage("messages.zombie-horde-subtitle"),
                    5, 20, 5);
        }
    }

    private boolean hasPlayersInside(KothEvent event) {
        return !event.getPlayersInZone().isEmpty();
    }
}
