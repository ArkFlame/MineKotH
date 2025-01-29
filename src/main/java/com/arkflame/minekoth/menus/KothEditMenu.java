package com.arkflame.minekoth.menus;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import com.arkflame.mineclans.utils.Materials;
import com.arkflame.minekoth.koth.Koth;
import com.arkflame.minekoth.setup.listeners.SetupChatListener;
import com.arkflame.minekoth.setup.session.SetupSession;
import com.arkflame.minekoth.utils.MenuUtil.Menu;
import com.arkflame.minekoth.utils.MenuUtil.MenuItem;
import com.arkflame.minekoth.MineKoth;

public class KothEditMenu extends Menu {
    private final Koth koth;

    public KothEditMenu(Koth koth) {
        super("§8» §6KoTH Edit Menu §8- §7" + koth.getId(), 6);
        this.koth = koth;
        setBackground(Materials.get("GRAY_STAINED_GLASS_PANE", "STAINED_GLASS_PANE:7"));
        setupItems();
    }

    private void setupItems() {
        MenuItem nameItem = new MenuItem.Builder(Materials.get("NAME_TAG"))
                .name("§e§lKoTH Name")
                .lore(
                        "§7Current value: §f" + koth.getName(),
                        "",
                        "§8• §7Click to modify the name",
                        "§8• §7This will be displayed in",
                        "§8  §7announcements and GUI elements",
                        "",
                        "§e§lClick to edit!")
                .onClick(e -> startNameSession(e))
                .build();

        MenuItem regionItem = new MenuItem.Builder(Materials.get("GOLDEN_AXE", "GOLD_AXE"))
                .name("§6§lRegion Selection")
                .lore(
                        "§7First Position: §f" + formatLocation(koth.getFirstPosition()),
                        "§7Second Position: §f" + formatLocation(koth.getSecondPosition()),
                        "",
                        "§8• §7Click to redefine the region",
                        "§8• §7You'll need to select two corners",
                        "§8• §7Use the golden axe provided",
                        "",
                        "§e§lClick to edit!")
                .onClick(e -> startRegionSession(e))
                .build();

        MenuItem timeLimitItem = new MenuItem.Builder(Materials.get("CLOCK"))
                .name("§b§lTime Limit")
                .lore(
                        "§7Current limit: §f" + formatTime(koth.getTimeLimit()),
                        "",
                        "§8• §7Set the maximum duration",
                        "§8• §7of each KoTH event",
                        "§8• §7Format: MM:SS",
                        "",
                        "§e§lClick to edit!")
                .onClick(e -> startTimeLimitSession(e))
                .build();

        MenuItem captureTimeItem = new MenuItem.Builder(Materials.get("HOPPER"))
                .name("§d§lCapture Time")
                .lore(
                        "§7Current time: §f" + formatTime(koth.getTimeToCapture()),
                        "",
                        "§8• §7Time required to capture",
                        "§8• §7the control point",
                        "§8• §7Format: MM:SS",
                        "",
                        "§e§lClick to edit!")
                .onClick(e -> startCaptureTimeSession(e))
                .build();

        MenuItem rewardsItem = new MenuItem.Builder(Materials.get("CHEST"))
                .name("§6§lRewards Configuration")
                .lore(
                        "§7Commands: §f" + koth.getRewards().getRewardsCommands().size(),
                        "§7Items: §f" + koth.getRewards().getRewardsItems().size(),
                        "§7Loot Type: §f" + koth.getRewards().getLootType(),
                        "§7Amount: §f" + koth.getRewards().getLootAmount(),
                        "",
                        "§8• §7Configure rewards",
                        "§8• §7Set commands and items",
                        "§8• §7Adjust loot settings",
                        "",
                        "§e§lClick to edit!")
                .onClick(e -> startRewardsSession(e))
                .build();

        MenuItem scheduleItem = new MenuItem.Builder(Materials.get("BOOK"))
                .name("§a§lSchedule Settings")
                .lore(
                        "§7Event Times: §f" + koth.getTimes(),
                        "§7Active Days: §f" + koth.getDays(),
                        "",
                        "§8• §7Set event schedule",
                        "§8• §7Configure active days",
                        "§8• §7Manage time slots",
                        "",
                        "§e§lClick to edit!")
                .onClick(e -> startScheduleSession(e))
                .build();

        grid(nameItem, regionItem, timeLimitItem,
                captureTimeItem, rewardsItem, scheduleItem);
    }

    private void startNameSession(InventoryClickEvent e) {
        Player player = (Player) e.getWhoClicked();
        SetupSession session = new SetupSession(koth);
        session.setName(null);
        MineKoth.getInstance().getSessionManager().addSession(player, session);
        player.closeInventory();
        player.sendMessage("§8» §7Please type the new name for the KoTH in chat.");
    }

    private void startWorldSession(InventoryClickEvent e) {
        Player player = (Player) e.getWhoClicked();
        SetupSession session = new SetupSession(koth);
        session.setWorldName(null);
        MineKoth.getInstance().getSessionManager().addSession(player, session);
        player.closeInventory();
        player.sendMessage("§8» §7Please type the world name in chat.");
    }

    private void startRegionSession(InventoryClickEvent e) {
        Player player = (Player) e.getWhoClicked();
        SetupSession session = new SetupSession(koth);
        session.setFirstPosition(null);
        session.setSecondPosition(null);
        MineKoth.getInstance().getSessionManager().addSession(player, session);
        player.closeInventory();
        player.sendMessage("§8» §7Click the positions to select the KoTH region.");
    }

    private void startTimeLimitSession(InventoryClickEvent e) {
        Player player = (Player) e.getWhoClicked();
        SetupSession session = new SetupSession(koth);
        session.setTimeLimit(0);
        MineKoth.getInstance().getSessionManager().addSession(player, session);
        player.closeInventory();
        player.sendMessage("§8» §7Please type the time limit (MM:SS) in chat.");
    }

    private void startCaptureTimeSession(InventoryClickEvent e) {
        Player player = (Player) e.getWhoClicked();
        SetupSession session = new SetupSession(koth);
        session.setCaptureTime(0);
        MineKoth.getInstance().getSessionManager().addSession(player, session);
        player.closeInventory();
        player.sendMessage("§8» §7Please type the capture time (MM:SS) in chat.");
    }

    private void startRewardsSession(InventoryClickEvent e) {
        Player player = (Player) e.getWhoClicked();
        SetupSession session = new SetupSession(koth);
        session.setRewards(null);
        session.setRewardsCommands(new ArrayList<>());
        session.setLootType(null);
        session.unsetLootAmount();
        session.setEditingRewards(true);
        player.closeInventory();
        player.sendMessage(ChatColor.GREEN + "Put rewards in the chest.");
        MineKoth.getInstance().getSessionManager().addSession(player, session);
        SetupChatListener.openRewardsInventory(player);
    }

    private void startScheduleSession(InventoryClickEvent e) {
        Player player = (Player) e.getWhoClicked();
        SetupSession session = new SetupSession(koth);
        session.setTimes(null);
        session.setDays(null);
        MineKoth.getInstance().getSessionManager().addSession(player, session);
        player.closeInventory();
        player.sendMessage("§8» §7Please type the event times in chat. (1pm, 11am...)");
    }

    private String formatLocation(Location loc) {
        return String.format("§f%d, %d, %d", loc.getBlockX(), loc.getBlockY(), loc.getBlockZ());
    }

    private String formatTime(int seconds) {
        return String.format("§f%02d:%02d", seconds / 60, seconds % 60);
    }
}