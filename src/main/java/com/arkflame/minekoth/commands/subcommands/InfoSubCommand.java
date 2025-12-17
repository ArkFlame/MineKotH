package com.arkflame.minekoth.commands.subcommands;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import com.arkflame.minekoth.MineKoth;
import com.arkflame.minekoth.koth.Koth;
import com.arkflame.minekoth.koth.managers.KothManager;

public class InfoSubCommand implements SubCommand {
    @Override
    public boolean execute(CommandSender sender, String[] args) {
        Player player = sender instanceof Player ? (Player) sender : null;
        KothManager kothManager = MineKoth.getInstance().getKothManager();
        
        if (args.length < 1) {
            sender.sendMessage(MineKoth.getInstance().getLangManager().getLang(player)
                    .getMessage("messages.usage-koth-info"));
            return true;
        }
        
        try {
            int id = Integer.parseInt(args[0]);
            Koth koth = kothManager.getKothById(id);
            
            if (koth == null) {
                sender.sendMessage(MineKoth.getInstance().getLangManager().getLang(player)
                        .getMessage("messages.no-koth-id").replace("<id>", String.valueOf(id)));
                return true;
            }
            
            MineKoth.getInstance().getLangManager().sendMessage(player, "messages.koth-info",
                    "<id>", koth.getId(),
                    "<name>", koth.getName(),
                    "<world>", koth.getWorldName(),
                    "<time_limit>", koth.getTimeLimit(),
                    "<time_to_capture>", koth.getTimeToCapture(),
                    "<reward_items>", koth.getRewards().getRewardsItems().size(),
                    "<reward_commands>", koth.getRewards().getRewardsCommands().toString());
        } catch (NumberFormatException e) {
            sender.sendMessage(
                    MineKoth.getInstance().getLangManager().getLang(player).getMessage("messages.invalid-id"));
        }
        
        return true;
    }

    @Override
    public String getName() {
        return "info";
    }

    @Override
    public String getPermission() {
        return "minekoth.command.info";
    }

    @Override
    public boolean requiresPlayer() {
        return false;
    }
}