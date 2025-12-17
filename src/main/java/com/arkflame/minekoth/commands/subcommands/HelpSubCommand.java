package com.arkflame.minekoth.commands.subcommands;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import com.arkflame.minekoth.MineKoth;

public class HelpSubCommand implements SubCommand {
    @Override
    public boolean execute(CommandSender sender, String[] args) {
        Player player = sender instanceof Player ? (Player) sender : null;
        sender.sendMessage(
                MineKoth.getInstance().getLangManager().getLang(player).getMessage("messages.koth-commands"));
        return true;
    }

    @Override
    public String getName() {
        return "help";
    }

    @Override
    public String getPermission() {
        return null; // No permission required for help
    }

    @Override
    public boolean requiresPlayer() {
        return false;
    }
}