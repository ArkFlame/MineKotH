package com.arkflame.minekoth.commands.subcommands;

import org.bukkit.command.CommandSender;

public interface SubCommand {
    /**
     * Execute the subcommand
     * @param sender The command sender
     * @param args The command arguments (excluding the subcommand name)
     * @return true if the command was handled successfully
     */
    boolean execute(CommandSender sender, String[] args);
    
    /**
     * Get the name of the subcommand
     * @return The subcommand name
     */
    String getName();
    
    /**
     * Get the permission required to use this subcommand
     * @return The permission node, or null if no permission is required
     */
    String getPermission();
    
    /**
     * Check if this command requires a player sender
     * @return true if only players can use this command
     */
    boolean requiresPlayer();
}