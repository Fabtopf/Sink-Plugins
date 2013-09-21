package de.static_interface.sinkcommands.commands;

import de.static_interface.sinkcommands.SinkCommands;
import de.static_interface.sinkcommands.Util;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class NewbiechatCommand implements CommandExecutor
{
    public static final String PREFIX = ChatColor.YELLOW + "[SupportChat] " + ChatColor.RESET;

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args)
    {
        if (args.length < 1)
        {
            return false;
        }
        String message = Util.formatArrayToString(args, " ");

        SinkCommands.broadcast(PREFIX + SinkCommands.getSenderName(sender) + ChatColor.WHITE + ": " + message, "sinkcommands.newbiechat");
        return true;
    }
}