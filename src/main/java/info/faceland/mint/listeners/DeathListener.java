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

import static info.faceland.mint.listeners.MintListener.DF;

import com.tealcube.minecraft.bukkit.TextUtils;
import com.tealcube.minecraft.bukkit.bullion.GoldDropEvent;
import com.tealcube.minecraft.bukkit.shade.apache.commons.lang3.StringUtils;
import info.faceland.mint.util.MintUtil;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.EntityType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.potion.PotionEffectType;
import org.nunnerycode.mint.MintPlugin;

public class DeathListener implements Listener {

  private final MintPlugin plugin;
  private final List<String> noLossWorlds;

  public DeathListener(MintPlugin mintPlugin) {
    this.plugin = mintPlugin;
    this.noLossWorlds = plugin.getSettings().getStringList("config.no-loss-worlds");
  }

  @EventHandler(priority = EventPriority.HIGHEST)
  public void onEntityDeathEvent(final EntityDeathEvent event) {

    if (!validBitDropConditions(event)) {
      return;
    }
    EntityType entityType = event.getEntityType();
    double reward = plugin.getSettings().getDouble("rewards." + entityType.name(), 0D);

    if (reward == 0D) {
      return;
    }

    String world = event.getEntity().getWorld().getName();
    Location worldSpawn = event.getEntity().getWorld().getSpawnLocation();
    Location entityLoc = event.getEntity().getLocation();
    double distance = worldSpawn.distance(entityLoc);

    double multPer100Blocks = plugin.getSettings()
        .getDouble("config.money-drop-worlds." + world + ".multiplier-per-100-blocks", 0.0);
    double exponent = plugin.getSettings()
        .getDouble("config.money-drop-worlds." + world + ".exponential-bonus", 1D);

    double distMult = 1 + ((distance / 100) * multPer100Blocks);

    reward *= distMult;
    reward = Math.pow(reward, exponent);
    reward *= 0.75 + ThreadLocalRandom.current().nextDouble() / 2;

    GoldDropEvent gde = new GoldDropEvent(event.getEntity().getKiller(), event.getEntity(), reward);
    Bukkit.getPluginManager().callEvent(gde);

    reward = Math.max(1, gde.getAmount());

    double bombChance = plugin.getSettings().getDouble("config.bit-bomb-chance", 0.002);
    if (event.getEntity().getKiller().hasPotionEffect(PotionEffectType.LUCK)) {
      bombChance = plugin.getSettings().getDouble("config.lucky-bit-bomb-chance", 0.004);
    }

    if (ThreadLocalRandom.current().nextDouble() <= bombChance) {
      int numberOfDrops = ThreadLocalRandom.current().nextInt(12, 30);
      double bombTotal = 0;
      while (numberOfDrops > 0) {
        double newReward = reward * (4 + Math.random());
        bombTotal += newReward;
        MintUtil.spawnCashDrop(event.getEntity().getLocation(), newReward);
        numberOfDrops--;
      }
      String broadcastString = plugin.getSettings().getString("language.bit-bomb-message");
      broadcastString = broadcastString.replace("%player%", event.getEntity().getKiller().getName())
          .replace("%value%", DF.format(bombTotal));
      Bukkit.broadcastMessage(TextUtils.color(broadcastString));
    } else {
      MintUtil.spawnCashDrop(event.getEntity().getLocation(), reward);
    }
  }

  @EventHandler(priority = EventPriority.HIGH)
  public void onPlayerDeathEvent(final PlayerDeathEvent event) {
    if (noLossWorlds.contains(event.getEntity().getWorld().getName())) {
      return;
    }

    int maximumKeptBits = event.getEntity().hasPermission("mint.keep") ? 1000 : 100;
    double amount = plugin.getEconomy().getBalance(event.getEntity()) - maximumKeptBits;

    if (amount > 0) {
      plugin.getEconomy().setBalance(event.getEntity(), maximumKeptBits);
      MintUtil.spawnCashDrop(event.getEntity().getLocation(), amount);
    }
  }

  private boolean validBitDropConditions(EntityDeathEvent event) {
    if (event instanceof PlayerDeathEvent || event.getEntity().getKiller() == null) {
      return false;
    }
    if (StringUtils.isBlank(event.getEntity().getCustomName())) {
      return false;
    }
    if (event.getEntity().getCustomName().startsWith(ChatColor.WHITE + "Spawned")) {
      return false;
    }
    String dropWorld = event.getEntity().getWorld().getName();
    if (!plugin.getSettings()
        .getBoolean("config.money-drop-worlds." + dropWorld + ".enabled", false)) {
      return false;
    }
    if (ThreadLocalRandom.current().nextDouble() > plugin.getSettings()
        .getDouble("config.money-drop-worlds." + dropWorld + ".drop-chance", 1.0)) {
      return false;
    }
    return true;
  }
}
