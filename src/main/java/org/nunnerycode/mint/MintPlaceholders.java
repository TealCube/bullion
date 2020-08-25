package org.nunnerycode.mint;

import static org.nunnerycode.mint.MintPlugin.INT_FORMAT;

import com.tealcube.minecraft.bukkit.shade.apache.commons.lang3.StringUtils;
import info.faceland.mint.util.MintUtil;
import me.clip.placeholderapi.PlaceholderAPI;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

public class MintPlaceholders extends PlaceholderExpansion {

  @Override
  public boolean register() {
    if (!canRegister()) {
      return false;
    }
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
  public boolean persist(){
    return true;
  }

  @Override
  public String onPlaceholderRequest(Player p, String identifier) {
    if (p == null || StringUtils.isBlank(identifier)) {
      return "";
    }
    if (identifier.startsWith("max_protected_money")) {
      return INT_FORMAT.format(MintUtil.getProtectedCash(p));
    }
    if (identifier.startsWith("bank_balance")) {
      return INT_FORMAT.format(MintPlugin.getInstance().getManager().getBankBalance(p.getUniqueId()));
    }
    if (identifier.startsWith("full_econ_format")) {
      double balance = MintPlugin.getInstance().getEconomy().getBalance(p);
      double protect = MintUtil.getProtectedCash(p);
      if (balance <= protect) {
        return ChatColor.YELLOW + "" + ChatColor.BOLD + INT_FORMAT.format(balance) + " Bits";
      }
      return ChatColor.GREEN + "" + ChatColor.BOLD + INT_FORMAT.format(protect) + ChatColor.YELLOW
          + "" + ChatColor.BOLD + "+" + INT_FORMAT.format(balance - protect) + " Bits";
    }
    return null;
  }
}
