package com.arkflame.minekoth.commands.subcommands;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import com.arkflame.minekoth.MineKoth;
import com.arkflame.minekoth.koth.Koth;
import com.arkflame.minekoth.koth.managers.KothManager;
import com.arkflame.minekoth.schedule.managers.ScheduleManager;

public class DeleteSubCommand implements SubCommand {
    @Override
    public boolean execute(CommandSender sender, String[] args) {
        Player player = sender instanceof Player ? (Player) sender : null;
        KothManager kothManager = MineKoth.getInstance().getKothManager();
        ScheduleManager scheduleManager = MineKoth.getInstance().getScheduleManager();
        
        if (args.length < 1) {
            MineKoth.getInstance().getLangManager().sendMessage(player, "messages.usage-delete");
            return true;
        }
        
        try {
            int id = Integer.parseInt(args[0]);
            Koth removed = kothManager.deleteKoth(id);
            scheduleManager.removeSchedulesByKoth(id);
            MineKoth.getInstance().getKothLoader().delete(id);
            
            if (removed == null) {
                MineKoth.getInstance().getLangManager().sendMessage(player, "messages.no-koth-id", "<id>", id);
                return true;
            }
            
            MineKoth.getInstance().getLangManager().sendMessage(player, "messages.koth-deleted", "<id>", id);
        } catch (NumberFormatException e) {
            MineKoth.getInstance().getLangManager().sendMessage(player, "messages.invalid-id");
        }
        
        return true;
    }

    @Override
    public String getName() {
        return "delete";
    }

    @Override
    public String getPermission() {
        return "minekoth.command.delete";
    }

    @Override
    public boolean requiresPlayer() {
        return false;
    }
}