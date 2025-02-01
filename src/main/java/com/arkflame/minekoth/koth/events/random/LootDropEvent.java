package com.arkflame.minekoth.koth.events.random;

import java.util.List;
import java.util.ArrayList;
import java.util.Random;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import com.arkflame.minekoth.MineKoth;
import com.arkflame.minekoth.koth.events.KothEvent;
import com.arkflame.minekoth.particles.ParticleUtil;
import com.arkflame.minekoth.utils.FoliaAPI;
import com.arkflame.minekoth.utils.Sounds;
import com.arkflame.minekoth.utils.Titles;

public class LootDropEvent extends RandomEvent {
    private final List<ItemStack> commonLoot;
    private final List<ItemStack> specialLoot;

    public LootDropEvent(double chance) {
        super("LootDrop", chance);
        this.commonLoot = initializeCommonLoot();
        this.specialLoot = initializeSpecialLoot();
    }

    @Override
    public void execute(KothEvent event) {
        Location center = event.getKoth().getCenter();
        World world = center.getWorld();

        // Random offset from center
        double offsetX = (Math.random() - 0.5) * 10;
        double offsetZ = (Math.random() - 0.5) * 10;
        Location dropLocation = center.clone().add(offsetX, 10, offsetZ);

        // 30% chance for special loot, 70% for common
        List<ItemStack> selectedLoot = Math.random() < 0.3 ? specialLoot : commonLoot;
        ItemStack lootItem = selectedLoot.get(new Random().nextInt(selectedLoot.size()));

        FoliaAPI.runTaskForRegion(dropLocation, () -> {
            world.dropItemNaturally(dropLocation, lootItem);
            ParticleUtil.generateCircle(dropLocation, "FIREWORKS_SPARK", 1.0, 10);
            Sounds.play(world, dropLocation, 1.0f, 1.0f, "ENTITY_ITEM_PICKUP");
        });

        for (Player player : event.getPlayersInZone()) {
            Titles.sendTitle(player,
                    MineKoth.getInstance().getLangManager().getLang(player).getMessage("messages.loot-drop-title"),
                    MineKoth.getInstance().getLangManager().getLang(player).getMessage("messages.loot-drop-subtitle"),
                    5, 20, 5);
        }
    }

    private List<ItemStack> initializeCommonLoot() {
        List<ItemStack> loot = new ArrayList<>();
        // Add common items
        loot.add(new ItemStack(Material.DIAMOND, 3));
        loot.add(new ItemStack(Material.GOLDEN_APPLE, 2));
        loot.add(new ItemStack(Material.ENDER_PEARL, 4));
        return loot;
    }

    private List<ItemStack> initializeSpecialLoot() {
        List<ItemStack> loot = new ArrayList<>();
        // Add special items
        ItemStack enchantedSword = new ItemStack(Material.DIAMOND_SWORD);
        enchantedSword.addEnchantment(Enchantment.getByName("DAMAGE_ALL"), 3);
        loot.add(enchantedSword);

        ItemStack enchantedArmor = new ItemStack(Material.DIAMOND_CHESTPLATE);
        enchantedArmor.addEnchantment(Enchantment.getByName("PROTECTION_ENVIRONMENTAL"), 4);
        loot.add(enchantedArmor);
        return loot;
    }
}