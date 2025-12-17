package com.arkflame.minekoth.commands.subcommands;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import com.arkflame.minekoth.schedule.commands.ScheduleCommand;

public class ScheduleSubCommand implements SubCommand {
    @Override
    public boolean execute(CommandSender sender, String[] args) {
        Player player = (Player) sender;
        
        // Create new args array including "schedule" as first element
        String[] fullArgs = new String[args.length + 1];
        fullArgs[0] = "schedule";
        System.arraycopy(args, 0, fullArgs, 1, args.length);
        
        ScheduleCommand.run(player, fullArgs);
        return true;
    }

    @Override
    public String getName() {
        return "schedule";
    }

    @Override
    public String getPermission() {
        return "minekoth.command.schedule";
    }

    @Override
    public boolean requiresPlayer() {
        return true;
    }
}