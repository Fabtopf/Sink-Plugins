package de.static_interface.ircplugin;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.*;
import org.bukkit.plugin.java.JavaPlugin;
import org.jibble.pircbot.IrcException;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Logger;

/**
 * IRCPluginMain
 * Description: Main Class
 * Date: 21.07.13
 * Author: Trojaner
 * Copyright © 2013 Trojaner
 */

@SuppressWarnings({ "UnusedDeclaration", "FieldCanBeLocal" })
public class IRCPluginMain extends JavaPlugin implements Listener
{
    private String Host = "irc.lolnein.de";
    private String Channel = "#IRCBot";
    private int Port = 6667;

    IRCBot ircBot;
    Logger log;

    @Override
    public void onEnable()
    {
        getServer().getPluginManager().registerEvents(this, this);

        log = getLogger();
        ircBot = new IRCBot();

        try
        {
            ircBot.connect(Host, 6667);
            ircBot.joinChannel(Channel);
        }
        catch (IOException | IrcException e)
        {
            log.severe("An Exception occurred while trying to connect to " + Host + ":");
            log.severe(e.toString());
        }
    }

    @Override
    public void onDisable()
    {
        ircBot.quitServer("Plugin deactivated");
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerJoin(PlayerJoinEvent event)
    {
        ircBot.sendCleanMessage(Channel, event.getJoinMessage());
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerQuit(PlayerQuitEvent event)
    {
        ircBot.sendCleanMessage(Channel, event.getQuitMessage());
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerKick(PlayerKickEvent event)
    {
        if (! event.getReason().equals(""))
        {
            ircBot.sendCleanMessage(Channel, "Player \"" + event.getPlayer().getDisplayName() + "\" has been kicked for reason: " + event.getReason());
        }
        else
        {
            ircBot.sendCleanMessage(Channel, "Player \"" + event.getPlayer().getDisplayName() + "\" has been kicked!");
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerDeath(PlayerDeathEvent event)
    {
        ircBot.sendCleanMessage(Channel, event.getDeathMessage());
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerChat(AsyncPlayerChatEvent event)
    {
        if (ircBot == null)
        {
            return;
        }
        if (! event.getMessage().startsWith("!"))
        {
            return;
        }
        String message = event.getMessage().replaceFirst("!", "");

        ircBot.sendCleanMessage(Channel, "[" + event.getPlayer().getDisplayName() + "] " + message);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerCommandPreprocess(PlayerCommandPreprocessEvent event)
    {
        ircBot.sendMessage(Channel, "-> onPlayerCommandPreprocess(PlayerCommandPreprocessEvent event): " + event.getMessage());
        String tmp = event.getMessage().replaceFirst("/", "");
        String[] cmdwithargs = tmp.split(" ");
        List<String> args = Arrays.asList(cmdwithargs);
        String cmd = cmdwithargs[0];
        args.remove(cmd); //Remove command from args
        switch (cmd.toLowerCase())
        {
            case "say":
            {
                String message = "";
                for (String s : args)
                {
                    if (message.equals(""))
                    {
                        message = s;
                    }
                    else
                    {
                        message = message + " " + s;
                    }
                }
                ircBot.sendCleanMessage(Channel, "[Server] " + message);
            }
        }
        event.setCancelled(false);
    }
}
