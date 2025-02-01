package com.arkflame.minekoth.menus;

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
        this.menu = new Menu("Your Koth Stats", 3);
        buildMenu();
    }

    /**
     * Build the menu items based on player's statistics.
     */
    private void buildMenu() {
        // Kills
        MenuItem killsItem = new MenuItem.Builder(Material.DIAMOND_SWORD)
                .name("Kills: " + playerData.getTotalKills())
                .lore("Total Kills: " + playerData.getTotalKills())
                .onClick((InventoryClickEvent event) ->
                        player.sendMessage("You have " + playerData.getTotalKills() + " kills."))
                .build();
        menu.setItem(10, killsItem);

        // Deaths
        MenuItem deathsItem = new MenuItem.Builder(Material.TNT)
                .name("Deaths: " + playerData.getTotalDeaths())
                .lore("Total Deaths: " + playerData.getTotalDeaths())
                .onClick((InventoryClickEvent event) ->
                        player.sendMessage("You have " + playerData.getTotalDeaths() + " deaths."))
                .build();
        menu.setItem(12, deathsItem);

        // Wins
        MenuItem winsItem = new MenuItem.Builder(Material.GOLDEN_APPLE)
                .name("Wins: " + playerData.getTotalWins())
                .lore("Total Wins: " + playerData.getTotalWins())
                .onClick((InventoryClickEvent event) ->
                        player.sendMessage("You have " + playerData.getTotalWins() + " wins."))
                .build();
        menu.setItem(14, winsItem);

        // Participations
        MenuItem participationsItem = new MenuItem.Builder(Material.FEATHER)
                .name("Participations: " + playerData.getTotalParticipations())
                .lore("Total Participations: " + playerData.getTotalParticipations())
                .onClick((InventoryClickEvent event) ->
                        player.sendMessage("You participated " + playerData.getTotalParticipations() + " times."))
                .build();
        menu.setItem(16, participationsItem);

        // Capture Time
        MenuItem captureTimeItem = new MenuItem.Builder(Material.CLOCK)
                .name("Capture Time: " + playerData.getTotalCaptureTime() + "s")
                .lore("Total Capture Time: " + playerData.getTotalCaptureTime() + " seconds")
                .onClick((InventoryClickEvent event) ->
                        player.sendMessage("Your total capture time is " + playerData.getTotalCaptureTime() + " seconds."))
                .build();
        menu.setItem(19, captureTimeItem);

        // Kill-Death Ratio
        MenuItem kdrItem = new MenuItem.Builder(Material.NETHER_STAR)
                .name("KDR: " + String.format("%.2f", playerData.getTotalKdr()))
                .lore("Your Kill/Death Ratio")
                .onClick((InventoryClickEvent event) ->
                        player.sendMessage("Your overall KDR is " + String.format("%.2f", playerData.getTotalKdr()) + "."))
                .build();
        menu.setItem(21, kdrItem);

        // Damage Dealt
        MenuItem damageDealtItem = new MenuItem.Builder(Material.IRON_SWORD)
                .name("Damage Dealt: " + playerData.getTotalDamageDealt())
                .lore("Total Damage Dealt: " + playerData.getTotalDamageDealt())
                .onClick((InventoryClickEvent event) ->
                        player.sendMessage("You have dealt " + playerData.getTotalDamageDealt() + " damage."))
                .build();
        menu.setItem(23, damageDealtItem);

        // Damage Received
        MenuItem damageReceivedItem = new MenuItem.Builder(Material.SHIELD)
                .name("Damage Received: " + playerData.getTotalDamageReceived())
                .lore("Total Damage Received: " + playerData.getTotalDamageReceived())
                .onClick((InventoryClickEvent event) ->
                        player.sendMessage("You have received " + playerData.getTotalDamageReceived() + " damage."))
                .build();
        menu.setItem(25, damageReceivedItem);

        // Rewards Received
        MenuItem rewardsItem = new MenuItem.Builder(Material.EMERALD)
                .name("Rewards Received: " + playerData.getTotalRewardsReceived())
                .lore("Total Rewards Received: " + playerData.getTotalRewardsReceived())
                .onClick((InventoryClickEvent event) ->
                        player.sendMessage("You have received " + playerData.getTotalRewardsReceived() + " rewards."))
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
