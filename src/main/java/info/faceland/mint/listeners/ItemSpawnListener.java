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

import com.tealcube.minecraft.bukkit.shade.apache.commons.lang3.math.NumberUtils;
import info.faceland.mint.util.MintUtil;
import io.pixeloutlaw.minecraft.spigot.hilt.ItemStackExtensionsKt;
import java.util.Arrays;
import java.util.concurrent.ThreadLocalRandom;
import org.bukkit.ChatColor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.ItemSpawnEvent;
import org.bukkit.inventory.ItemStack;
import org.nunnerycode.mint.MintPlugin;

public class ItemSpawnListener implements Listener {

  private final MintPlugin plugin;

  public ItemSpawnListener(MintPlugin mintPlugin) {
    this.plugin = mintPlugin;
  }

  @EventHandler(priority = EventPriority.LOWEST)
  public void onBitSpawn(ItemSpawnEvent event) {
    if (!MintUtil.isCashDrop(event.getEntity().getItemStack())) {
      return;
    }
    ItemStack nuggetStack = event.getEntity().getItemStack();
    String s = ItemStackExtensionsKt.getLore(nuggetStack).get(0);
    String stripped = ChatColor.stripColor(s);
    double amount = NumberUtils.toDouble(stripped);
    if (amount <= 0.00D) {
      event.setCancelled(true);
      return;
    }
    int antiStackSerial = ThreadLocalRandom.current().nextInt(0, 999999);

    ItemStackExtensionsKt.setDisplayName(nuggetStack, MintUtil.CASH_STRING);
    ItemStackExtensionsKt
        .setLore(nuggetStack, Arrays.asList(Double.toString(amount), "S:" + antiStackSerial));

    event.getEntity().setItemStack(nuggetStack);
    event.getEntity()
        .setCustomName(ChatColor.YELLOW + plugin.getEconomy().format(Math.floor(amount)));
    event.getEntity().setCustomNameVisible(true);
  }
}
