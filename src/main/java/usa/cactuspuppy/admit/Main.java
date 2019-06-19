package usa.cactuspuppy.admit;

import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.plugin.java.annotation.plugin.*;
import org.bukkit.plugin.java.annotation.plugin.author.Author;
import usa.cactuspuppy.admit.utils.Config;
import usa.cactuspuppy.admit.utils.Logger;

public class Main extends JavaPlugin implements Listener {
    @Getter
    private static Main instance;

    @Getter
    private Config mainConfig;

    @Override
    public void onEnable() {
        long start = System.nanoTime();
        instance = this;
        mainConfig = new Config();
        if (mainConfig.getInitCode() != 0) {
            abortSetup();
            return;
        }
        long elapsedNanos = System.nanoTime() - start;
        Logger.logInfo(this.getClass(), String.format(ChatColor.AQUA + "Admit" + ChatColor.GREEN + " startup complete!\n"
                + ChatColor.LIGHT_PURPLE + "Time Elapsed: " + ChatColor.GOLD + "%1$.2fms (%2$dns)",
                elapsedNanos / 10e6, elapsedNanos));
    }

    private void abortSetup() {
        Bukkit.getPluginManager().disablePlugin(this);
        Logger.logSevere(this.getClass(), "Critical error on plugin startup, disabling...");
    }

    @Override
    public void onDisable() {
        Logger.logInfo(this.getClass(), ChatColor.GREEN + "Shutting down " + ChatColor.AQUA + "Admit" + ChatColor.GREEN + "...");
    }

    @EventHandler
    public void onPlayerLogin(PlayerLoginEvent event) {
        if (event.getResult() != PlayerLoginEvent.Result.KICK_FULL) {
            return;
        }
    }
}
