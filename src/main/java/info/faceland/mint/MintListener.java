/**
 * The MIT License
 * Copyright (c) 2015 Teal Cube Games
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package info.faceland.mint;

import com.tealcube.minecraft.bukkit.bullion.GoldDropEvent;
import com.tealcube.minecraft.bukkit.facecore.ui.ActionBarMessage;
import com.tealcube.minecraft.bukkit.facecore.utilities.MessageUtils;
import com.tealcube.minecraft.bukkit.facecore.utilities.TextUtils;
import com.tealcube.minecraft.bukkit.hilt.HiltItemStack;
import com.tealcube.minecraft.bukkit.kern.apache.commons.lang3.math.NumberUtils;
import com.tealcube.minecraft.bukkit.kern.shade.google.common.base.CharMatcher;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.ItemSpawnEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.CraftItemEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryPickupItemEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.nunnerycode.mint.MintPlugin;

import java.text.DecimalFormat;
import java.util.*;

public class MintListener implements Listener {

    private static final DecimalFormat DF = new DecimalFormat("#.##");
    private final MintPlugin plugin;
    private final Set<UUID> dead;

    public MintListener(MintPlugin mintPlugin) {
        this.plugin = mintPlugin;
        this.dead = new HashSet<>();
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onMintEvent(MintEvent mintEvent) {
        if (mintEvent.getUuid().equals("")) {
            return;
        }
        UUID uuid;
        try {
            uuid = UUID.fromString(mintEvent.getUuid());
        } catch (IllegalArgumentException e) {
            uuid = Bukkit.getPlayer(mintEvent.getUuid()).getUniqueId();
        }
        Player player = Bukkit.getPlayer(uuid);
        if (player == null) {
            return;
        }
        PlayerInventory pi = player.getInventory();
        HiltItemStack wallet = null;
        ItemStack[] contents = pi.getContents();
        for (ItemStack is : contents) {
            if (is == null || is.getType() == Material.AIR) {
                continue;
            }
            HiltItemStack his = new HiltItemStack(is);
            if (his.getName().equals(TextUtils.color(plugin.getSettings().getString("config.wallet.name", "")))) {
                wallet = his;
            }
        }
        if (wallet == null) {
            wallet = new HiltItemStack(Material.PAPER);
        }
        pi.removeItem(wallet);
        player.updateInventory();
        double b = plugin.getEconomy().getBalance(player);
        wallet.setName(TextUtils.color(plugin.getSettings().getString("config.wallet.name", "")));
        wallet.setLore(TextUtils.args(
                TextUtils.color(plugin.getSettings().getStringList("config.wallet.lore")),
                new String[][]{{"%amount%", DF.format(b)},
                               {"%currency%", b == 1.00D ? plugin.getEconomy().currencyNameSingular()
                                                         : plugin.getEconomy().currencyNamePlural()}}));
        if (pi.getItem(17) != null && pi.getItem(17).getType() != Material.AIR) {
            ItemStack old = new HiltItemStack(pi.getItem(17));
            pi.setItem(17, wallet);
            pi.addItem(old);
        } else {
            pi.setItem(17, wallet);
        }
        player.updateInventory();
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onEntityDeathEvent(final EntityDeathEvent event) {
        if (event instanceof PlayerDeathEvent || dead.contains(event.getEntity().getUniqueId())) {
            return;
        }
        dead.add(event.getEntity().getUniqueId());
        Bukkit.getScheduler().runTaskLater(plugin, new Runnable() {
            @Override
            public void run() {
                dead.remove(event.getEntity().getUniqueId());
            }
        }, 20L * 5);
        EntityType entityType = event.getEntityType();
        double reward = plugin.getSettings().getDouble("rewards." + entityType.name(), 0D);
        Location worldSpawn = event.getEntity().getWorld().getSpawnLocation();
        Location entityLoc = event.getEntity().getLocation();
        double distanceSquared = Math.pow(worldSpawn.getX() - entityLoc.getX(), 2) + Math.pow(worldSpawn.getZ() -
                                                                                                      entityLoc.getZ(),
                                                                                              2);
        reward += reward * (distanceSquared / Math.pow(10D, 2D))
                * plugin.getSettings().getDouble("config.per-ten-blocks-mult", 0.0);
        if (reward == 0D) {
            return;
        }
        GoldDropEvent gde = new GoldDropEvent(event.getEntity().getKiller(), event.getEntity(), reward);
        Bukkit.getPluginManager().callEvent(gde);
        HiltItemStack his = new HiltItemStack(Material.GOLD_NUGGET);
        his.setName(ChatColor.GOLD + "REWARD!");
        his.setLore(Arrays.asList(DF.format(gde.getAmount())));
        event.getDrops().add(his);
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onItemPickupEvent(PlayerPickupItemEvent event) {
        if (event.isCancelled()) {
            return;
        }
        if (event.getItem() == null || event.getItem().getItemStack() == null) {
            return;
        }
        Item item = event.getItem();
        HiltItemStack hiltItemStack = new HiltItemStack(item.getItemStack());
        if (hiltItemStack.getType() != Material.GOLD_NUGGET) {
            return;
        }
        if (!hiltItemStack.getName().equals(ChatColor.GOLD + "REWARD!")) {
            return;
        }
        String name = item.getCustomName();
        if (name == null) {
            return;
        }
        String stripped = ChatColor.stripColor(name);
        String replaced = CharMatcher.JAVA_LETTER.removeFrom(stripped).trim();
        double amount = NumberUtils.toDouble(replaced);
        plugin.getEconomy().depositPlayer(event.getPlayer(), amount);
        event.getItem().remove();
        event.setCancelled(true);
        new ActionBarMessage("<dark green>Wallet: <white>" + plugin.getEconomy().format(plugin.getEconomy()
                                                                                              .getBalance(
                                                                                                      event.getPlayer
                                                                                                              ()))
                                                                   .replace(" ", ChatColor.GREEN + " ")).send(
                event.getPlayer());
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerDeathEvent(final PlayerDeathEvent event) {
        if (dead.contains(event.getEntity().getUniqueId())) {
            return;
        }
        dead.add(event.getEntity().getUniqueId());
        Bukkit.getScheduler().runTaskLater(plugin, new Runnable() {
            @Override
            public void run() {
                dead.remove(event.getEntity().getUniqueId());
            }
        }, 20L * 5);
        double amount = plugin.getEconomy().getBalance(event.getEntity());
        HiltItemStack his = new HiltItemStack(Material.GOLD_NUGGET);
        his.setName(ChatColor.GOLD + "REWARD!");
        his.setLore(Arrays.asList(DF.format(amount) + ""));
        event.getEntity().getWorld().dropItemNaturally(event.getEntity().getLocation(), his);
        plugin.getEconomy().withdrawPlayer(event.getEntity().getUniqueId().toString(), amount);
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerDropItemEvent(PlayerDropItemEvent event) {
        Item item = event.getItemDrop();
        ItemStack is = item.getItemStack();
        HiltItemStack his = new HiltItemStack(is);
        if (!his.getName().equals(TextUtils.color(plugin.getSettings().getString("config.wallet.name")))) {
            return;
        }
        if (his.getLore().size() < 1) {
            return;
        }
        plugin.getEconomy().setBalance(event.getPlayer().getUniqueId().toString(), 0.00);
    }


    @EventHandler(priority = EventPriority.LOWEST)
    public void onItemSpawnEvent(ItemSpawnEvent event) {
        Material i = event.getEntity().getItemStack().getType();
        switch (i) {
            case PAPER:
                HiltItemStack walletStack = new HiltItemStack(event.getEntity().getItemStack());
                if (walletStack.getName()
                               .equals(TextUtils.color(plugin.getSettings().getString("config.wallet.name")))) {
                    if (walletStack.getLore().isEmpty()) {
                        return;
                    }
                    String s = walletStack.getLore().get(0);
                    String stripped = ChatColor.stripColor(s);
                    String replaced = CharMatcher.JAVA_LETTER.removeFrom(stripped);
                    double amount = NumberUtils.toDouble(replaced);
                    HiltItemStack nugget = new HiltItemStack(Material.GOLD_NUGGET);
                    nugget.setName(ChatColor.GOLD + "REWARD!");
                    event.getEntity().setItemStack(nugget);
                    event.getEntity().setCustomName(ChatColor.YELLOW + plugin.getEconomy().format(amount));
                    event.getEntity().setCustomNameVisible(true);
                }
                break;
            case GOLD_NUGGET:
                HiltItemStack nuggetStack = new HiltItemStack(event.getEntity().getItemStack());
                if (!nuggetStack.getName().equals(ChatColor.GOLD + "REWARD!") || nuggetStack.getLore().isEmpty()) {
                    return;
                }
                String s = nuggetStack.getLore().get(0);
                String stripped = ChatColor.stripColor(s);
                double amount = NumberUtils.toDouble(stripped);
                if (amount == 0.00D) {
                    event.setCancelled(true);
                    return;
                }
                HiltItemStack nugget = new HiltItemStack(Material.GOLD_NUGGET);
                nugget.setName(ChatColor.GOLD + "REWARD!");
                event.getEntity().setItemStack(nugget);
                event.getEntity().setCustomName(ChatColor.YELLOW + plugin.getEconomy().format(amount));
                event.getEntity().setCustomNameVisible(true);
                break;
            default:
                break;
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onCraftItem(CraftItemEvent event) {
        for (ItemStack is : event.getInventory().getMatrix()) {
            if (is == null || is.getType() != Material.PAPER) {
                continue;
            }
            HiltItemStack his = new HiltItemStack(is);
            if (his.getName().equals(TextUtils.color(plugin.getSettings().getString("config.wallet.name")))) {
                event.setCancelled(true);
                return;
            }
        }
    }

    @EventHandler
    public void onInventoryClickEvent(InventoryClickEvent event) {
        ItemStack is = event.getCurrentItem();
        if (is == null || is.getType() == Material.AIR) {
            return;
        }
        HiltItemStack his = new HiltItemStack(is);
        if (his.getLore().size() < 1) {
            return;
        }
        if (his.getName().equals(TextUtils.color(plugin.getSettings().getString("config.wallet.name", "")))) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        if (!event.getInventory().getName()
                  .equals(TextUtils.color(plugin.getSettings().getString("language.pawn-shop-name")))) {
            return;
        }
        double value = 0D;
        int amountSold = 0;
        for (ItemStack itemStack : event.getInventory().getContents()) {
            if (itemStack == null || itemStack.getType() == Material.AIR) {
                continue;
            }
            HiltItemStack hiltItemStack = new HiltItemStack(itemStack);
            if (hiltItemStack.getName()
                             .equals(TextUtils.color(plugin.getSettings().getString("config.wallet.name", "")))) {
                continue;
            }
            List<String> lore = hiltItemStack.getLore();
            double amount = plugin.getSettings().getDouble("prices.materials." + hiltItemStack.getType().name(), 0D);
            if (!lore.isEmpty()) {
                amount += plugin.getSettings().getDouble("prices.options.lore.base-price", 3D);
                amount += plugin.getSettings().getDouble("prices.options.lore" + ".per-line", 1D) * lore.size();
            }
            String strippedName = ChatColor.stripColor(hiltItemStack.getName());
            if (plugin.getSettings().isSet("prices.names." + strippedName)) {
                value += plugin.getSettings().getDouble("prices.names." + strippedName, 0D) * hiltItemStack.getAmount();
            } else {
                value += amount * hiltItemStack.getAmount();
            }
        }
        for (HumanEntity entity : event.getViewers()) {
            if (!(entity instanceof Player)) {
                continue;
            }
            plugin.getEconomy().depositPlayer((Player) entity, value);
            MessageUtils.sendMessage(entity, plugin.getSettings().getString("language.pawn-success"),
                                     new String[][]{{"%amount%", "" + amountSold},
                                                    {"%currency%", plugin.getEconomy().format(value)}});
        }
    }

    @EventHandler
    public void onInventoryPickupItem(InventoryPickupItemEvent event) {
        HiltItemStack his = new HiltItemStack(event.getItem().getItemStack());
        if (his.getName().equals(TextUtils.color(plugin.getSettings().getString("config.wallet.name", ""))) ||
                his.getName().equals(
                        ChatColor.GOLD + "REWARD!")) {
            event.setCancelled(true);
        }
    }

}
