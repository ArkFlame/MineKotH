package com.arkflame.minekoth.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.arkflame.minekoth.MineKoth;
import com.arkflame.minekoth.commands.subcommands.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Arrays;

public class KothCommand implements CommandExecutor {
    private final Map<String, SubCommand> subCommands = new HashMap<>();
    
    public KothCommand() {
        registerSubCommands();
    }
    
    private void registerSubCommands() {
        // Register all subcommands
        registerSubCommand(new HelpSubCommand());
        registerSubCommand(new ReloadSubCommand());
        registerSubCommand(new SetupSubCommand());
        registerSubCommand(new ListSubCommand());
        registerSubCommand(new InfoSubCommand());
        registerSubCommand(new DeleteSubCommand());
        registerSubCommand(new ScheduleSubCommand());
        registerSubCommand(new StartSubCommand());
        
        // Stop command with alias "end"
        StopSubCommand stopCmd = new StopSubCommand();
        subCommands.put("stop", stopCmd);
        subCommands.put("end", stopCmd);
        
        registerSubCommand(new CaptureSubCommand());
        
        // Teleport command with alias "tp"
        TeleportSubCommand tpCmd = new TeleportSubCommand();
        subCommands.put("teleport", tpCmd);
        subCommands.put("tp", tpCmd);
        
        registerSubCommand(new BetSubCommand());
        registerSubCommand(new StatsSubCommand());
    }
    
    private void registerSubCommand(SubCommand subCommand) {
        subCommands.put(subCommand.getName().toLowerCase(), subCommand);
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        // Show help if no args or help subcommand
        if (args.length == 0 || args[0].equalsIgnoreCase("help")) {
            SubCommand helpCmd = subCommands.get("help");
            if (helpCmd != null) {
                helpCmd.execute(sender, new String[0]);
            }
            return true;
        }
        
        // Get the subcommand
        String subCommandName = args[0].toLowerCase();
        SubCommand subCommand = subCommands.get(subCommandName);
        
        if (subCommand == null) {
            Player player = sender instanceof Player ? (Player) sender : null;
            sender.sendMessage(MineKoth.getInstance().getLangManager().getLang(player)
                    .getMessage("messages.unknown-subcommand"));
            return true;
        }
        
        // Check if command requires player but sender is not a player
        if (subCommand.requiresPlayer() && !(sender instanceof Player)) {
            sender.sendMessage(
                    MineKoth.getInstance().getLangManager().getLang(null).getMessage("messages.players-only"));
            return true;
        }
        
        // Check permission
        String permission = subCommand.getPermission();
        if (permission != null && !sender.hasPermission(permission)) {
            Player player = sender instanceof Player ? (Player) sender : null;
            sender.sendMessage(
                    MineKoth.getInstance().getLangManager().getLang(player).getMessage("messages.no-permission")
                            .replace("<node>", permission));
            return true;
        }
        
        // Execute the subcommand with remaining args
        String[] subArgs = Arrays.copyOfRange(args, 1, args.length);
        return subCommand.execute(sender, subArgs);
    }
}