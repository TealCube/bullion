/**
 * The MIT License Copyright (c) 2015 Teal Cube Games
 * <p>
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated
 * documentation files (the "Software"), to deal in the Software without restriction, including without limitation the
 * rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to
 * permit persons to whom the Software is furnished to do so, subject to the following conditions:
 * <p>
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the
 * Software.
 * <p>
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE
 * WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR
 * COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR
 * OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package info.faceland.mint.util;

import com.tealcube.minecraft.bukkit.shade.apache.commons.lang3.StringUtils;
import com.tealcube.minecraft.bukkit.shade.apache.commons.lang3.math.NumberUtils;
import com.tealcube.minecraft.bukkit.shade.google.common.base.CharMatcher;
import io.pixeloutlaw.minecraft.spigot.hilt.ItemStackExtensionsKt;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Item;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;
import org.nunnerycode.mint.MintPlugin;

public class MintUtil {

  public static Map<UUID, Double> protectedCashCache = new HashMap<>();
  public static String CASH_STRING = ChatColor.GOLD + "REWARD!";

  public static void setProtectedCash(Player player, double amount) {
    protectedCashCache.put(player.getUniqueId(), amount);
  }

  public static double getProtectedCash(Player player) {
    return protectedCashCache.getOrDefault(player.getUniqueId(), 0D);
  }

  public static Item spawnCashDrop(Location location, double amount, float velocity) {
    ItemStack item = new ItemStack(Material.GOLD_NUGGET);
    ItemStackExtensionsKt.setDisplayName(item, CASH_STRING);
    ItemStackExtensionsKt.setLore(item, Collections.singletonList(Double.toString(amount)));
    if (velocity > 0) {
      Item droppedItem = location.getWorld().dropItem(location, item);
      droppedItem.setVelocity(new Vector(
          Math.random() * velocity * (Math.random() > 0.5 ? 1 : -1),
          0.1 + Math.random() * velocity,
          Math.random() * velocity * (Math.random() > 0.5 ? 1 : -1))
      );
      return droppedItem;
    } else {
      return location.getWorld().dropItemNaturally(location, item);
    }
  }

  public static void applyDropProtection(Item drop, UUID owner, long duration) {
    drop.setOwner(owner);
    Bukkit.getScheduler().runTaskLater(MintPlugin.getInstance(), () -> clearDropProtection(drop), duration);
  }

  public static void clearDropProtection(Item drop) {
    if (drop != null) {
      drop.setOwner(null);
    }
  }

  public static boolean isCashDrop(ItemStack itemStack) {
    return itemStack.getType() == Material.GOLD_NUGGET && itemStack.getItemMeta() != null
        && CASH_STRING.equals(itemStack.getItemMeta().getDisplayName())
        && itemStack.getItemMeta().getLore() != null && !itemStack.getItemMeta().getLore()
        .isEmpty();
  }

  public static int getMobLevel(LivingEntity livingEntity) {
    int level;
    if (livingEntity instanceof Player) {
      level = ((Player) livingEntity).getLevel();
    } else if (livingEntity.hasMetadata("LVL")) {
      level = livingEntity.getMetadata("LVL").get(0).asInt();
    } else if (StringUtils.isBlank(livingEntity.getCustomName())) {
      level = 0;
    } else {
      String lev = CharMatcher.digit().or(CharMatcher.is('-')).negate()
          .collapseFrom(ChatColor.stripColor(livingEntity.getCustomName()), ' ').trim();
      level = NumberUtils.toInt(lev.split(" ")[0], 0);
    }
    return level;
  }
}
