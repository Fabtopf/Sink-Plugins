/*
 * Copyright (c) 2013 - 2015 http://static-interface.de and contributors
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

package de.static_interface.sinkantispam.command;

import de.static_interface.sinkantispam.WarnUtil;
import de.static_interface.sinkantispam.database.row.Warning;
import de.static_interface.sinklibrary.SinkLibrary;
import de.static_interface.sinklibrary.api.command.SinkCommand;
import de.static_interface.sinklibrary.api.command.annotation.Aliases;
import de.static_interface.sinklibrary.api.command.annotation.DefaultPermission;
import de.static_interface.sinklibrary.api.command.annotation.Usage;
import de.static_interface.sinklibrary.api.configuration.Configuration;
import de.static_interface.sinklibrary.api.user.SinkUser;
import de.static_interface.sinklibrary.user.IngameUser;
import org.apache.commons.cli.ParseException;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.Plugin;

import java.util.List;

@Usage("<player> <warning-id>")
@Aliases({"delwarn", "deletewarn", "delwarning"})
@DefaultPermission
public class DeleteWarnCommand extends SinkCommand {

    public DeleteWarnCommand(Plugin plugin, Configuration config) {
        super(plugin, config);
        getCommandOptions().setIrcOpOnly(true);
    }

    @Override
    protected boolean onExecute(CommandSender sender, String label, String[] args) throws ParseException {
        if (args.length < 2) {
            return false;
        }

        IngameUser target = SinkLibrary.getInstance().getIngameUser(args[0]);

        int id = getArg(args, 1, Integer.class);

        SinkUser user = SinkLibrary.getInstance().getUser((Object) sender);

        List<Warning> warnings = WarnUtil.getWarnings(target, false);
        for (Warning warning : warnings) {
            if (warning.userWarningId == id) {
                WarnUtil.deleteWarning(warning, user);
                sender.sendMessage(ChatColor.DARK_GREEN + "Success");
                return true;
            }
        }

        sender.sendMessage(ChatColor.DARK_RED + "ID not found: " + id + "!");
        return true;
    }
}
