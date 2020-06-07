package org.nunnerycode.mint;

import static org.nunnerycode.mint.MintPlugin.INT_FORMAT;

import com.tealcube.minecraft.bukkit.bullion.PlayerDeathDropEvent;
import com.tealcube.minecraft.bukkit.shade.apache.commons.lang3.StringUtils;
import me.clip.placeholderapi.PlaceholderAPI;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class MintPlaceholders extends PlaceholderExpansion {

  private MintPlugin plugin;

  @Override
  public boolean register() {
    if (!canRegister()) {
      return false;
    }
    plugin = MintPlugin.getInstance();
    return PlaceholderAPI.registerPlaceholderHook(getIdentifier(), this);
  }

  @Override
  public String getAuthor() {
    return "Faceguy";
  }

  @Override
  public String getIdentifier() {
    return "mint";
  }

  @Override
  public String getVersion() {
    return "1.0.0";
  }

  @Override
  public String onPlaceholderRequest(Player p, String identifier) {
    if (p == null || StringUtils.isBlank(identifier)) {
      return "";
    }
    if (identifier.startsWith("protected_money")) {
      PlayerDeathDropEvent e = new PlayerDeathDropEvent(p, 50);
      Bukkit.getPluginManager().callEvent(e);
      return INT_FORMAT.format(e.getAmountProtected());
    }
    return null;
  }
}
