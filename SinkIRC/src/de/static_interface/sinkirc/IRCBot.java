/*
 * Copyright (c) 2013 adventuria.eu / static-interface.de
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package de.static_interface.sinkirc;

import de.static_interface.sinklibrary.BukkitUtil;
import de.static_interface.sinklibrary.SinkLibrary;
import de.static_interface.sinklibrary.events.*;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.jibble.pircbot.Colors;
import org.jibble.pircbot.PircBot;
import org.jibble.pircbot.User;

public class IRCBot extends PircBot
{
    public static final String IRC_PREFIX = ChatColor.GRAY + "[IRC] " + ChatColor.RESET;
    private static boolean disabled = false;
    private Plugin plugin;

    public IRCBot(Plugin plugin)
    {
        String botName = "AdventuriaBot";
        this.setName(botName);
        this.setLogin(botName);
        this.setVersion("SinkIRC for Bukkit, visit http://dev.bukkit.org/bukkit-plugins/sink-plugins/");
        this.plugin = plugin;
    }

    public static String replaceColorCodes(String input)
    {
        input = input.replace(ChatColor.BLACK.toString(), Colors.BLACK);
        input = input.replace(ChatColor.DARK_BLUE.toString(), Colors.DARK_BLUE);
        input = input.replace(ChatColor.DARK_GREEN.toString(), Colors.DARK_GREEN);
        input = input.replace(ChatColor.DARK_AQUA.toString(), Colors.TEAL);
        input = input.replace(ChatColor.DARK_RED.toString(), Colors.RED);
        input = input.replace(ChatColor.DARK_PURPLE.toString(), Colors.PURPLE);
        input = input.replace(ChatColor.GOLD.toString(), Colors.OLIVE);
        input = input.replace(ChatColor.GRAY.toString(), Colors.LIGHT_GRAY);
        input = input.replace(ChatColor.DARK_GRAY.toString(), Colors.DARK_GRAY);
        input = input.replace(ChatColor.BLUE.toString(), Colors.BLUE);
        input = input.replace(ChatColor.GREEN.toString(), Colors.GREEN);
        input = input.replace(ChatColor.AQUA.toString(), Colors.CYAN);
        input = input.replace(ChatColor.RED.toString(), Colors.RED);
        input = input.replace(ChatColor.LIGHT_PURPLE.toString(), Colors.PURPLE);
        input = input.replace(ChatColor.YELLOW.toString(), Colors.YELLOW);
        input = input.replace(ChatColor.WHITE.toString(), Colors.NORMAL);
        input = input.replace("§k", "");
        input = input.replace(ChatColor.BOLD.toString(), Colors.BOLD);
        input = input.replace(ChatColor.STRIKETHROUGH.toString(), "");
        input = input.replace(ChatColor.UNDERLINE.toString(), Colors.UNDERLINE);
        input = input.replace(ChatColor.ITALIC.toString(), "");
        input = input.replace(ChatColor.RESET.toString(), Colors.NORMAL);
        return input;
    }

    public void sendCleanMessage(String target, String message)
    {
        message = replaceColorCodes(message);
        sendMessage(target, message);
    }

    @Override
    public void onJoin(String channel, String sender, String login, String hostname)
    {
        IRCJoinEvent event = new IRCJoinEvent(channel, sender, login, hostname);
        if ( disabled )
        {
            event.setCancelled(true);
        }
        Bukkit.getPluginManager().callEvent(event);
    }

    @Override
    public void onPart(String channel, String sender, String login, String hostname)
    {
        IRCPartEvent event = new IRCPartEvent(channel, sender, login, hostname);
        if ( disabled )
        {
            event.setCancelled(true);
        }
        Bukkit.getPluginManager().callEvent(event);
    }

    @Override
    public void onKick(String channel, String kickerNick, String kickerLogin, String kickerHostname, String recipientNick, String reason)
    {
        IRCKickEvent event = new IRCKickEvent(channel, kickerNick, kickerLogin, kickerHostname, recipientNick, reason);
        if ( disabled )
        {
            event.setCancelled(true);
        }
        Bukkit.getPluginManager().callEvent(event);
    }

    @Override
    public void onQuit(String sourceNick, String sourceLogin, String sourceHostname, String reason)
    {
        IRCQuitEvent event = new IRCQuitEvent(sourceNick, sourceLogin, sourceHostname, reason);
        if ( disabled )
        {
            event.setCancelled(true);
        }
        Bukkit.getPluginManager().callEvent(event);
    }

    @Override
    public void onNickChange(String oldNick, String login, String hostname, String newNick)
    {
        IRCNickChangeEvent event = new IRCNickChangeEvent(oldNick, login, hostname, newNick);
        if ( disabled )
        {
            event.setCancelled(true);
        }
        Bukkit.getPluginManager().callEvent(event);
    }

    @Override
    public void onPrivateMessage(String sender, String login, String hostname, String message)
    {
        IRCPrivateMessageEvent event = new IRCPrivateMessageEvent(sender, login, hostname, message);
        if ( disabled )
        {
            event.setCancelled(true);
        }
        Bukkit.getPluginManager().callEvent(event);
    }

    @Override
    public void onMessage(String channel, String sender, String login, String hostname, String message)
    {
        IRCReceiveMessageEvent event = new IRCReceiveMessageEvent(channel, sender, login, hostname, message);
        if ( disabled )
        {
            event.setCancelled(true);
        }
        Bukkit.getPluginManager().callEvent(event);
    }

    @Override
    public void onPing(String sourceNick, String sourceLogin, String sourceHostname, String target, String pingValue)
    {
        IRCPingEvent event = new IRCPingEvent(sourceNick, sourceLogin, sourceHostname, target, pingValue);
        if ( disabled )
        {
            event.setCancelled(true);
        }
        Bukkit.getPluginManager().callEvent(event);
    }


    public static boolean isOp(String channel, String user, IRCBot ircBot)
    {
        for ( User u : ircBot.getUsers(channel) )
        {
            if ( u.isOp() && u.getNick().equals(user) )
            {
                return true;
            }
        }
        return false;
    }

    public static void executeCommand(String command, String[] args, String source, String sender, String label, IRCBot ircBot)
    {
        try
        {
            boolean isOp = isOp(SinkIRC.getMainChannel(), sender, ircBot);

            if ( command.equals("toggle") )
            {
                if ( !isOp )
                {
                    throw new UnauthorizedAccessException();
                }
                disabled = !disabled;
                if ( disabled )
                {
                    ircBot.sendMessage(source, "Disabled " + ircBot.getName());
                }
                else
                {
                    ircBot.sendMessage(source, "Enabled " + ircBot.getName());
                }
            }

            if ( disabled )
            {
                return;
            }

            if ( command.equals("exec") ) //Execute command as console
            {
                if ( !isOp ) throw new UnauthorizedAccessException();
                String commandWithArgs = "";
                int i = 0;
                for ( String arg : args )
                {
                    if ( i == args.length - 1 )
                    {
                        break;
                    }
                    i++;
                    if ( commandWithArgs.equals("") )
                    {
                        commandWithArgs = arg;
                        continue;
                    }
                    commandWithArgs = commandWithArgs + " " + arg;
                }


                final String finalCommandWithArgs = commandWithArgs;

                Bukkit.getScheduler().runTask(ircBot.plugin, new Runnable()
                {
                    @Override
                    public void run()
                    {
                        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), finalCommandWithArgs);
                    }
                });

                ircBot.sendMessage(source, "Executed command: \"" + commandWithArgs + "\"");
            }

            if ( command.equals("say") ) //Speak to ingame players
            {
                boolean privateMessageCommand = !source.startsWith("#");

                if ( args.length < 2 )
                {
                    ircBot.sendCleanMessage(source, "Usage: !say <text>");
                    return;
                }

                if ( privateMessageCommand )
                {
                    source = "Query";
                }

                String messageWithPrefix;

                if ( isOp )
                {
                    messageWithPrefix = IRC_PREFIX + ChatColor.GRAY + "[" + source + "] " + ChatColor.DARK_AQUA + sender + ChatColor.GRAY + ": " + ChatColor.WHITE + label.replaceFirst("say", "");
                }
                else
                {
                    messageWithPrefix = IRC_PREFIX + ChatColor.GRAY + "[" + source + "] " + ChatColor.DARK_AQUA + sender + ChatColor.GRAY + ": " + ChatColor.WHITE + ChatColor.stripColor(label.replaceFirst("say", ""));
                }

                BukkitUtil.broadcastMessage(messageWithPrefix);
                //sendCleanMessage(SinkIRC.getMainChannel(), replaceColorCodes(messageWithPrefix));
            }

            if ( command.equals("kick") )  //Kick players from IRC
            {
                if ( !isOp )
                {
                    throw new UnauthorizedAccessException();
                }
                String targetPlayerName;
                try
                {
                    targetPlayerName = args[0];
                }
                catch ( Exception e )
                {
                    ircBot.sendCleanMessage(source, "Usage: !kick <player> <reason>");
                    return;
                }
                final Player targetPlayer = BukkitUtil.getPlayer(targetPlayerName);
                if ( targetPlayer == null )
                {
                    ircBot.sendCleanMessage(source, "Player \"" + targetPlayerName + "\" is not online!");
                    return;
                }

                String formattedReason;
                if ( args.length > 1 )
                {
                    String reason = label.replace(targetPlayerName, "");
                    reason = reason.replace("kick ", "");
                    formattedReason = " (Reason: " + reason + ")";
                }
                else
                {
                    formattedReason = ".";
                }
                formattedReason = ChatColor.translateAlternateColorCodes('&', formattedReason);
                final String finalReason = "Kicked by " + sender + " from IRC" + formattedReason;
                Bukkit.getScheduler().runTask(ircBot.plugin, new Runnable()
                {
                    @Override
                    public void run()
                    {
                        targetPlayer.kickPlayer(finalReason);
                    }
                });
                BukkitUtil.broadcastMessage(finalReason);

            }

            if ( command.equals("list") ) //List Players
            {
                String players = "";
                if ( Bukkit.getOnlinePlayers().length == 0 )
                {
                    ircBot.sendCleanMessage(source, "There are currently no online players");
                    return;
                }

                for ( Player player : Bukkit.getOnlinePlayers() )
                {
                    de.static_interface.sinklibrary.User user = SinkLibrary.getUser(player);
                    if ( players.equals("") )
                    {
                        players = user.getDisplayName();
                    }
                    else
                    {
                        players = players + ", " + user.getDisplayName();
                    }
                }
                ircBot.sendCleanMessage(source, "Online Players (" + Bukkit.getOnlinePlayers().length + "/" + Bukkit.getMaxPlayers() + "): " + players);
            }

            if ( command.equals("debug") )
            {
                if ( !isOp ) throw new UnauthorizedAccessException();
                ircBot.sendCleanMessage(source, Colors.BLUE + "Debug Output: ");
                String values = "";
                for ( String user : SinkLibrary.getUsers().keySet() )
                {
                    String tmp = "<" + user + "," + ChatColor.stripColor(SinkLibrary.getUsers().get(user).getDisplayName()) + ">";
                    if ( values.equals("") )
                    {
                        values = tmp;
                        continue;
                    }
                    values = values + ", " + tmp;
                }
                ircBot.sendCleanMessage(source, "HashMap Values: " + values);
            }
        }
        catch ( UnauthorizedAccessException e )
        {
            ircBot.sendMessage(source, "You may not use that command");
        }
        catch ( Exception e )
        {
            ircBot.sendMessage(source, "Unexpected exception occured while trying to execute command: " + command);
            ircBot.sendMessage(source, e.getMessage());
        }
    }

    public static boolean isDisabled()
    {
        return disabled;
    }
}
