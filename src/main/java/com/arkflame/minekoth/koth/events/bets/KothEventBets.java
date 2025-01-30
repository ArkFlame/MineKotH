package com.arkflame.minekoth.koth.events.bets;

import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import com.arkflame.minekoth.MineKoth;
import com.arkflame.minekoth.utils.ChatColors;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class KothEventBets {

    private static final int MONEY_SCALE = 2;
    private static final RoundingMode ROUNDING_MODE = RoundingMode.HALF_UP;

    private final ConcurrentMap<String, Map<UUID, Double>> bets = new ConcurrentHashMap<>(); // Stores bets for each participant

    /**
     * Places a bet on a participant.
     *
     * @param player      The player placing the bet.
     * @param participant The participant to bet on.
     * @param amount      The amount to bet.
     * @return True if the bet was successful, false otherwise.
     */
    public boolean placeBet(OfflinePlayer player, String participant, double amount) {
        if (!MineKoth.getInstance().isEconomyPresent()) {
            sendMessage(player, "&cVault is not present. Bet cannot be placed.");
            return false;
        }

        if (amount <= 0) {
            sendMessage(player, "&cBet amount must be positive. Bet not placed.");
            return false;
        }

        Player participantPlayer = MineKoth.getInstance().getServer().getPlayer(participant);
        if (participantPlayer == null) {
            sendMessage(player, "&cParticipant not found. Bet not placed.");
            return false;
        }

        Economy economy = MineKoth.getInstance().getEconomy();

        // Check if the player has enough money
        if (!economy.has(player, amount)) {
            sendMessage(player, "&cYou do not have enough money to place this bet.");
            return false;
        }

        // Deduct the bet amount from the player's balance
        try {
            economy.withdrawPlayer(player, amount);
        } catch (Exception e) {
            sendMessage(player, "&cFailed to place your bet. Please try again.");
            return false;
        }

        // Add the bet to the participant's map
        bets.computeIfAbsent(participantPlayer.getName(), k -> new ConcurrentHashMap<>())
                .merge(participantPlayer.getUniqueId(), amount, Double::sum);

        // Send a fancy message to the player
        sendMessage(player, "&aYou have successfully placed a bet of &6" + amount + " &aon &b" + participantPlayer.getName() + "&a!");

        return true;
    }

    /**
     * Distributes rewards when the event ends.
     *
     * @param winner The name of the winning participant.
     */
    public void giveRewards(String winner) {
        if (!MineKoth.getInstance().isEconomyPresent()) {
            Bukkit.getOnlinePlayers().forEach(p -> sendMessage(p, "&cVault is not present. Rewards cannot be distributed."));
            return;
        }

        Map<UUID, Double> winningBets = bets.getOrDefault(winner, Collections.emptyMap());

        // Calculate the total pool (all bets)
        BigDecimal totalPool = bets.values().stream()
                .flatMap(map -> map.values().stream())
                .map(BigDecimal::valueOf)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Calculate the total bets on the winner
        BigDecimal totalWinningBets = winningBets.values().stream()
                .map(BigDecimal::valueOf)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        if (totalWinningBets.compareTo(BigDecimal.ZERO) <= 0) {
            Bukkit.getOnlinePlayers().forEach(p -> sendMessage(p, "&cNo bets were placed on the winner. No rewards to distribute."));
            return;
        }

        Economy economy = MineKoth.getInstance().getEconomy();

        // Distribute rewards to winners based on their share
        for (Map.Entry<UUID, Double> entry : winningBets.entrySet()) {
            UUID playerId = entry.getKey();
            double playerBet = entry.getValue();
            BigDecimal playerShare = BigDecimal.valueOf(playerBet).divide(totalWinningBets, MONEY_SCALE, ROUNDING_MODE);
            BigDecimal reward = playerShare.multiply(totalPool).setScale(MONEY_SCALE, ROUNDING_MODE);

            // Give the reward to the player
            OfflinePlayer player = Bukkit.getOfflinePlayer(playerId);
            try {
                economy.depositPlayer(player, reward.doubleValue());
            } catch (Exception e) {
                sendMessage(player, "&cFailed to deposit your reward. Please contact an administrator.");
                continue;
            }

            // Send a fancy message to the player
            sendMessage(player, "&6Congratulations! You won &a" + reward + " &6for betting on &b" + winner + "&6!");
        }

        // Clear all bets after distributing rewards
        synchronized (bets) {
            bets.clear();
        }

        Bukkit.getOnlinePlayers().forEach(p -> sendMessage(p, "&aRewards have been distributed for the winner: &b" + winner));
    }

    /**
     * Sends a message to the player if they are online.
     *
     * @param player  The player to send the message to.
     * @param message The message to send.
     */
    private void sendMessage(OfflinePlayer player, String message) {
        if (player.isOnline()) {
            Objects.requireNonNull(player.getPlayer()).sendMessage(ChatColors.color(message));
        }
    }
}