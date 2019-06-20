package usa.cactuspuppy.admit;

import lombok.Getter;
import lombok.Setter;
import org.apache.commons.io.FileUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.PluginCommand;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.event.server.ServerListPingEvent;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.plugin.java.JavaPluginLoader;
import usa.cactuspuppy.admit.utils.Config;
import usa.cactuspuppy.admit.utils.Logger;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;
import java.util.Objects;

public class Main extends JavaPlugin implements Listener {
    @Getter
    private static Main instance;

    @Getter
    private Config mainConfig;

    @Getter @Setter
    private boolean bypassEnabled;

    @Getter @Setter
    private static BypassMode bypassMode;

    @Override
    public void onEnable() {
        long start = System.nanoTime();
        instance = this;
        Logger.setOutput(this.getLogger());
        if (!createConfig()) {
            abortSetup();
            return;
        }
        mainConfig = new Config();
        if (mainConfig.getInitCode() != 0) {
            abortSetup();
            return;
        }
        if (!processConfig()) {
            abortSetup();
            return;
        }
        PluginCommand command = getCommand("admit");
        if (command == null) {
            Logger.logWarning(this.getClass(), "Unable to register command handler");
        } else {
            AdmitCommand handler = new AdmitCommand();
            command.setExecutor(handler);
            command.setTabCompleter(handler);
        }
        if (bypassEnabled) {
            try {
                Bukkit.getPluginManager().registerEvents(this, this);
            } catch (NullPointerException e) {
                Logger.logWarning(this.getClass(), "Unable to register login listener!");
            }
        }
        long elapsedNanos = System.nanoTime() - start;
        Logger.logInfo(this.getClass(), ChatColor.AQUA + "Admit" + ChatColor.GREEN + " startup complete!\n");
        Logger.logInfo(this.getClass(), String.format(
                ChatColor.LIGHT_PURPLE + "Time Elapsed: " + ChatColor.GOLD + "%1$.2fms (%2$dns)",
                elapsedNanos / 10e6, elapsedNanos));
    }

    private void abortSetup() {
        Logger.logSevere(this.getClass(), "Critical error on plugin startup, disabling...");
        Bukkit.getPluginManager().disablePlugin(this);
    }

    @Override
    public void saveDefaultConfig() {
        //Do nothing, this breaks custom config setup
    }

    private boolean createConfig() {
        //Get/create main config
        File dataFolder = Main.getInstance().getDataFolder();
        if (!dataFolder.isDirectory() && !dataFolder.mkdirs()) {
            Logger.logSevere(this.getClass(), "Could not find or create data folder.");
            return false;
        }
        File config = new File(Main.getInstance().getDataFolder(), "config.yml");
        //Create config if not exist
        if (!config.isFile()) {
            InputStream inputStream = getResource("config.yml");
            if (inputStream == null) {
                Logger.logSevere(this.getClass(), "No packaged config.yml?!");
                return false;
            }
            try {
                FileUtils.copyToFile(inputStream, config);
            } catch (IOException e) {
                Logger.logSevere(this.getClass(), "Error while creating new config", e);
                return false;
            }
        }
        return true;
    }

    /**
     * Checks that the config contains the necessary keys for operation,
     * fixing any values which are not expected, and taking action on
     * the values in the config.
     * @return Whether the plugin can resume startup
     */
    private boolean processConfig() {
        try {
            String value = mainConfig.get("enabled");
            if (value.equalsIgnoreCase("true")) {
                bypassEnabled = true;
            } else if (value.equalsIgnoreCase("false")) {
                bypassEnabled = false;
            } else {
                throw new IllegalArgumentException();
            }
        } catch (IllegalArgumentException e) {
            Logger.logWarning(this.getClass(), "Value for bypassEnabled in config.yml is not true/false, defaulting to true");
            mainConfig.set("enabled", "true");
            bypassEnabled = true;
        }
        try {
            bypassMode = BypassMode.valueOf(mainConfig.get("bypass-mode").toUpperCase());
        } catch (IllegalArgumentException e) {
            Logger.logWarning(this.getClass(), "Unknown value for bypass mode in config.yml, defaulting to no_count");
            mainConfig.set("bypass-mode", "no_count");
            bypassMode = BypassMode.NO_COUNT;
        }
        mainConfig.saveConfig();
        return true;
    }

    @Override
    public void onDisable() {
        Logger.logInfo(this.getClass(), ChatColor.GREEN + "Shutting down " + ChatColor.AQUA + "Admit" + ChatColor.GREEN + "...");
    }

    public void reload(CommandSender sender) {
        mainConfig.reload();
        if (!processConfig()) {
            Logger.logSevere(this.getClass(), ChatColor.RED + "Failed to reload " + ChatColor.AQUA + "Admit"
                    + ChatColor.RED + ", disabling...");
            sender.sendMessage(ChatColor.RED.toString() + ChatColor.BOLD + "Admit failed to reload, disabling...");
            Bukkit.getPluginManager().disablePlugin(this);
            return;
        }
        sender.sendMessage(ChatColor.GREEN + "Admit successfully reloaded!");
        Logger.logInfo(this.getClass(), ChatColor.GREEN + "Reloaded config");
    }

    @EventHandler
    public void onPlayerLogin(PlayerLoginEvent event) {
        if (!bypassEnabled) {
            return;
        }
        if (event.getResult() != PlayerLoginEvent.Result.KICK_FULL) {
            return;
        }
        if (event.getPlayer().hasPermission("admit.bypass")) {
            event.setResult(PlayerLoginEvent.Result.ALLOWED);
            return;
        }
        switch (bypassMode) {
            case NO_COUNT:
                long onlinePlayers = Bukkit.getOnlinePlayers().size();
                long numBypassers = Bukkit.getOnlinePlayers().stream().filter(Objects::nonNull)
                        .filter(p -> p.hasPermission("admit.bypass")).count();
                if (onlinePlayers - numBypassers < Bukkit.getMaxPlayers()) {
                    event.setResult(PlayerLoginEvent.Result.ALLOWED);
                }
                break;
            case OVERRIDE:
            default:
        }
    }

    @EventHandler
    public void onServerPing(ServerListPingEvent event) {
        if (!bypassEnabled) {
            return;
        }
        if (bypassMode == BypassMode.NO_COUNT) {
            Iterator<Player> iterator = event.iterator();
            while (iterator.hasNext()) {
                Player p = iterator.next();
                if (p.hasPermission("admit.bypass")) {
                    iterator.remove();
                }
            }
        }
    }



    //MockBukkit constructors
    public Main() {
        super();
    }

    protected Main(JavaPluginLoader loader, PluginDescriptionFile description, File dataFolder, File file) {
        super(loader, description, dataFolder, file);
    }
}
