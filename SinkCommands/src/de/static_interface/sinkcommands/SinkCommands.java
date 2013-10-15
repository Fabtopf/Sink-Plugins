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

package de.static_interface.sinkcommands;

import de.static_interface.sinkcommands.commands.*;
import de.static_interface.sinkcommands.listener.*;
import de.static_interface.sinklibrary.SinkLibrary;
import de.static_interface.sinklibrary.User;
import de.static_interface.sinklibrary.configuration.PlayerConfiguration;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scoreboard.*;

import java.util.logging.Level;

public class SinkCommands extends JavaPlugin
{
    public static boolean globalmuteEnabled;

    private static CommandsTimer timer;

    SinkLibrary sinkLibrary;

    public void onEnable()
    {
        if ( !checkDependencies() )
        {
            return;
        }
        SinkLibrary.registerPlugin(this);

        timer = new CommandsTimer();
        LagTimer lagTimer = new LagTimer();

        Bukkit.getScheduler().scheduleSyncRepeatingTask(this, timer, 1000, 50);
        Bukkit.getScheduler().scheduleSyncRepeatingTask(this, lagTimer, 60000, 60000);
        registerEvents();
        registerCommands();
        Bukkit.getScheduler().scheduleSyncRepeatingTask(this, new Runnable()
        {

            @Override
            public void run()
            {
                refreshScoreboard();
            }
        }, 0, 20 * 30); //Update every 30 seconds

    }

    private boolean checkDependencies()
    {

        PluginManager pm = Bukkit.getPluginManager();
        try
        {
            sinkLibrary = (SinkLibrary) pm.getPlugin("SinkLibrary");
        }
        catch ( NoClassDefFoundError ignored )
        {
            sinkLibrary = null;
        }
        if ( sinkLibrary == null )
        {
            getLogger().log(Level.WARNING, "This Plugin requires SinkLibrary!");
            pm.disablePlugin(this);
            return false;
        }
        return true;
    }

    public void onDisable()
    {
        for ( Player p : SpectateCommands.specedPlayers.keySet() )
        {
            Player target = SpectateCommands.specedPlayers.get(p);
            target.eject();
            SpectateCommands.show(p);
            SpectateCommands.specedPlayers.remove(p);
            p.sendMessage(SpectateCommands.PREFIX + "Du wurdest durch einen Reload gezwungen den Spectate Modus zu verlassen.");
        }
        getLogger().info("Saving player configurations...");
        for ( Player p : Bukkit.getOnlinePlayers() )
        {
            User user = SinkLibrary.getUser(p);
            PlayerConfiguration config = user.getPlayerConfiguration();
            config.save();
        }
        getLogger().info("Done, disabled.");
    }

    /**
     * Refresh Scoarboard for all players
     */
    public static void refreshScoreboard()
    {
        refreshScoreboard(-1);
    }

    /**
     * Refresh Scoreboard for all players
     *
     * @param players Online Players
     */
    public static void refreshScoreboard(int players)
    {
        for ( Player p : Bukkit.getOnlinePlayers() )
        {
            refreshScoreboard(p, players);
        }
    }

    /**
     * Refresh scoreboard for specified player
     *
     * @param player Refresh scoreboard for this player
     */
    public static void refreshScoreboard(Player player, int players)
    {
        User user = SinkLibrary.getUser(player);
        PlayerConfiguration config = user.getPlayerConfiguration();

        if ( !config.exists() )
        {
            return;
        }

        ScoreboardManager manager = Bukkit.getScoreboardManager();

        if ( !user.hasPermission("sinkcommands.stats") || !config.getStatsEnabled() )
        {
            player.setScoreboard(manager.getNewScoreboard());
            return;
        }

        Scoreboard board = manager.getNewScoreboard();
        Team team = board.registerNewTeam(player.getName());
        Objective objective = board.registerNewObjective(ChatColor.DARK_GREEN + "Statistiken", "dummy");
        objective.setDisplaySlot(DisplaySlot.SIDEBAR);

        if ( SinkLibrary.economyAvailable() )
        {
            Score money = objective.getScore(Bukkit.getOfflinePlayer(ChatColor.DARK_GRAY + "Geld: "));
            money.setScore(user.getMoney());
        }

        Score onlinePlayers = objective.getScore(Bukkit.getOfflinePlayer(ChatColor.DARK_GRAY + "Online: "));

        if ( players >= 0 )
        {
            onlinePlayers.setScore(players);
        }
        else
        {
            onlinePlayers.setScore(Bukkit.getOnlinePlayers().length);
        }

        Score date = objective.getScore(Bukkit.getOfflinePlayer(ChatColor.DARK_GRAY + "Leben: "));
        date.setScore((int) player.getHealth());
        team.setAllowFriendlyFire(true);
        team.setCanSeeFriendlyInvisibles(false);
        player.setScoreboard(board);
    }

    /**
     * Get CommandsTimer
     *
     * @return CommandsTimer
     */
    public static CommandsTimer getCommandsTimer()
    {
        return timer;
    }

    private void registerEvents()
    {
        PluginManager pm = Bukkit.getPluginManager();
        pm.registerEvents(new FreezeListener(), this);
        pm.registerEvents(new GlobalMuteListener(), this);
        pm.registerEvents(new SpectateListener(), this);
        pm.registerEvents(new VotekickListener(), this);
        pm.registerEvents(new DrugDeadListener(), this);
        pm.registerEvents(new ScoreboardListener(), this);
    }

    private void registerCommands()
    {
        getCommand("commandsver").setExecutor(new CommandsverCommand(this));
        getCommand("drug").setExecutor(new DrugCommand());
        getCommand("milk").setExecutor(new MilkCommand());
        getCommand("warn").setExecutor(new WarnCommand());
        getCommand("freeze").setExecutor(new FreezeCommands.FreezeCommand());
        getCommand("freezelist").setExecutor(new FreezeCommands.FreezelistCommand());
        getCommand("globalmute").setExecutor(new GlobalmuteCommand());
        getCommand("teamchat").setExecutor(new TeamchatCommand());
        getCommand("newbiechat").setExecutor(new NewbiechatCommand());
        getCommand("spectate").setExecutor(new SpectateCommands.SpectateCommand());
        getCommand("unspectate").setExecutor(new SpectateCommands.UnspectateCommand());
        getCommand("spectatorlist").setExecutor(new SpectateCommands.SpectatorlistCommand());
        getCommand("lag").setExecutor(new LagCommand());
        getCommand("votekick").setExecutor(new VotekickCommands.VotekickCommand(this));
        getCommand("voteyes").setExecutor(new VotekickCommands.VoteyesCommand(this));
        getCommand("voteno").setExecutor(new VotekickCommands.VotenoCommand(this));
        getCommand("votestatus").setExecutor(new VotekickCommands.VotestatusCommand());
        getCommand("endvote").setExecutor(new VotekickCommands.EndvoteCommand(this));
        getCommand("votekickunban").setExecutor(new VotekickCommands.VotekickunbanCommand());
        getCommand("rename").setExecutor(new RenameCommand());
        getCommand("clear").setExecutor(new ClearCommand());
        getCommand("enablestats").setExecutor(new StatsCommands.EnableStatsCommand());
        getCommand("disablestats").setExecutor(new StatsCommands.DisableStatsCommand());
    }
}
