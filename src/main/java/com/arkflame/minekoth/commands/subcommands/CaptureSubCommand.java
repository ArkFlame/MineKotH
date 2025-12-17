package com.arkflame.minekoth.commands.subcommands;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import com.arkflame.minekoth.MineKoth;
import com.arkflame.minekoth.koth.events.CapturingPlayers;
import com.arkflame.minekoth.koth.events.KothEvent.KothEventState;

public class CaptureSubCommand implements SubCommand {
    @Override
    public boolean execute(CommandSender sender, String[] args) {
        Player player = (Player) sender;
        
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
        
        MineKoth.getInstance().getKothEventManager().getKothEvent().setCaptured(new CapturingPlayers(player));
        
        return true;
    }

    @Override
    public String getName() {
        return "capture";
    }

    @Override
    public String getPermission() {
        return "minekoth.command.capture";
    }

    @Override
    public boolean requiresPlayer() {
        return true;
    }
}