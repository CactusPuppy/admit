package usa.cactuspuppy.admit;

import lombok.Getter;
import lombok.Setter;
import org.apache.commons.io.FileUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.plugin.java.JavaPluginLoader;
import org.bukkit.plugin.java.annotation.plugin.*;
import org.bukkit.plugin.java.annotation.plugin.author.Author;
import usa.cactuspuppy.admit.utils.Config;
import usa.cactuspuppy.admit.utils.Logger;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Objects;

public class Main extends JavaPlugin implements Listener {
    @Getter
    private static Main instance;

    @Getter
    private Config mainConfig;

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
        if (!checkConfig()) {
            abortSetup();
            return;
        }
        try {
            Bukkit.getPluginManager().registerEvents(this, this);
        } catch (NullPointerException e) {
            Logger.logWarning(this.getClass(), "Unable to register login listener!");
        }
        long elapsedNanos = System.nanoTime() - start;
        Logger.logInfo(this.getClass(), String.format(ChatColor.AQUA + "Admit" + ChatColor.GREEN + " startup complete!\n"
                + ChatColor.LIGHT_PURPLE + "Time Elapsed: " + ChatColor.GOLD + "%1$.2fms (%2$dns)",
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
     * fixing any values which are not expected
     * @return Whether the plugin can resume startup
     */
    private boolean checkConfig() {
        //TODO
        return true;
    }

    @Override
    public void onDisable() {
        Logger.logInfo(this.getClass(), ChatColor.GREEN + "Shutting down " + ChatColor.AQUA + "Admit" + ChatColor.GREEN + "...");
    }

    public void reload() {
        mainConfig.reload();
        if (!checkConfig()) {
            Logger.logSevere(this.getClass(), ChatColor.RED + "Failed to reload " + ChatColor.AQUA + "Admit"
                    + ChatColor.RED + ", disabling...");
            Bukkit.getPluginManager().disablePlugin(this);
            return;
        }
        Logger.logInfo(this.getClass(), ChatColor.GREEN + "Reloaded config");
    }

    @EventHandler
    public void onPlayerLogin(PlayerLoginEvent event) {
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

    //MockBukkit constructors
    public Main() {
        super();
    }

    protected Main(JavaPluginLoader loader, PluginDescriptionFile description, File dataFolder, File file) {
        super(loader, description, dataFolder, file);
    }
}
