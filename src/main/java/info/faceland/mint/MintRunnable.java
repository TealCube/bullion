/*
 * This file is part of Mint, licensed under the ISC License.
 *
 * Copyright (c) 2014 Richard Harrah
 *
 * Permission to use, copy, modify, and/or distribute this software for any purpose with or without fee is hereby granted,
 * provided that the above copyright notice and this permission notice appear in all copies.
 *
 * THE SOFTWARE IS PROVIDED "AS IS" AND THE AUTHOR DISCLAIMS ALL WARRANTIES WITH REGARD TO THIS SOFTWARE INCLUDING ALL
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS. IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR ANY SPECIAL, DIRECT,
 * INDIRECT, OR CONSEQUENTIAL DAMAGES OR ANY DAMAGES WHATSOEVER RESULTING FROM LOSS OF USE, DATA OR PROFITS, WHETHER IN AN
 * ACTION OF CONTRACT, NEGLIGENCE OR OTHER TORTIOUS ACTION, ARISING OUT OF OR IN CONNECTION WITH THE USE OR PERFORMANCE OF
 * THIS SOFTWARE.
 */
package info.faceland.mint;

import com.tealcube.minecraft.bukkit.facecore.shade.hilt.HiltItemStack;
import com.tealcube.minecraft.bukkit.facecore.utilities.TextUtils;
import com.tealcube.minecraft.bukkit.kern.apache.commons.lang3.math.NumberUtils;
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
