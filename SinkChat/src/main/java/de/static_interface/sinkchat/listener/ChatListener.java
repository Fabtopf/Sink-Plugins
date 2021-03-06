/*
 * Copyright (c) 2013 - 2014 http://static-interface.de and contributors
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package de.static_interface.sinkchat.listener;

import de.static_interface.sinkchat.SinkChat;
import de.static_interface.sinkchat.TownyHelper;
import de.static_interface.sinkchat.Util;
import de.static_interface.sinkchat.channel.Channel;
import de.static_interface.sinkchat.channel.ChannelHandler;
import de.static_interface.sinkchat.config.ScSettings;
import de.static_interface.sinklibrary.SinkLibrary;
import de.static_interface.sinklibrary.api.stream.MessageStream;
import de.static_interface.sinklibrary.api.user.SinkUser;
import de.static_interface.sinklibrary.user.IngameUser;
import de.static_interface.sinklibrary.util.StringUtil;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;

import java.util.HashMap;
import java.util.HashSet;
import java.util.logging.Level;

import javax.annotation.Nullable;

public class ChatListener implements Listener {

    public ChatListener() {
        SinkLibrary.getInstance().registerMessageStream(new MessageStream("sc_spy") {
            @Override
            protected boolean onSendMessage(@Nullable SinkUser user, String message, Object... args) {
                return true;
            }
        });
    }
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onAsyncPlayerChat(AsyncPlayerChatEvent event) {
        try {
            handleChat(event);
        } catch (RuntimeException e) {
            SinkChat.getInstance().getLogger().log(Level.SEVERE, "Warning! Unexpected exception occurred", e);
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void handleChatMonitor(AsyncPlayerChatEvent event) {
        //for (Player p : new HashSet<>(event.getRecipients())) {
        //    p.sendMessage(event.getFormat().replace("%1$s", event.getPlayer().getDisplayName()).replace("%2$s", event.getMessage()));
        //}
        //
        //event.setCancelled(true);
    }

    private void handleChat(AsyncPlayerChatEvent event) {
        if (event.isCancelled()) {
            return;
        }


        IngameUser user = SinkLibrary.getInstance().getIngameUser(event.getPlayer());
        String message = event.getMessage();

        if (user.hasPermission("sinkchat.color")) {
            message = ChatColor.translateAlternateColorCodes('&', message);
            event.setMessage(message);
        }

        Channel channel;
        for (String callChar : ChannelHandler.getRegisteredChannels().keySet()) {
            channel = ChannelHandler.getRegisteredChannel(callChar);
            if (!StringUtil.isEmptyOrNull(callChar) &&
                event.getMessage().startsWith(callChar) &&
                !event.getMessage().equalsIgnoreCase(callChar)) {

                if (!channel.enabledForPlayer(event.getPlayer().getUniqueId())) {
                    continue;
                }

                String msg = event.getMessage();
                event.setFormat(channel.formatEventFormat(user));
                event.setMessage(channel.formatMessage(event.getMessage()));
                channel.handleMessage(user, event.getRecipients(), message);
                channel.sendMessage(user, msg, true);
                return;
            }
        }

        int range = ScSettings.SC_LOCAL_CHAT_RANGE.getValue();

        HashMap<String, Object> customParams = new HashMap<>();
        if (SinkChat.getInstance().isTownyAvailable()) {
            customParams.put("NATIONTAG", TownyHelper.getNationTag(event.getPlayer()));
            customParams.put("TOWN(Y)?TAG", TownyHelper.getTownTag(event.getPlayer()));
            customParams.put("TOWN(Y)?", TownyHelper.getTown(event.getPlayer()));
            customParams.put("NATION", TownyHelper.getNation(event.getPlayer()));
        }
        customParams.put("CHANNEL", ScSettings.SC_PREFIX_LOCAL.format());

        String eventFormat = ScSettings.SC_DEFAULT_CHAT_FORMAT.getValue();
        //String eventFormat = format.replaceAll("\\{((PLAYER(NAME)?)|DISPLAYNAME|NAME|FORMATTEDNAME)\\}", "\\$1\\%s");
        //eventFormat = eventFormat.replaceAll("\\{MESSAGE\\}", "\\$2\\%s");
        eventFormat = StringUtil.format(eventFormat, user, null, "%2$s", customParams, false, null);

        if (!SinkLibrary.getInstance().isPermissionsAvailable()) {
            eventFormat = ScSettings.SC_PREFIX_LOCAL.format() + ' ' + ChatColor.RESET + eventFormat;
        }

        event.setFormat(eventFormat);

        String spyMessage = Util.getSpyPrefix() + eventFormat.replace("%1$s", user.getDisplayName()).replace("%2$s", event.getMessage());
        //String spyMessage = Util.getSpyPrefix() + eventFormat;

        for (Player p : new HashSet<>(event.getRecipients())) {
            if (!Util.isInRange(user, p, range)) {
                event.getRecipients().remove(p);
            }
        }

        for (Player p : Bukkit.getOnlinePlayers()) {
            if (event.getRecipients().contains(p)) {
                continue;
            }

            IngameUser target = SinkLibrary.getInstance().getIngameUser(p);

            if (!Util.isInRange(user, p, range) && Util.canSpySender(target, user)) {
                p.sendMessage(spyMessage);
            }
        }

        SinkLibrary.getInstance().getMessageStream("sc_spy").sendMessage(spyMessage);

        //Bukkit.getConsoleSender().sendMessage(spyMessage);
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onPlayerCommandPreprocess(PlayerCommandPreprocessEvent event) {
        IngameUser user = SinkLibrary.getInstance().getIngameUser(event.getPlayer());
        if (user.hasPermission("sinkchat.color")) {
            event.setMessage(ChatColor.translateAlternateColorCodes('&', event.getMessage()));
        }
    }
}