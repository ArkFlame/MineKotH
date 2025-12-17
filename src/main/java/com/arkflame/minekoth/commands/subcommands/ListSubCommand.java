package com.arkflame.minekoth.commands.subcommands;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import com.arkflame.minekoth.MineKoth;
import com.arkflame.minekoth.koth.managers.KothManager;

public class ListSubCommand implements SubCommand {
    @Override
    public boolean execute(CommandSender sender, String[] args) {
        KothManager kothManager = MineKoth.getInstance().getKothManager();
        Player player = sender instanceof Player ? (Player) sender : null;
        
        if (kothManager.getAllkoths().isEmpty()) {
            MineKoth.getInstance().getLangManager().sendMessage(player, "messages.no-koths");
            return true;
        }
        
        MineKoth.getInstance().getLangManager().sendMessage(player, "messages.koth-list");
        kothManager.getAllkoths().values()
                .forEach(koth -> MineKoth.getInstance().getLangManager().sendMessage(
                        player, "messages.koth-info-list",
                        "<id>", koth.getId(),
                        "<name>", koth.getName()));
        
        return true;
    }

    @Override
    public String getName() {
        return "list";
    }

    @Override
    public String getPermission() {
        return "minekoth.command.list";
    }

    @Override
    public boolean requiresPlayer() {
        return false;
    }
}