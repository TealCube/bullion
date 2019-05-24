/**
 * The MIT License Copyright (c) 2015 Teal Cube Games
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and
 * associated documentation files (the "Software"), to deal in the Software without restriction,
 * including without limitation the rights to use, copy, modify, merge, publish, distribute,
 * sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or
 * substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT
 * NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM,
 * DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package info.faceland.mint.listeners;

import com.tealcube.minecraft.bukkit.TextUtils;
import com.tealcube.minecraft.bukkit.bullion.GoldDropEvent;
import com.tealcube.minecraft.bukkit.facecore.utilities.MessageUtils;
import com.tealcube.minecraft.bukkit.shade.apache.commons.lang3.StringUtils;
import com.tealcube.minecraft.bukkit.shade.apache.commons.lang3.math.NumberUtils;
import com.tealcube.minecraft.bukkit.shade.google.common.base.CharMatcher;
import gyurix.spigotlib.ChatAPI;
import info.faceland.mint.MintEvent;
import info.faceland.mint.util.MintUtil;
import io.pixeloutlaw.minecraft.spigot.hilt.HiltItemStack;
import java.text.DecimalFormat;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.CraftItemEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.inventory.ItemStack;
import org.nunnerycode.mint.MintPlugin;

public class MintListener implements Listener {

  public static final DecimalFormat DF = new DecimalFormat("###,###,###");
  private static final String CARRIED_BITS = TextUtils.color("&2&lCarried Bits: &f&l{}");

  private final MintPlugin plugin;

  public MintListener(MintPlugin mintPlugin) {
    this.plugin = mintPlugin;
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
    plugin.getManager().updateWallet(player);
  }

  @EventHandler(priority = EventPriority.LOWEST)
  public void onItemPickupEvent(EntityPickupItemEvent event) {
    if (event.isCancelled() || !(event.getEntity() instanceof Player)) {
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
    if (!hiltItemStack.getName().equals(MintUtil.CASH_STRING)) {
      return;
    }
    if (StringUtils.isBlank(item.getCustomName())) {
      return;
    }

    Player player = (Player) event.getEntity();
    player.getWorld().playSound(player.getLocation(), Sound.ITEM_ARMOR_EQUIP_CHAIN, 1.0F, 1.3F);

    String stripped = ChatColor.stripColor(hiltItemStack.getLore().get(0));
    String replaced = CharMatcher.JAVA_LETTER.removeFrom(stripped).trim();
    int stacksize = hiltItemStack.getAmount();

    double amount = stacksize * NumberUtils.toDouble(replaced);
    plugin.getEconomy().depositPlayer(player, amount);

    event.getItem().remove();
    event.setCancelled(true);

    String message = CARRIED_BITS
        .replace("{}", plugin.getEconomy().format(plugin.getEconomy().getBalance(player)));
    ChatAPI.sendJsonMsg(ChatAPI.ChatMessageType.ACTION_BAR, TextUtils.color(message), player);
  }

  @EventHandler(priority = EventPriority.HIGHEST)
  public void onPlayerRespawnEvent(PlayerRespawnEvent event) {
    plugin.getManager().updateWallet(event.getPlayer());
  }

  @EventHandler(priority = EventPriority.HIGHEST)
  public void onPlayerJoinEvent(PlayerJoinEvent event) {
    plugin.getManager().updateWallet(event.getPlayer());
  }

  @EventHandler(priority = EventPriority.LOWEST)
  public void onCraftItem(CraftItemEvent event) {
    for (ItemStack is : event.getInventory().getMatrix()) {
      if (is == null || is.getType() != Material.PAPER) {
        continue;
      }
      HiltItemStack his = new HiltItemStack(is);
      if (his.getName()
          .equals(TextUtils.color(plugin.getSettings().getString("config.wallet.name")))) {
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
    if (his.getName()
        .equals(TextUtils.color(plugin.getSettings().getString("config.wallet.name", "")))) {
      event.setCancelled(true);
    }
  }

  @EventHandler
  public void onPawnClose(InventoryCloseEvent event) {
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
      double amount = plugin.getSettings()
          .getDouble("prices.materials." + hiltItemStack.getType().name(), 0D);
      if (!lore.isEmpty()) {
        amount += plugin.getSettings().getDouble("prices.options.lore.base-price", 3D);
        amount += plugin.getSettings().getDouble("prices.options.lore.per-line", 1D) * lore.size();
      }
      String strippedName = ChatColor.stripColor(hiltItemStack.getName());
      if (strippedName.startsWith("Socket Gem")) {
        value += plugin.getSettings().getDouble("prices.special.gems") * hiltItemStack.getAmount();
      } else if (plugin.getSettings().isSet("prices.names." + strippedName)) {
        value += plugin.getSettings().getDouble("prices.names." + strippedName, 0D) * hiltItemStack
            .getAmount();
      } else {
        value += amount * hiltItemStack.getAmount();
      }
    }
    for (HumanEntity entity : event.getViewers()) {
      if (!(entity instanceof Player)) {
        continue;
      }
      if (value > 0) {
        plugin.getEconomy().depositPlayer((Player) entity, value);
        MessageUtils.sendMessage(entity, plugin.getSettings().getString("language.pawn-success"),
            new String[][]{{"%amount%", "" + amountSold},
                {"%currency%", plugin.getEconomy().format(value)}});
      }
    }
  }
}
