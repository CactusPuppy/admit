package usa.cactuspuppy.admit;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class AdmitCommand implements CommandExecutor, TabCompleter {
    private static String[] subcommands = {"mode", "on", "off", "reload"};

    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String alias, @NotNull String[] args) {
        if (!commandSender.hasPermission("admit.admin")) {
            commandSender.sendMessage(ChatColor.RED + "I'm sorry, but you do not have permission to perform this command. Contact an administrator if you believe this is in error.");
            return true;
        }
        if (args.length == 0) {
            commandSender.sendMessage(ChatColor.AQUA + "[Admit] "
                    + ChatColor.GREEN + "Running Admit v" + Main.getInstance().getDescription().getVersion());
            return true;
        }
        String subcommand = args[0].toLowerCase();
        if (!Arrays.asList(subcommands).contains(subcommand)) {
            commandSender.sendMessage(ChatColor.RED + "Unknown subcommand " + subcommand);
            return false;
        }
        switch (subcommand) {
            case "mode":
                if (args.length < 2) {
                    commandSender.sendMessage(ChatColor.RED + "Please specify a bypass mode.");
                    return true;
                }
                try {
                    BypassMode mode = BypassMode.valueOf(args[1].toUpperCase());
                    Main.setBypassMode(mode);
                    commandSender.sendMessage(ChatColor.GREEN + "Set bypass mode to " + mode.name());
                } catch (IllegalArgumentException e) {
                    commandSender.sendMessage(ChatColor.RED + "Unknown bypass mode " + args[1]);
                    return true;
                }
                break;
            case "on":
                if (Main.getInstance().isBypassEnabled()) {
                    commandSender.sendMessage(ChatColor.YELLOW + "Admit is already enabled!");
                    return true;
                }
                Main.getInstance().setBypassEnabled(true);
                Main.getInstance().getMainConfig().set("enabled", "true");
                commandSender.sendMessage(ChatColor.GREEN + "Enabled Admit bypassing");
                break;
            case "off":
                if (!Main.getInstance().isBypassEnabled()) {
                    commandSender.sendMessage(ChatColor.YELLOW + "Admit is already disabled!");
                    return true;
                }
                Main.getInstance().setBypassEnabled(false);
                Main.getInstance().getMainConfig().set("enabled", "false");
                commandSender.sendMessage(ChatColor.GREEN + "Disabled Admit bypassing");
                break;
            case "reload":
                commandSender.sendMessage(ChatColor.GOLD + "Restarting Admit...");
                Main.getInstance().reload(commandSender);
                return true;
        }
        new Thread(() -> Main.getInstance().getMainConfig().saveConfig());
        return true;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String alias, @NotNull String[] args) {
        if (args.length == 0) {
            return new ArrayList<>();
        } else if (args.length == 1) {
            return Arrays.stream(subcommands).filter(s -> s.startsWith(args[0].toLowerCase())).map(String::toLowerCase).sorted().collect(Collectors.toList());
        } else if (args.length == 2) {
            if (args[0].equalsIgnoreCase("mode")) {
                return Arrays.stream(BypassMode.values()).map(Enum::toString).filter(s -> s.startsWith(args[1].toLowerCase())).map(String::toLowerCase).sorted().collect(Collectors.toList());
            }
        }
        return new ArrayList<>();
    }
}
