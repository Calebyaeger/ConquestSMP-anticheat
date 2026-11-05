
package com.conquestsmp.anticheat;

import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.event.player.PlayerToggleFlightEvent;
import org.bukkit.potion.PotionEffectType;

public final class MovementListener implements Listener {

    private final ConquestAntiCheat plugin;
    private final CheckManager checks;

    public MovementListener(ConquestAntiCheat plugin, CheckManager checks) {
        this.plugin = plugin;
        this.checks = checks;
    }

    @EventHandler(ignoreCancelled = true)
    public void onMove(PlayerMoveEvent event) {
        Location from = event.getFrom();
        Location to = event.getTo();

        if (to == null || from.getWorld() != to.getWorld()) return;

        if (from.getX() == to.getX()
            && from.getY() == to.getY()
            && from.getZ() == to.getZ()) return;

        Player p = event.getPlayer();

        if (exempt(p) || p.isInsideVehicle() || p.isSwimming()
            || p.isGliding() || p.isRiptiding()) return;

        if (p.getLocation().getBlock().isLiquid()
            || to.getBlock().isLiquid()) return;

        if (p.hasPotionEffect(PotionEffectType.SPEED)
            || p.hasPotionEffect(PotionEffectType.JUMP_BOOST)) return;

        if (plugin.getConfig().getBoolean("checks.movement.enabled", true)
            && p.isOnGround()) {

            double dx = to.getX() - from.getX();
            double dz = to.getZ() - from.getZ();
            double horizontal = Math.sqrt(dx * dx + dz * dz);

            double threshold = plugin.getConfig().getDouble(
                "checks.movement.max-horizontal-distance", 0.90
            );

            if (horizontal > threshold) {
                int flags = checks.speedFlag(p.getUniqueId());
                int required = plugin.getConfig().getInt(
                    "checks.movement.flags-before-alert", 4
                );

                if (flags >= required) {
                    plugin.alert(p, "Movement",
                        String.format("unusual horizontal step %.2f (flags %d)",
                        horizontal, flags));
                }
            }
        }

        if (plugin.getConfig().getBoolean("checks.flight.enabled", true)
            && !p.isOnGround()) {

            double dy = to.getY() - from.getY();

            if (Math.abs(dy) < 0.012
                && !p.hasPotionEffect(PotionEffectType.LEVITATION)) {

                int flags = checks.flightFlag(p.getUniqueId());
                int required = plugin.getConfig().getInt(
                    "checks.flight.flags-before-alert", 3
                );

                if (flags >= required) {
                    plugin.alert(p, "Flight",
                        "unusual stable airborne movement (flags " + flags + ")");
                }
            }
        }
    }

    @EventHandler
    public void onTeleport(PlayerTeleportEvent e) {
        checks.reset(e.getPlayer().getUniqueId());
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent e) {
        checks.reset(e.getPlayer().getUniqueId());
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent e) {
        checks.reset(e.getPlayer().getUniqueId());
    }

    @EventHandler
    public void onToggleFlight(PlayerToggleFlightEvent e) {
        Player p = e.getPlayer();

        if (p.getGameMode() == GameMode.CREATIVE || p.getAllowFlight()) return;

        if (e.isFlying()) {
            plugin.alert(p, "Flight", "flight toggle event");
        }
    }

    private boolean exempt(Player p) {
        return p.getGameMode() == GameMode.CREATIVE
            || p.getGameMode() == GameMode.SPECTATOR
            || p.hasPermission("conquestac.bypass")
            || p.getAllowFlight();
    }
}
