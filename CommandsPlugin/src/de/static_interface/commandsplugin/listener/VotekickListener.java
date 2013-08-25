package de.static_interface.commandsplugin.listener;

import de.static_interface.commandsplugin.CommandsPlugin;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;

public class VotekickListener implements Listener
{
    @EventHandler(priority = EventPriority.LOWEST)
    public void onAsyncPlayerPreLogin(AsyncPlayerPreLoginEvent event)
    {
        if (CommandsPlugin.tmpBannedPlayers.contains(event.getName()))
        {
            event.disallow(AsyncPlayerPreLoginEvent.Result.KICK_BANNED, "Du wurdest wegen einem Votekick für 5 Minuten gebannt.");
        }
    }
}
