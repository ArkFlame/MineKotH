package com.arkflame.minekoth.commands.subcommands;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import com.arkflame.minekoth.MineKoth;

public class ReloadSubCommand implements SubCommand {
    @Override
    public boolean execute(CommandSender sender, String[] args) {
        MineKoth.getInstance().onDisable();
        MineKoth.getInstance().onEnable();
        
        if (sender instanceof Player) {
            MineKoth.getInstance().getLangManager().sendMessage((Player) sender, "messages.reload");
        } else {
            MineKoth.getInstance().getLangManager().sendMessage(null, "messages.reload");
        }
        
        return true;
    }

    @Override
    public String getName() {
        return "reload";
    }

    @Override
    public String getPermission() {
        return "minekoth.command.reload";
    }

    @Override
    public boolean requiresPlayer() {
        return false;
    }
}