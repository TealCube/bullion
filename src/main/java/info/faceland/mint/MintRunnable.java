/**
 * The MIT License
 * Copyright (c) 2015 Teal Cube Games
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package info.faceland.mint;

import com.tealcube.minecraft.bukkit.TextUtils;
import com.tealcube.minecraft.bukkit.hilt.HiltItemStack;
import com.tealcube.minecraft.bukkit.shade.apache.commons.lang3.math.NumberUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.scheduler.BukkitRunnable;
import org.nunnerycode.mint.MintPlugin;

import java.util.List;

public class MintRunnable extends BukkitRunnable {

    private MintPlugin plugin;

    public MintRunnable(MintPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public void run() {
        for (Player p : Bukkit.getOnlinePlayers()) {
            PlayerInventory pi = p.getInventory();
            HiltItemStack wallet = null;
            for (ItemStack is : pi.getContents()) {
                if (is == null || is.getType() == Material.AIR) {
                    continue;
                }
                HiltItemStack his = new HiltItemStack(is);
                if (his.getName().equals(TextUtils.color(plugin.getSettings().getString("config.wallet.name")))) {
                    wallet = his;
                }
            }
            if (wallet == null) {
                continue;
            }
            List<String> lore = wallet.getLore();
            if (lore.isEmpty()) {
                continue;
            }
            String s = lore.get(0);
            String first = ChatColor.stripColor(s).trim();
            String amountString = first.replaceAll("[a-zA-Z]", "");
            double d = NumberUtils.toDouble(amountString);
            double accountBalance = plugin.getEconomy().getBalance(p.getUniqueId().toString());
            double difference = accountBalance - d;
            if (difference < 0) {
                plugin.getEconomy().depositPlayer(p.getUniqueId().toString(), difference);
            } else if (difference > 0) {
                plugin.getEconomy().withdrawPlayer(p.getUniqueId().toString(), difference);
            }
        }
    }

}
