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

package de.static_interface.sinklibrary;

import de.static_interface.sinklibrary.commands.SinkDebugCommand;
import de.static_interface.sinklibrary.commands.SinkReloadCommand;
import de.static_interface.sinklibrary.configuration.LanguageConfiguration;
import de.static_interface.sinklibrary.configuration.PlayerConfiguration;
import de.static_interface.sinklibrary.configuration.Settings;
import de.static_interface.sinklibrary.events.IRCSendMessageEvent;
import de.static_interface.sinklibrary.listener.DisplayNameListener;
import de.static_interface.sinklibrary.listener.PlayerConfigurationListener;
import net.milkbowl.vault.chat.Chat;
import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.permission.Permission;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Level;

public class SinkLibrary extends JavaPlugin
{

    public static List<String> tmpBannedPlayers;

    private static Economy econ;
    private static Permission perm;
    private static Chat chat;
    private static File dataFolder;

    private static boolean economyAvailable = true;
    private static boolean permissionsAvailable = true;
    private static boolean chatAvailable = true;

    private static String pluginName;
    private static String version;
    private static Settings settings;
    private static List<JavaPlugin> registeredPlugins;
    private static HashMap<String, User> users;

    public void onEnable()
    {
        // Init variables first to prevent NullPointerExceptions when other plugins are trying to access them

        version = getDescription().getVersion();
        tmpBannedPlayers = new ArrayList<>();
        registeredPlugins = new ArrayList<>();
        users = new HashMap<>();


        // Check optional dependencies
        if ( !isVaultAvailable() )
        {
            Bukkit.getLogger().warning("Vault Plugin not found. Disabling economy and some permission features.");
            permissionsAvailable = false;
            economyAvailable = false;
            chatAvailable = false;
        }
        else
        {
            if ( !setupChat() )
            {
                chatAvailable = false;
            }

            if ( !setupEcononmy() )
            {
                Bukkit.getLogger().warning("Economy Plugin not found. Disabling economy features.");
                economyAvailable = false;
            }
            if ( !setupPermissions() )
            {
                Bukkit.getLogger().warning("Permissions Plugin not found. Disabling permissions features.");
                permissionsAvailable = false;
            }

            for ( Player p : Bukkit.getOnlinePlayers() )
            {
                refreshDisplayName(p);
            }
        }

        pluginName = this.getDescription().getName();
        dataFolder = getDataFolder();

        if ( !getCustomDataFolder().exists() )
        {
            try
            {
                getCustomDataFolder().mkdirs();
            }
            catch ( Exception e )
            {
                Bukkit.getLogger().log(Level.SEVERE, "Couldn't create Data Folder!", e);
            }
        }

        if ( chatAvailable && economyAvailable && permissionsAvailable )
        {
            Bukkit.getLogger().info("Successfully hooked into permissions, economy and chat.");
        }


        // Init language
        LanguageConfiguration.load();

        // Init Settings
        settings = new Settings();

        // Register Listeners and Commands
        registerListeners();
        registerCommands();

        // Check for updates
        update();

        // Init players
        for ( Player p : Bukkit.getOnlinePlayers() )
        {
            refreshDisplayName(p);
            loadUser(p.getName());
        }
    }

    public void onDisable()
    {
        Bukkit.getLogger().info("Saving players...");
        for ( Player p : Bukkit.getOnlinePlayers() )
        {
            User user = SinkLibrary.getUser(p);
            if ( user.getPlayerConfiguration().exists() )
            {
                user.getPlayerConfiguration().save();
            }
        }
        Bukkit.getLogger().info("Disabled.");
    }

