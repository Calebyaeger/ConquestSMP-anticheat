
package com.conquestsmp.anticheat;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

public final class ConquestAntiCheat extends JavaPlugin {

    private CheckManager checks;

    @Override
    public void onEnable() {
        saveDefaultConfig();

        checks = new CheckManager(this);

        Bukkit.getPluginManager().registerEvents(
            new MovementListener(this, checks), this
        );
        Bukkit.getPluginManager().registerEvents(
            new NoFallListener(this), this
        );
        Bukkit.getPluginManager().registerEvents(
            new AdminGui(this), this
        );

        getLogger().info("ConquestSMP Anti-Cheat enabled.");
    }

    public CheckManager checks() {
        return checks;
    }

    @Override
    public boolean onCommand(
        CommandSender sender,
        Command command,
        String label,
        String[] args
    ) {
        if (!sender.hasPermission("conquestac.admin")) {
            sender.sendMessage(color("&cYou do not have permission."));
            return true;
        }

        String sub = args.length == 0 ? "gui" : args[0].toLowerCase();

        switch (sub) {
            case "gui" -> {
                if (sender instanceof Player player) {
                    new AdminGui(this).open(player);
                } else {
                    sender.sendMessage("Use this command in-game.");
                }
            }
            case "alerts" -> {
                boolean next = !getConfig().getBoolean("alerts.enabled", true);
                getConfig().set("alerts.enabled", next);
                saveConfig();
                sender.sendMessage(color("&aStaff alerts " + (next ? "enabled." : "disabled.")));
            }
            case "reload" -> {
                reloadConfig();
                sender.sendMessage(color("&aConquestAC config reloaded."));
            }
            case "status" -> {
                sender.sendMessage(color("&5ConquestAC &7v"
                    + getDescription().getVersion()
                    + " | alerts: "
                    + (getConfig().getBoolean("alerts.enabled", true) ? "&aON" : "&cOFF")
                    + " | punishments: "
                    + (getConfig().getBoolean("punishments.enabled", false) ? "&cON" : "&aOFF")));
            }
            default -> sender.sendMessage(
                color("&eUsage: /conquestac <gui|alerts|reload|status>")
            );
        }

        return true;
    }

    public void alert(Player player, String check, String detail) {
        if (!getConfig().getBoolean("alerts.enabled", true)) return;

        if (!checks.shouldAlert(
            player.getUniqueId(),
            check,
            getConfig().getLong("alerts.cooldown-seconds", 8)
        )) return;

        String prefix = getConfig().getString(
            "alerts.prefix", "&8[&5ConquestAC&8] &r"
        );

        String msg = color(prefix + "&d" + player.getName()
            + " &7flagged &f" + check + " &8- &7" + detail);

        for (Player staff : Bukkit.getOnlinePlayers()) {
            if (staff.hasPermission("conquestac.alerts")
                || staff.hasPermission("conquestac.admin")) {
                staff.sendMessage(msg);
            }
        }

        getLogger().info(player.getName() + " flagged " + check + ": " + detail);
    }

    public static String color(String s) {
        return ChatColor.translateAlternateColorCodes('&', s);
    }
}
