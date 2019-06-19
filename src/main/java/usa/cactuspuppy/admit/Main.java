package usa.cactuspuppy.admit;

import lombok.Getter;
import lombok.Setter;
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
        mainConfig = new Config();
        if (mainConfig.getInitCode() != 0) {
            abortSetup();
            return;
        }
        if (!checkConfig()) {
            abortSetup();
            return;
        }
        Bukkit.getPluginManager().registerEvents(this, this);
        long elapsedNanos = System.nanoTime() - start;
        Logger.logInfo(this.getClass(), String.format(ChatColor.AQUA + "Admit" + ChatColor.GREEN + " startup complete!\n"
                + ChatColor.LIGHT_PURPLE + "Time Elapsed: " + ChatColor.GOLD + "%1$.2fms (%2$dns)",
                elapsedNanos / 10e6, elapsedNanos));
    }

    private void abortSetup() {
        Logger.logSevere(this.getClass(), "Critical error on plugin startup, disabling...");
        Bukkit.getPluginManager().disablePlugin(this);
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
                if (event.getPlayer().hasPermission("admit.bypass")) {
                    event.setResult(PlayerLoginEvent.Result.ALLOWED);
                }
                break;
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