    private void update()
    {
        Updater updater = new Updater(getSettings().getUpdateType());
        String permission = "sinklibrary.updatenotification";
        String versionType = " " + updater.getLatestGameVersion() + " ";
        if ( versionType.equalsIgnoreCase("release") ) versionType = " ";
        if ( updater.getResult() == Updater.UpdateResult.UPDATE_AVAILABLE )
        {
            BukkitUtil.broadcast(Updater.CONSOLEPREFIX + "A new" + versionType + "update is available: " + updater.getLatestName(), permission);
        }
        else if ( updater.getResult() == Updater.UpdateResult.NO_UPDATE )
        {
            Bukkit.getLogger().info(Updater.CONSOLEPREFIX + "No new updates found...");
        }
        else if ( updater.getResult() == Updater.UpdateResult.SUCCESS )
        {
            Bukkit.getLogger().info(Updater.CONSOLEPREFIX + "Updates downloaded, please restart or reload the server to take effect...");
        }
    }

    private boolean setupChat()
    {
        RegisteredServiceProvider<Chat> chatProvider = getServer().getServicesManager().getRegistration(Chat.class);
        if ( chatProvider != null )
        {
            chat = chatProvider.getProvider();
        }
        return (chat != null);
    }

    private boolean setupEcononmy()
    {
        RegisteredServiceProvider<Economy> rsp = getServer().getServicesManager().getRegistration(Economy.class);
        if ( rsp == null )
        {
            return false;
        }
        econ = rsp.getProvider();
        return econ != null;
    }

    private boolean setupPermissions()
    {
        RegisteredServiceProvider<Permission> permissionProvider = getServer().getServicesManager().getRegistration(Permission.class);
        if ( permissionProvider != null )
        {
            perm = permissionProvider.getProvider();
        }
        return (perm != null);
    }

    private void registerListeners()
    {
        Bukkit.getPluginManager().registerEvents(new PlayerConfigurationListener(), this);
        Bukkit.getPluginManager().registerEvents(new DisplayNameListener(), this);
    }

    private void registerCommands()
    {
        getCommand("sinkdebug").setExecutor(new SinkDebugCommand());
        getCommand("sinkreload").setExecutor(new SinkReloadCommand());
    }

    /**
     * @return True if chat is available
     */
    public static boolean isChatAvailable()
    {
        return chatAvailable;
    }

    /**
     * @return True if economy is available
     */
    public static boolean isEconomyAvailable()
    {
        return economyAvailable;
    }

    /**
     * @return True if permissions are available
     */
    public static boolean isPermissionsAvailable()
    {
        return permissionsAvailable;
    }

    /**
     * @return True if Vault available
     */
    public static boolean isVaultAvailable()
    {
        return Bukkit.getPluginManager().getPlugin("Vault") != null;
    }

    /**
     * Get Chat instance
     *
     * @return Chat instance
     * @see Chat
     */
    public static Chat getChat()
    {
        return chat;
    }

    /**
     * Get Economy instance from Vault
     *
     * @return Economy instace
     * @see Economy
     */
    public static Economy getEconomy()
    {
        return econ;
    }

    /**
     * Get Permissions instance
     *
     * @return Permissions
     * @see Permission
     */
    public static Permission getPermissions()
    {
        return perm;
    }

    /**
     * Get custom data folder
     *
     * @return Data Folder of Sink Plugins (.../plugins/SinkPlugins/)
     */
    public static File getCustomDataFolder()
    {
        return new File(dataFolder.getAbsolutePath().replace(pluginName, "SinkPlugins"));
    }

    /**
     * Send Message to IRC via SinkIRC Plugin.
     *
     * @param message Message to send
     */
    public static boolean sendIRCMessage(String message)
    {
        boolean ircAvailable = false;
        for ( Plugin plugin : getRegisteredPlugins() )
        {
            if ( plugin.getName().equals("SinkIRC") )
            {
                ircAvailable = true;
                break;
            }
        }
        if ( !ircAvailable ) return false;

        IRCSendMessageEvent event = new IRCSendMessageEvent(message);
        Bukkit.getPluginManager().callEvent(event);
        return true;
    }

