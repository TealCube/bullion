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
package info.faceland.mint.util;

import io.pixeloutlaw.minecraft.spigot.hilt.ItemStackExtensionsKt;
import java.util.Arrays;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

public class MintUtil {

  public static String CASH_STRING = ChatColor.GOLD + "REWARD!";

  public static void spawnCashDrop(Location location, double amount) {
    ItemStack item = new ItemStack(Material.GOLD_NUGGET);
    ItemStackExtensionsKt.setDisplayName(item, CASH_STRING);
    ItemStackExtensionsKt.setLore(item, Arrays.asList(Double.toString(amount)));
    location.getWorld().dropItemNaturally(location, item);
  }

  public static boolean isCashDrop(ItemStack itemStack) {
    return itemStack.getType() == Material.GOLD_NUGGET && itemStack.getItemMeta() != null
        && CASH_STRING.equals(itemStack.getItemMeta().getDisplayName())
        && itemStack.getItemMeta().getLore() != null && !itemStack.getItemMeta().getLore()
        .isEmpty();
  }
}
