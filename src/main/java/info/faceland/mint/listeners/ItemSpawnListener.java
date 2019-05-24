package info.faceland.mint.listeners;

import com.tealcube.minecraft.bukkit.TextUtils;
import com.tealcube.minecraft.bukkit.shade.apache.commons.lang3.math.NumberUtils;
import info.faceland.mint.util.MintUtil;
import io.pixeloutlaw.minecraft.spigot.hilt.HiltItemStack;
import java.util.Arrays;
import java.util.concurrent.ThreadLocalRandom;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.ItemSpawnEvent;
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
    HiltItemStack nuggetStack = new HiltItemStack(event.getEntity().getItemStack());
    String s = nuggetStack.getLore().get(0);
    String stripped = ChatColor.stripColor(s);
    double amount = NumberUtils.toDouble(stripped);
    if (amount <= 0.00D) {
      event.setCancelled(true);
      return;
    }
    int antiStackSerial = ThreadLocalRandom.current().nextInt(0, 999999);

    HiltItemStack nugget = new HiltItemStack(Material.GOLD_NUGGET);
    nugget.setName(MintUtil.CASH_STRING);
    nugget.setLore(Arrays.asList(Double.toString(amount), "S:" + antiStackSerial));

    event.getEntity().setItemStack(nugget);
    event.getEntity()
        .setCustomName(ChatColor.YELLOW + plugin.getEconomy().format(Math.floor(amount)));
    event.getEntity().setCustomNameVisible(true);
  }
}
