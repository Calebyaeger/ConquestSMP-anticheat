
package com.conquestsmp.anticheat;

import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;

public final class NoFallListener implements Listener {

    private final ConquestAntiCheat plugin;

    public NoFallListener(ConquestAntiCheat plugin) {
        this.plugin = plugin;
    }

    @EventHandler(ignoreCancelled = true)
    public void onDamage(EntityDamageEvent event) {
        if (!plugin.getConfig().getBoolean("checks.no-fall.enabled", true)) {
            return;
        }

        if (!(event.getEntity() instanceof Player p)) return;

        if (event.getCause() != EntityDamageEvent.DamageCause.FALL) {
            return;
        }

        if (p.getGameMode() == GameMode.CREATIVE
            || p.getGameMode() == GameMode.SPECTATOR
            || p.hasPermission("conquestac.bypass")) {
            return;
        }

        if (event.getDamage() <= 0.0) {
            plugin.alert(p, "NoFall", "fall damage event had zero damage");
        }
    }
}
