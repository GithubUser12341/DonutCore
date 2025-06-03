package donutcore;

import net.milkbowl.vault.economy.Economy;
import org.bukkit.*;
import org.bukkit.attribute.Attribute;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.*;

public class DonutCore extends JavaPlugin implements Listener, TabExecutor {

    private static Economy econ;
    private final Map<UUID, Boolean> afkStatus = new HashMap<>();
    private Location spawnLocation;

    @Override
    public void onEnable() {
        if (!setupEconomy()) {
            getLogger().severe("Vault not found or no economy plugin! Disabling...");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }
        this.spawnLocation = getServer().getWorlds().get(0).getSpawnLocation();

        getServer().getPluginManager().registerEvents(this, this);
        getCommand("shop").setExecutor(this);
        getCommand("balance").setExecutor(this);
        getCommand("help").setExecutor(this);
        getCommand("spawn").setExecutor(this);
        getCommand("afk").setExecutor(this);
        getCommand("settings").setExecutor(this);
        getCommand("market").setExecutor(this);
        getCommand("ah").setExecutor(this);
    }

    private boolean setupEconomy() {
        if (getServer().getPluginManager().getPlugin("Vault") == null) return false;
        var rsp = getServer().getServicesManager().getRegistration(Economy.class);
        if (rsp == null) return false;
        econ = rsp.getProvider();
        return econ != null;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "Only players can use these commands.");
            return true;
        }
        Player player = (Player) sender;
        String cmd = command.getName().toLowerCase();

        switch (cmd) {
            case "shop": openShopGUI(player); break;
            case "balance": player.sendMessage(ChatColor.GREEN + "Balance: $" + econ.getBalance(player)); break;
            case "help": openHelpGUI(player); break;
            case "spawn": player.teleport(spawnLocation); player.sendMessage(ChatColor.AQUA + "Teleported to spawn!"); break;
            case "afk": toggleAfk(player); break;
            case "settings": openSettingsGUI(player); break;
            case "market": openMarketGUI(player); break;
            case "ah": openAuctionHouseGUI(player); break;
            default: return false;
        }
        return true;
    }

    private void toggleAfk(Player player) {
        boolean isAfk = afkStatus.getOrDefault(player.getUniqueId(), false);
        if (!isAfk) {
            afkStatus.put(player.getUniqueId(), true);
            player.setPlayerListName(ChatColor.GRAY + "[AFK] " + ChatColor.RESET + player.getName());
            player.sendMessage(ChatColor.YELLOW + "You are now AFK.");
            Bukkit.broadcastMessage(ChatColor.YELLOW + player.getName() + " is now AFK.");
        } else {
            afkStatus.remove(player.getUniqueId());
            player.setPlayerListName(player.getName());
            player.sendMessage(ChatColor.GREEN + "You are no longer AFK.");
            Bukkit.broadcastMessage(ChatColor.GREEN + player.getName() + " is no longer AFK.");
        }
    }

    private void openShopGUI(Player player) {
        Inventory shop = Bukkit.createInventory(null, 27, ChatColor.DARK_GREEN + "Donut SMP Shop");
        shop.setItem(11, createItem(Material.DIAMOND, ChatColor.AQUA + "Diamond - $100", List.of("Click to buy")));
        shop.setItem(12, createItem(Material.IRON_INGOT, ChatColor.GRAY + "Iron Ingot - $50", List.of("Click to buy")));
        shop.setItem(13, createItem(Material.GOLD_INGOT, ChatColor.GOLD + "Gold Ingot - $75", List.of("Click to buy")));
        shop.setItem(14, createItem(Material.EMERALD, ChatColor.GREEN + "Emerald - $80", List.of("Click to buy")));
        player.openInventory(shop);
    }

    private void openSettingsGUI(Player player) {
        Inventory settings = Bukkit.createInventory(null, 9, ChatColor.BLUE + "Settings");
        settings.setItem(3, createItem(Material.REDSTONE_TORCH, ChatColor.RED + "Toggle PvP", List.of("Click to toggle PvP")));
        settings.setItem(5, createItem(Material.NOTE_BLOCK, ChatColor.YELLOW + "Toggle Sounds", List.of("Click to toggle sounds")));
        player.openInventory(settings);
    }

    private void openHelpGUI(Player player) {
        Inventory help = Bukkit.createInventory(null, 27, ChatColor.BLUE + "DonutCore Help");
        help.setItem(11, createItem(Material.BOOK, ChatColor.GOLD + "/shop", List.of("Open the server shop")));
        help.setItem(12, createItem(Material.BOOK, ChatColor.GOLD + "/balance", List.of("Check your money")));
        help.setItem(13, createItem(Material.BOOK, ChatColor.GOLD + "/spawn", List.of("Teleport to spawn")));
        help.setItem(14, createItem(Material.BOOK, ChatColor.GOLD + "/afk", List.of("Toggle AFK status")));
        help.setItem(15, createItem(Material.BOOK, ChatColor.GOLD + "/settings", List.of("Open settings menu")));
        help.setItem(16, createItem(Material.BOOK, ChatColor.GOLD + "/market", List.of("Open market GUI")));
        help.setItem(17, createItem(Material.BOOK, ChatColor.GOLD + "/ah", List.of("Open auction house")));
        player.openInventory(help);
    }

    private void openMarketGUI(Player player) {
        Inventory market = Bukkit.createInventory(null, 27, ChatColor.DARK_PURPLE + "Market");
        market.setItem(11, createItem(Material.DIAMOND_SWORD, ChatColor.AQUA + "Sword - $250", List.of("Click to buy")));
        market.setItem(12, createItem(Material.BOW, ChatColor.GREEN + "Bow - $200", List.of("Click to buy")));
        market.setItem(13, createItem(Material.SHIELD, ChatColor.YELLOW + "Shield - $180", List.of("Click to buy")));
        market.setItem(14, createItem(Material.COOKED_BEEF, ChatColor.RED + "Food Pack - $60", List.of("Click to buy")));
        player.openInventory(market);
    }

    private void openAuctionHouseGUI(Player player) {
        Inventory ah = Bukkit.createInventory(null, 27, ChatColor.GOLD + "Auction House");
        ah.setItem(11, createItem(Material.DIAMOND, ChatColor.AQUA + "Auction Diamond", List.of("Item up for auction")));
        ah.setItem(12, createItem(Material.IRON_INGOT, ChatColor.GRAY + "Auction Iron", List.of("Item up for auction")));
        ah.setItem(13, createItem(Material.EMERALD, ChatColor.GREEN + "Auction Emerald", List.of("Item up for auction")));
        player.openInventory(ah);
    }

    private ItemStack createItem(Material mat, String name, List<String> lore) {
        ItemStack item = new ItemStack(mat);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(name);
            meta.setLore(lore);
            item.setItemMeta(meta);
        }
        return item;
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent e) {
        if (!(e.getWhoClicked() instanceof Player)) return;
        Player player = (Player) e.getWhoClicked();
        Inventory inv = e.getInventory();
        String title = inv.getTitle();
        ItemStack clicked = e.getCurrentItem();

        if (clicked == null || clicked.getType() == Material.AIR) return;

        if (title.equals(ChatColor.DARK_GREEN + "Donut SMP Shop")) {
            e.setCancelled(true);
            handleShopClick(player, clicked);
        } else if (title.equals(ChatColor.BLUE + "Settings")) {
            e.setCancelled(true);
            handleSettingsClick(player, clicked);
        } else if (title.equals(ChatColor.DARK_PURPLE + "Market")) {
            e.setCancelled(true);
            handleMarketClick(player, clicked);
        } else if (title.equals(ChatColor.GOLD + "Auction House")) {
            e.setCancelled(true);
            handleAuctionHouseClick(player, clicked);
        } else if (title.equals(ChatColor.BLUE + "DonutCore Help")) {
            e.setCancelled(true);
        }
    }

    private void handleShopClick(Player player, ItemStack item) {
        Material mat = item.getType();
        double price = switch (mat) {
            case DIAMOND -> 100;
            case IRON_INGOT -> 50;
            case GOLD_INGOT -> 75;
            case EMERALD -> 80;
            default -> 0;
        };
        if (price <= 0) return;
        if (econ.getBalance(player) >= price) {
            econ.withdrawPlayer(player, price);
            player.getInventory().addItem(new ItemStack(mat));
            player.sendMessage(ChatColor.GREEN + "Purchased " + mat.name() + " for $" + price);
        } else {
            player.sendMessage(ChatColor.RED + "Insufficient funds.");

