
package com.conquestsmp.anticheat;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import java.util.List;

public final class AdminGui implements Listener {
    private static final String TITLE =
        ConquestAntiCheat.color("&5Conquest SMP Anti-Cheat");
    private final ConquestAntiCheat plugin;

    public AdminGui(ConquestAntiCheat plugin) {
        this.plugin = plugin;
    }

    public void open(Player player) {
        Inventory inv = Bukkit.createInventory(null, 27, TITLE);
        inv.setItem(10, toggle(Material.FEATHER, "&bMovement Check",
            "checks.movement.enabled"));
        inv.setItem(12, toggle(Material.ELYTRA, "&dFlight Check",
            "checks.flight.enabled"));
        inv.setItem(14, toggle(Material.IRON_BOOTS, "&eNo-Fall Check",
            "checks.no-fall.enabled"));
        inv.setItem(16, toggle(Material.DIAMOND_ORE,
            "&aX-ray (Paper config recommended)", "checks.xray.enabled"));
        inv.setItem(22, info(Material.PAPER, "&fPunishments",
            "&7Currently: " +
            (plugin.getConfig().getBoolean("punishments.enabled", false)
                ? "&cEnabled" : "&aDisabled"),
            "&7Edit config.yml; keep disabled while tuning."));
        inv.setItem(26, info(Material.BARRIER, "&cClose",
            "&7Click to close"));
        player.openInventory(inv);
    }

    private ItemStack toggle(Material material, String name, String path) {
        boolean enabled = plugin.getConfig().getBoolean(path, false);
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(ConquestAntiCheat.color(name));
        meta.setLore(List.of(
            ConquestAntiCheat.color("&7Status: " +
                (enabled ? "&aEnabled" : "&cDisabled")),
            ConquestAntiCheat.color("&eClick to toggle")));
        meta.setLocalizedName(path);
        item.setItemMeta(meta);
        return item;
    }

    private ItemStack info(Material material, String name, String... lore) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(ConquestAntiCheat.color(name));
        meta.setLore(java.util.Arrays.stream(lore)
            .map(ConquestAntiCheat::color).toList());
        item.setItemMeta(meta);
        return item;
    }

    @EventHandler
    public void onClick(InventoryClickEvent event) {
        if (!TITLE.equals(event.getView().getTitle())) return;
        event.setCancelled(true);
        if (!(event.getWhoClicked() instanceof Player player)
                || event.getCurrentItem() == null) return;
        if (event.getRawSlot() == 26) {
            player.closeInventory();
            return;
        }
        ItemMeta meta = event.getCurrentItem().getItemMeta();
        if (meta == null) return;
        String path = meta.getLocalizedName();
        if (path == null || !path.startsWith("checks.")) return;
        plugin.getConfig().set(path,
            !plugin.getConfig().getBoolean(path, false));
        plugin.saveConfig();
        open(player);
    }
}
