package info.faceland.mint.util;

import io.pixeloutlaw.minecraft.spigot.hilt.HiltItemStack;
import java.util.Arrays;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

public class MintUtil {

  public static String CASH_STRING = ChatColor.GOLD + "REWARD!";

  public static void spawnCashDrop(Location location, double amount) {
    HiltItemStack his = new HiltItemStack(Material.GOLD_NUGGET);
    his.setName(CASH_STRING);
    his.setLore(Arrays.asList(Double.toString(amount)));
    location.getWorld().dropItemNaturally(location, his);
  }

  public static boolean isCashDrop(ItemStack itemStack) {
    return itemStack.getType() == Material.GOLD_NUGGET && itemStack.getItemMeta() != null
        && CASH_STRING.equals(itemStack.getItemMeta().getDisplayName())
        && itemStack.getItemMeta().getLore() != null && !itemStack.getItemMeta().getLore()
        .isEmpty();
  }
}
