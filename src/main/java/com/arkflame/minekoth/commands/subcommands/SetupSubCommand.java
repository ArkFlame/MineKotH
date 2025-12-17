package com.arkflame.minekoth.commands.subcommands;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import com.arkflame.minekoth.setup.commands.SetupCommand;

public class SetupSubCommand implements SubCommand {
    @Override
    public boolean execute(CommandSender sender, String[] args) {
        Player player = (Player) sender;
        
        // Create new args array including "setup" as first element
        String[] fullArgs = new String[args.length + 1];
        fullArgs[0] = "setup";
        System.arraycopy(args, 0, fullArgs, 1, args.length);
        
        SetupCommand.run(player, fullArgs);
        return true;
    }

    @Override
    public String getName() {
        return "setup";
    }

    @Override
    public String getPermission() {
        return "minekoth.command.setup";
    }

    @Override
    public boolean requiresPlayer() {
        return true;
    }
}