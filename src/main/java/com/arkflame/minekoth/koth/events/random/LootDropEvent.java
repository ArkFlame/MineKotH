package com.arkflame.minekoth.koth.events.random;

import java.util.List;
import java.util.ArrayList;
import java.util.Random;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

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

public class LootDropEvent extends RandomEvent {
    private final List<ItemStack> loot;
    private final int dropCount;

    public LootDropEvent(double chance, List<String> lootConfig, int dropCount) {
        super("LootDrop", chance);
        this.loot = parseLootConfig(lootConfig);
        this.dropCount = dropCount;
    }

    @Override
    public void execute(KothEvent event) {
        Location center = event.getKoth().getCenter();
        World world = center.getWorld();

        Random rand = new Random();
        for (int i = 0; i < dropCount; i++) {
            // Random offset from center
            double offsetX = (Math.random() - 0.5) * 10;
            double offsetZ = (Math.random() - 0.5) * 10;
            Location dropLocation = center.clone().add(offsetX, 10, offsetZ);

            ItemStack lootItem = loot.get(rand.nextInt(loot.size()));

            FoliaAPI.runTaskForRegion(dropLocation, () -> {
                world.dropItemNaturally(dropLocation, lootItem);
                ParticleUtil.generateCircle(dropLocation, "FIREWORKS_SPARK", 1.0, 10);
                Sounds.play(world, dropLocation, 1.0f, 1.0f, "ENTITY_ITEM_PICKUP");
            });
        }

        for (Player player : event.getPlayersInZone()) {
            Titles.sendTitle(player,
                    MineKoth.getInstance().getLangManager().getLang(player).getMessage("messages.loot-drop-title"),
                    MineKoth.getInstance().getLangManager().getLang(player).getMessage("messages.loot-drop-subtitle"),
                    5, 20, 5);
        }
    }

    private List<ItemStack> parseLootConfig(List<String> lootConfig) {
        return lootConfig.stream().map(lootEntry -> {
            String[] parts = lootEntry.split(": ");
            Material material = Material.matchMaterial(parts[0]);
            int amount = Integer.parseInt(parts[1]);
            return new ItemStack(material, amount);
        }).collect(Collectors.toList());
    }
}
