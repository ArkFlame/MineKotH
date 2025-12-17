package com.arkflame.minekoth.commands.subcommands;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import com.arkflame.minekoth.MineKoth;
import com.arkflame.minekoth.koth.events.KothEvent.KothEventState;
import com.arkflame.minekoth.utils.Titles;

public class StopSubCommand implements SubCommand {
    @Override
    public boolean execute(CommandSender sender, String[] args) {
        Player player = sender instanceof Player ? (Player) sender : null;
        
        if (!MineKoth.getInstance().getKothEventManager().isEventActive()) {
            sender.sendMessage(MineKoth.getInstance().getLangManager().getLang(player)
                    .getMessage("messages.no-event-active"));
            return true;
        }
        
        if (MineKoth.getInstance().getKothEventManager().getKothEvent().getState() == KothEventState.CAPTURED) {
            sender.sendMessage(MineKoth.getInstance().getLangManager().getLang(player)
                    .getMessage("messages.koth-captured"));
            return true;
        }
        
        MineKoth.getInstance().getKothEventManager().end();
        sender.sendMessage(
                MineKoth.getInstance().getLangManager().getLang(player).getMessage("messages.event-stopped"));
        
        String senderName = sender instanceof Player ? ((Player) sender).getName() : "Console";
        Titles.sendTitle(MineKoth.getInstance().getLangManager().getLang(player)
                .getMessage("messages.event-stopped-title").replace("<n>", senderName),
                MineKoth.getInstance().getLangManager().getLang(player)
                        .getMessage("messages.event-stopped-subtitle").replace("<n>", senderName),
                20, 40, 20);
        
        return true;
    }

    @Override
    public String getName() {
        return "stop";
    }

    @Override
    public String getPermission() {
        return "minekoth.command.stop";
    }

    @Override
    public boolean requiresPlayer() {
        return false;
    }
}