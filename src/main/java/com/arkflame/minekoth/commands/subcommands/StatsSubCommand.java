package com.arkflame.minekoth.commands.subcommands;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import com.arkflame.minekoth.MineKoth;
import com.arkflame.minekoth.menus.PlayerStatsMenu;
import com.arkflame.minekoth.playerdata.PlayerData;

public class StatsSubCommand implements SubCommand {
    @Override
    public boolean execute(CommandSender sender, String[] args) {
        Player player = (Player) sender;
        
        if (args.length > 0 && !sender.hasPermission("minekoth.command.stats.other")) {
            sender.sendMessage(
                    MineKoth.getInstance().getLangManager().getLang(player).getMessage("messages.no-permission")
                            .replace("<node>", "minekoth.command.stats.other"));
            return true;
        }
        
        Player statsTarget;
        if (args.length < 1) {
            statsTarget = player;
        } else {
            statsTarget = Bukkit.getPlayer(args[0]);
        }

        if (statsTarget == null) {
            player.sendMessage(MineKoth.getInstance().getLangManager().getLang(player)
                    .getMessage("messages.no-player").replace("<n>", args[0]));
            return true;
        }

        PlayerData playerData = MineKoth.getInstance().getPlayerDataManager()
                .getIfLoaded(statsTarget.getUniqueId().toString());

        if (playerData == null) {
            player.sendMessage(MineKoth.getInstance().getLangManager().getLang(player)
                    .getMessage("messages.no-player-data"));
            return true;
        }

        new PlayerStatsMenu(player, playerData).open();
        
        return true;
    }

    @Override
    public String getName() {
        return "stats";
    }

    @Override
    public String getPermission() {
        return "minekoth.command.stats";
    }

    @Override
    public boolean requiresPlayer() {
        return true;
    }
}