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

    // Participant, Betting Player, Amount
    private final ConcurrentMap<String, Map<UUID, Double>> bets = new ConcurrentHashMap<>();

    /**
     * Places a bet on a participant.
     *
     * @param player      The player placing the bet.
     * @param participant The participant to bet on.
     * @param amount      The amount to bet.
     * @return True if the bet was successful, false otherwise.
     */
    public boolean placeBet(OfflinePlayer player, String participant, double amount) {
        if (!isEconomyAvailable()) {
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
                .merge(player.getUniqueId(), amount, Double::sum);

        // Send a fancy message to the player
        sendMessage(player, "&aYou have successfully placed a bet of &6" + amount + " &aon &b"
                + participantPlayer.getName() + "&a!");

        return true;
    }

    /**
     * Distributes rewards when the event ends.
     *
     * @param winner The name of the winning participant.
     */
    public void giveRewards(String winner) {
        if (!isEconomyAvailable()) {
            return;
        }

        Map<UUID, Double> winningBets = getWinningBets(winner);
        BigDecimal totalPool = calculateTotalPool();
        BigDecimal totalWinningBets = calculateTotalWinningBets(winningBets);

        if (totalWinningBets.compareTo(BigDecimal.ZERO) <= 0) {
            return; // No valid bets on the winner
        }

        Economy economy = MineKoth.getInstance().getEconomy();
        
        // Distribute rewards only to those who placed bets on the winner
        distributeRewards(winningBets, totalPool, totalWinningBets, economy, winner);

        // Notify losers who did not win
        notifyLosers(winningBets);

        clearAllBets();
    }

    private boolean isEconomyAvailable() {
        return MineKoth.getInstance().isEconomyPresent();
    }

    private Map<UUID, Double> getWinningBets(String winner) {
        return bets.getOrDefault(winner, Collections.emptyMap());
    }

    private BigDecimal calculateTotalPool() {
        return bets.values().stream()
                .flatMap(map -> map.values().stream())
                .map(BigDecimal::valueOf)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private BigDecimal calculateTotalWinningBets(Map<UUID, Double> winningBets) {
        return winningBets.values().stream()
                .map(BigDecimal::valueOf)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private void distributeRewards(Map<UUID, Double> winningBets, BigDecimal totalPool,
                                   BigDecimal totalWinningBets, Economy economy, String winner) {
        for (Map.Entry<UUID, Double> entry : winningBets.entrySet()) {
            UUID playerId = entry.getKey();
            BigDecimal reward = calculateReward(entry.getValue(), totalWinningBets, totalPool);

            OfflinePlayer player = Bukkit.getOfflinePlayer(playerId);
            if (depositReward(economy, player, reward)) {
                sendMessage(player, "&6Congratulations! You won &a" + reward + " &6for betting on &b" + winner + "&6!");
            }
        }
    }

    private BigDecimal calculateReward(double playerBet, BigDecimal totalWinningBets,
                                       BigDecimal totalPool) {
        BigDecimal playerShare = BigDecimal.valueOf(playerBet).divide(totalWinningBets, MONEY_SCALE, ROUNDING_MODE);
        return playerShare.multiply(totalPool).setScale(MONEY_SCALE, ROUNDING_MODE);
    }

    private boolean depositReward(Economy economy, OfflinePlayer player, BigDecimal reward) {
        try {
            economy.depositPlayer(player, reward.doubleValue());
            return true;
        } catch (Exception e) {
            sendMessage(player, "&cFailed to deposit your reward. Please contact an administrator.");
            return false;
        }
    }

    private void notifyLosers(Map<UUID, Double> winningBets) {
        for (Map.Entry<String, Map<UUID, Double>> entry : bets.entrySet()) {
            String participant = entry.getKey();
            for (Map.Entry<UUID, Double> losingEntry : entry.getValue().entrySet()) {
                UUID playerId = losingEntry.getKey();
                if (!winningBets.containsKey(playerId)) { // Only notify those who lost
                    OfflinePlayer player = Bukkit.getOfflinePlayer(playerId);
                    double playerBet = losingEntry.getValue();
                    sendMessage(player,
                            "&cYou lost &a" + playerBet + " &cbet on &b" + participant + "&c.");
                }
            }
        }
    }

    private void clearAllBets() {
        synchronized (bets) {
            bets.clear();
        }
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
