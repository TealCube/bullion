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
import com.tealcube.minecraft.bukkit.facecore.utilities.MessageUtils;
import com.tealcube.minecraft.bukkit.shade.apache.commons.lang3.StringUtils;
import com.tealcube.minecraft.bukkit.shade.apache.commons.lang3.math.NumberUtils;
import com.tealcube.minecraft.bukkit.shade.google.common.base.CharMatcher;
import info.faceland.mint.util.MintUtil;
import io.pixeloutlaw.minecraft.spigot.hilt.ItemStackExtensionsKt;
import java.util.List;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.ItemStack;
import org.nunnerycode.mint.MintPlugin;

public class MintListener implements Listener {

  private static final String CARRIED_BITS = TextUtils.color("&2&lCarried Bits: &f&l{}");

  private final MintPlugin plugin;

  public MintListener(MintPlugin mintPlugin) {
    this.plugin = mintPlugin;
  }

  @EventHandler(priority = EventPriority.LOWEST)
  public void onItemPickupEvent(EntityPickupItemEvent event) {
    if (event.isCancelled() || !(event.getEntity() instanceof Player)) {
      return;
    }
    ItemStack item = event.getItem().getItemStack();

    if (item.getType() != Material.GOLD_NUGGET) {
      return;
    }
    if (!MintUtil.CASH_STRING.equals(ItemStackExtensionsKt.getDisplayName(item))) {
      return;
    }

    Player player = (Player) event.getEntity();
    player.playSound(player.getLocation(), Sound.BLOCK_CHAIN_PLACE, 1.0F, 1.3F);

    String stripped = ChatColor.stripColor(ItemStackExtensionsKt.getLore(item).get(0));
    String replaced = CharMatcher.forPredicate(Character::isLetter).removeFrom(stripped).trim();
    int stacksize = item.getAmount();

    double amount = stacksize * NumberUtils.toDouble(replaced);
    plugin.getEconomy().depositPlayer(player, amount);

    event.getItem().remove();
    event.setCancelled(true);

    String message = CARRIED_BITS
        .replace("{}", plugin.getEconomy().format(plugin.getEconomy().getBalance(player)));
    MessageUtils.sendActionBar(player, message);
  }

  @EventHandler
  public void onPawnClose(InventoryCloseEvent event) {
    if (!plugin.getManager().isPlayerInPawnMap((Player) event.getPlayer())) {
      return;
    }
    double value = 0;
    int amountSold = 0;

    for (ItemStack itemStack : event.getInventory().getContents()) {
      if (itemStack == null || itemStack.getType() == Material.AIR) {
        continue;
      }
      int stackSize = itemStack.getAmount();
      List<String> lore = ItemStackExtensionsKt.getLore(itemStack);
      String name = ItemStackExtensionsKt.getDisplayName(itemStack);
      name = StringUtils.isBlank(name) ? "" : ChatColor.stripColor(name);

      if (name.startsWith("Socket Gem")) {
        value = plugin.getSettings().getDouble("prices.special.gems");
      } else if (plugin.getSettings().isSet("prices.names." + name)) {
        value = plugin.getSettings().getDouble("prices.names." + name, 0D);
      } else {
        value = plugin.getSettings()
            .getDouble("prices.materials." + itemStack.getType().name(), 0D);
        if (!lore.isEmpty()) {
          value += plugin.getSettings().getDouble("prices.options.lore.base-price", 3D);
          value += plugin.getSettings().getDouble("prices.options.lore.per-line", 1D) * lore.size();
        }
      }
      value *= stackSize;
    }

    if (value > 0) {
      plugin.getEconomy().depositPlayer((Player) event.getPlayer(), value);
      MessageUtils
          .sendMessage(event.getPlayer(), plugin.getSettings().getString("language.pawn-success"),
              new String[][]{{"%amount%", "" + amountSold},
                  {"%currency%", plugin.getEconomy().format(value)}});
    }
    plugin.getManager().removePlayerFromPawnMap((Player) event.getPlayer());
  }
}
