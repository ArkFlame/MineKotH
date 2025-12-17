package com.arkflame.minekoth.commands.subcommands;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import com.arkflame.minekoth.MineKoth;
import com.arkflame.minekoth.koth.Koth;
import com.arkflame.minekoth.koth.events.KothEvent;
import com.arkflame.minekoth.koth.managers.KothManager;
import com.arkflame.minekoth.schedule.Schedule;

public class StartSubCommand implements SubCommand {
    @Override
    public boolean execute(CommandSender sender, String[] args) {
        Player player = sender instanceof Player ? (Player) sender : null;
        KothManager kothManager = MineKoth.getInstance().getKothManager();
        
        Koth koth = null;
        if (args.length >= 1) {
            String kothName = String.join(" ", args);
            koth = kothManager.getKothByName(kothName);
        } else {
            Schedule schedule = MineKoth.getInstance().getScheduleManager().getNextSchedule();
            if (schedule == null) {
                sender.sendMessage(MineKoth.getInstance().getLangManager().getLang(player)
                        .getMessage("messages.no-schedules"));
                return true;
            }
            koth = schedule.getKoth();
        }
        
        if (koth == null) {
            sender.sendMessage(MineKoth.getInstance().getLangManager().getLang(player)
                    .getMessage("messages.invalid-koth"));
            return true;
        }
        
        if (MineKoth.getInstance().getKothEventManager().getKothEvent(koth) != null) {
            sender.sendMessage(MineKoth.getInstance().getLangManager().getLang(player)
                    .getMessage("messages.koth-running"));
            return true;
        }
        
        MineKoth.getInstance().getKothEventManager().start(koth);
        sender.sendMessage(MineKoth.getInstance().getLangManager().getLang(player)
                .getMessage("messages.koth-schedule").replace("<n>", koth.getName()));
        sender.sendMessage(MineKoth.getInstance().getLangManager().getLang(player)
                .getMessage("messages.koth-running-list"));
        
        for (KothEvent k : MineKoth.getInstance().getKothEventManager().getRunningKoths()) {
            sender.sendMessage(MineKoth.getInstance().getLangManager().getLang(player)
                    .getMessage("messages.koth-running-item").replace("<n>", k.getKoth().getName()));
        }
        
        return true;
    }

    @Override
    public String getName() {
        return "start";
    }

    @Override
    public String getPermission() {
        return "minekoth.command.start";
    }

    @Override
    public boolean requiresPlayer() {
        return false;
    }
}