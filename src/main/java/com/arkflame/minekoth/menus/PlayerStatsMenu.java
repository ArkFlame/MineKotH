package com.arkflame.minekoth.menus;

import com.arkflame.mineclans.modernlib.utils.Materials;
import com.arkflame.minekoth.MineKoth;
import com.arkflame.minekoth.lang.Lang;
import com.arkflame.minekoth.playerdata.PlayerData;
import com.arkflame.minekoth.utils.MenuUtil.Menu;
import com.arkflame.minekoth.utils.MenuUtil.MenuItem;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;

/**
 * A menu to display a player’s Koth statistics.
 */
public class PlayerStatsMenu {

        private final Player player;
        private final PlayerData playerData;
        private final Menu menu;
    
        /**
         * Constructs a PlayerStatsMenu for the given player.
         *
         * @param player     the Player to show the menu to
         * @param playerData the PlayerData from which stats are retrieved
         */
        public PlayerStatsMenu(Player player, PlayerData playerData) {
            this.player = player;
            this.playerData = playerData;
            // Create a menu with 3 rows (27 slots) and a title.
            this.menu = new Menu(MineKoth.getInstance().getLangManager().getLang(player).getMessage("messages.koth-stats-title"), 5);
            buildMenu();
        }
    
        /**
         * Build the menu items based on player's statistics.
         */
        private void buildMenu() {
            Lang lang = MineKoth.getInstance().getLangManager().getLang(player);
    
            // Kills
            MenuItem killsItem = new MenuItem.Builder(Material.DIAMOND_SWORD)
                    .name(lang.getMessage("messages.kills").replace("<value>", String.valueOf(playerData.getTotalKills())))
                    .lore(lang.getMessage("messages.total-kills").replace("<value>", String.valueOf(playerData.getTotalKills())))
                    .onClick((InventoryClickEvent event) ->
                            player.sendMessage(lang.getMessage("messages.kills-message").replace("<value>", String.valueOf(playerData.getTotalKills()))))
                    .build();
            menu.setItem(10, killsItem);
    
            // Deaths
            MenuItem deathsItem = new MenuItem.Builder(Material.TNT)
                    .name(lang.getMessage("messages.deaths").replace("<value>", String.valueOf(playerData.getTotalDeaths())))
                    .lore(lang.getMessage("messages.total-deaths").replace("<value>", String.valueOf(playerData.getTotalDeaths())))
                    .onClick((InventoryClickEvent event) ->
                            player.sendMessage(lang.getMessage("messages.deaths-message").replace("<value>", String.valueOf(playerData.getTotalDeaths()))))
                    .build();
            menu.setItem(12, deathsItem);
    
            // Wins
            MenuItem winsItem = new MenuItem.Builder(Material.GOLDEN_APPLE)
                    .name(lang.getMessage("messages.wins").replace("<value>", String.valueOf(playerData.getTotalWins())))
                    .lore(lang.getMessage("messages.total-wins").replace("<value>", String.valueOf(playerData.getTotalWins())))
                    .onClick((InventoryClickEvent event) ->
                            player.sendMessage(lang.getMessage("messages.wins-message").replace("<value>", String.valueOf(playerData.getTotalWins()))))
                    .build();
            menu.setItem(14, winsItem);
    
            // Participations
            MenuItem participationsItem = new MenuItem.Builder(Material.FEATHER)
                    .name(lang.getMessage("messages.participations").replace("<value>", String.valueOf(playerData.getTotalParticipations())))
                    .lore(lang.getMessage("messages.total-participations").replace("<value>", String.valueOf(playerData.getTotalParticipations())))
                    .onClick((InventoryClickEvent event) ->
                            player.sendMessage(lang.getMessage("messages.participations-message").replace("<value>", String.valueOf(playerData.getTotalParticipations()))))
                    .build();
            menu.setItem(16, participationsItem);
    
            // Capture Time
            MenuItem captureTimeItem = new MenuItem.Builder(Materials.get("CLOCK", "WATCH"))
                    .name(lang.getMessage("messages.capture-time").replace("<value>", String.valueOf(playerData.getTotalCaptureTime())))
                    .lore(lang.getMessage("messages.total-capture-time").replace("<value>", String.valueOf(playerData.getTotalCaptureTime())))
                    .onClick((InventoryClickEvent event) ->
                            player.sendMessage(lang.getMessage("messages.capture-time-message").replace("<value>", String.valueOf(playerData.getTotalCaptureTime()))))
                    .build();
            menu.setItem(19, captureTimeItem);
    
            // Kill-Death Ratio
            MenuItem kdrItem = new MenuItem.Builder(Material.NETHER_STAR)
                    .name(lang.getMessage("messages.kdr").replace("<value>", String.format("%.2f", playerData.getTotalKdr())))
                    .lore(lang.getMessage("messages.total-kdr"))
                    .onClick((InventoryClickEvent event) ->
                            player.sendMessage(lang.getMessage("messages.kdr-message").replace("<value>", String.format("%.2f", playerData.getTotalKdr()))))
                    .build();
            menu.setItem(21, kdrItem);
    
            // Damage Dealt
            MenuItem damageDealtItem = new MenuItem.Builder(Material.IRON_SWORD)
                    .name(lang.getMessage("messages.damage-dealt").replace("<value>", String.valueOf(playerData.getTotalDamageDealt())))
                    .lore(lang.getMessage("messages.total-damage-dealt").replace("<value>", String.valueOf(playerData.getTotalDamageDealt())))
                    .onClick((InventoryClickEvent event) ->
                            player.sendMessage(lang.getMessage("messages.damage-dealt-message").replace("<value>", String.valueOf(playerData.getTotalDamageDealt()))))
                    .build();
            menu.setItem(23, damageDealtItem);
    
            // Damage Received
            MenuItem damageReceivedItem = new MenuItem.Builder(Materials.get("SHIELD", "IRON_HELMET"))
                    .name(lang.getMessage("messages.damage-received").replace("<value>", String.valueOf(playerData.getTotalDamageReceived())))
                    .lore(lang.getMessage("messages.total-damage-received").replace("<value>", String.valueOf(playerData.getTotalDamageReceived())))
                    .onClick((InventoryClickEvent event) ->
                            player.sendMessage(lang.getMessage("messages.damage-received-message").replace("<value>", String.valueOf(playerData.getTotalDamageReceived()))))
                    .build();
            menu.setItem(25, damageReceivedItem);
    
            // Rewards Received
            MenuItem rewardsItem = new MenuItem.Builder(Material.EMERALD)
                    .name(lang.getMessage("messages.rewards-received").replace("<value>", String.valueOf(playerData.getTotalRewardsReceived())))
                    .lore(lang.getMessage("messages.total-rewards-received").replace("<value>", String.valueOf(playerData.getTotalRewardsReceived())))
                    .onClick((InventoryClickEvent event) ->
                            player.sendMessage(lang.getMessage("messages.rewards-received-message").replace("<value>", String.valueOf(playerData.getTotalRewardsReceived()))))
                    .build();
            menu.setItem(28, rewardsItem);
        }
    
        /**
         * Opens the stats menu for the player.
         */
        public void open() {
            menu.open(player);
        }
    }
    