    /**
     * Add Temp Ban to Player. Will be cleared on next server restart or plugin reload.
     *
     * @param username Player to ban
     */
    public static void addTempBan(String username)
    {
        tmpBannedPlayers.add(username);
    }

    /**
     * Remove Temp Ban
     *
     * @param username Player to unban
     */
    public static void removeTempBan(String username)
    {
        tmpBannedPlayers.remove(username);
    }

    /**
     * Get SinkPlugins Settings.
     *
     * @return Settings
     */
    public static Settings getSettings()
    {
        return settings;
    }

    /**
     * Register a plugin
     *
     * @param plugin Class which extends {@link org.bukkit.plugin.java.JavaPlugin}
     */
    public static void registerPlugin(JavaPlugin plugin)
    {
        registeredPlugins.add(plugin);
    }

    /**
     * @param player Player
     * @return User instance of player
     */
    public static User getUser(Player player)
    {
        return new User(player.getName());
        //return users.get(player.getName());
    }

    /**
     * @param playerName Name of Player
     * @return User instance
     */
    public static User getUser(String playerName)
    {
        return new User(playerName);
        //return users.get(playerName);
    }

    /**
     * @param sender Command Sender
     * @return User instance
     */
    public static User getUser(CommandSender sender)
    {
        return new User(sender.getName());
        //return users.get(sender.getName());
    }

    /**
     * Get Version of SinkLibrary
     *
     * @return Version
     */
    public static String getVersion()
    {
        return version;
    }

    /**
     * Refresh a player's DisplayName
     *
     * @param player Player that needs to refresh DisplayName
     */
    public static void refreshDisplayName(Player player)
    {
        if ( !getSettings().getDisplayNamesEnabled() ) return;
        User user = SinkLibrary.getUser(player);
        PlayerConfiguration config = user.getPlayerConfiguration();

        if ( !config.exists() )
        {
            return;
        }

        String nickname = user.getDisplayName();

        if ( nickname == null || nickname.equals("null") || nickname.equals("") )
        {
            config.setDisplayName(user.getDefaultDisplayName());
            config.setHasDisplayName(false);
        }
        else
        {
            nickname = user.getDefaultDisplayName();
        }

        if ( nickname.equals(user.getDefaultDisplayName()) )
        {
            config.setHasDisplayName(false);
        }

        player.setDisplayName(nickname);
        player.setCustomName(nickname);
    }

    /**
     * @return Plugins which are registered through {@link SinkLibrary#registerPlugin(org.bukkit.plugin.java.JavaPlugin)}
     */
    public static List<JavaPlugin> getRegisteredPlugins()
    {
        return registeredPlugins;
    }

    /**
     * Loads an User
     *
     * @param name Name of the player who needs to be loaded
     */
    public static void loadUser(String name)
    {
        if ( !users.containsKey(name) )
        {
            users.put(name, new User(name));
        }
    }

    /**
     * Loads an User
     *
     * @param player Name of the player who needs to be loaded
     */
    public static void loadUser(Player player)
    {
        String name = player.getName();
        if ( !users.containsKey(name) )
        {
            users.put(name, new User(name));
        }
    }

    /**
     * Unload an User
     *
     * @param player Player which needs to be unloaded
     */
    public static void unloadUser(Player player)
    {
        User user = getUser(player);
        user.getPlayerConfiguration().save();
        String name = user.getPlayer().getName();
        if ( name != null && !name.equals("") )
        {
            users.remove(name);
        }
    }

    /**
     * Get the name of this plugin
     *
     * @return the name of this plugin
     */
    public static String getPluginName()
    {
        return pluginName;
    }

    /**
     * Get all online players
     *
     * @return Online players as Users
     */
   /*
    public static Collection<User> getOnlineUsers()
    {
        users.clear();
        for (Player player : Bukkit.getOnlinePlayers())
        {
            loadUser(player);
        }
        return users.values();
    }
    */

    /**
     * Get Users HashMap
     */
    public static HashMap<String, User> getUsers()
    {
        return users;
    }
}
