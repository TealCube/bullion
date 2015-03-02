/*
 * This file is part of Bullion, licensed under the ISC License.
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
import com.tealcube.minecraft.bukkit.facecore.utilities.MessageUtils;
import com.tealcube.minecraft.bukkit.facecore.utilities.TextUtils;
import com.tealcube.minecraft.bukkit.kern.methodcommand.Arg;
import com.tealcube.minecraft.bukkit.kern.methodcommand.Command;
import net.milkbowl.vault.economy.EconomyResponse;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.nunnerycode.mint.MintPlugin;

import java.text.DecimalFormat;
import java.util.Arrays;
import java.util.logging.Level;

public class MintCommand {

    private static final DecimalFormat DF = new DecimalFormat("#.##");
    private MintPlugin plugin;

    public MintCommand(MintPlugin plugin) {
        this.plugin = plugin;
    }

    @Command(identifier = "bank create", permissions = "mint.bank.create")
    public void bankCreateSubcommand(CommandSender commandSender, @Arg(name = "player") Player target) {
        EconomyResponse response = plugin.getEconomy().bankBalance(target.getUniqueId().toString());
        if (response.transactionSuccess()) {
            commandSender.sendMessage(
                    TextUtils.color(plugin.getSettings().getString("language.bank-create-failure", "")));
            return;
        }
        response = plugin.getEconomy().createBank(target.getUniqueId().toString(), target.getUniqueId().toString());
        if (response.transactionSuccess()) {
            commandSender.sendMessage(TextUtils.args(
                    TextUtils.color(plugin.getSettings().getString("language.bank-create-success", "")),
                    new String[][]{{"%player%", target.getDisplayName()}}));
            return;
        }
        commandSender.sendMessage(
                TextUtils.color(plugin.getSettings().getString("language.bank-create-failure", "")));
    }

    @Command(identifier = "bank balance", permissions = "mint.bank.balance")
    public void bankBalance(Player player) {
        EconomyResponse response = plugin.getEconomy().bankBalance(player.getUniqueId().toString());
        if (!response.transactionSuccess()) {
            player.sendMessage(
                    TextUtils.color(plugin.getSettings().getString("language.bank-balance-failure", "")));
            return;
        }
        player.sendMessage(TextUtils.args(
                TextUtils.color(plugin.getSettings().getString("language.bank-balance", "")),
                new String[][]{
                        {"%currency%",
                                plugin.getEconomy().format(response.balance)}}));
    }

    @Command(identifier = "bank deposit", permissions = "mint.bank.deposit")
    public void bankDeposit(Player player, @Arg(name = "amount") double amount) {
        EconomyResponse response = plugin.getEconomy().bankBalance(player.getUniqueId().toString());
        if (!response.transactionSuccess()) {
            plugin.getDebugPrinter().log(Level.INFO, "no bank exists for " + player.getUniqueId().toString());
            player.sendMessage(
                    TextUtils.color(plugin.getSettings().getString("language.bank-deposit-failure", "")));
            return;
        }
        if (!plugin.getEconomy().has(player.getUniqueId().toString(), amount)) {
            plugin.getDebugPrinter().log(Level.INFO, player.getUniqueId().toString() + " does not have enough money");
            player.sendMessage(
                    TextUtils.color(plugin.getSettings().getString("language.bank-deposit-failure", "")));
            return;
        }
        if (plugin.getEconomy().bankDeposit(player.getUniqueId().toString(), amount).transactionSuccess()) {
            if (plugin.getEconomy().withdrawPlayer(player.getUniqueId().toString(), amount).transactionSuccess()) {
                player.sendMessage(TextUtils.args(
                        TextUtils.color(plugin.getSettings().getString("language.bank-deposit-success", "")),
                        new String[][]{{"%currency%", plugin.getEconomy().format(amount)}}));
                player.sendMessage(TextUtils.args(
                        TextUtils.color(plugin.getSettings().getString("language.bank-balance", "")),
                        new String[][]{
                                {"%currency%",
                                        plugin.getEconomy().format(plugin.getEconomy()
                                                .bankBalance(player.getUniqueId().toString()).balance)}}));
                return;
            } else {
                plugin.getDebugPrinter()
                        .log(Level.INFO, "could not withdraw money from " + player.getUniqueId().toString());
            }
        } else {
            plugin.getDebugPrinter().log(Level.INFO, "could not deposit money in " + player.getUniqueId().toString());
        }
        player.sendMessage(
                TextUtils.color(plugin.getSettings().getString("language.bank-deposit-failure", "")));
    }

    @Command(identifier = "bank withdraw", permissions = "mint.bank.withdraw")
    public void bankWithdraw(Player player, @Arg(name = "amount") double amount) {
        EconomyResponse response = plugin.getEconomy().bankBalance(player.getUniqueId().toString());
        if (!response.transactionSuccess()) {
            player.sendMessage(
                    TextUtils.color(plugin.getSettings().getString("language.bank-withdraw-failure", "")));
            return;
        }
        if (!plugin.getEconomy().bankHas(player.getUniqueId().toString(), amount).transactionSuccess()) {
            player.sendMessage(
                    TextUtils.color(plugin.getSettings().getString("language.bank-withdraw-failure", "")));
            return;
        }
        if (plugin.getEconomy().bankWithdraw(player.getUniqueId().toString(), amount).transactionSuccess() && plugin
                .getEconomy().depositPlayer(player.getUniqueId().toString(), amount).transactionSuccess()) {
            player.sendMessage(TextUtils.args(
                    TextUtils.color(plugin.getSettings().getString("language.bank-withdraw-success", "")),
                    new String[][]{{"%currency%", plugin.getEconomy().format(amount)}}));
            player.sendMessage(TextUtils.args(
                    TextUtils.color(plugin.getSettings().getString("language.bank-balance", "")),
                    new String[][]{
                            {"%currency%", plugin.getEconomy().format(plugin.getEconomy()
                                    .bankBalance(player.getUniqueId().toString()).balance)}}));
            return;
        }
        player.sendMessage(
                TextUtils.color(plugin.getSettings().getString("language.bank-withdraw-failure", "")));
    }

    @Command(identifier = "pay", permissions = "mint.pay")
    public void payCommand(Player player, @Arg(name = "target") Player target, @Arg(name = "amount") double amount) {
        if (player.getLocation().distanceSquared(target.getLocation()) > plugin.getSettings().getDouble(
                "config.pay-distance-max", 25)) {
            player.sendMessage(TextUtils.color(plugin.getSettings().getString("language.pay-range", "")));
            return;
        }
        if (!plugin.getEconomy().has(player.getUniqueId().toString(), amount)) {
            player.sendMessage(TextUtils.color(plugin.getSettings().getString("language.pay-failure", "")));
            return;
        }
        if (plugin.getEconomy().withdrawPlayer(player.getUniqueId().toString(), amount).transactionSuccess() &&
                plugin.getEconomy().depositPlayer(target.getUniqueId().toString(), amount).transactionSuccess()) {
            player.sendMessage(TextUtils.args(
                    TextUtils.color(plugin.getSettings().getString("language.pay-success", "")),
                    new String[][]{{"%player%", target.getDisplayName()},
                            {"%currency%", plugin.getEconomy().format(Math.abs(amount))
                            }}));
            target.sendMessage(TextUtils.args(
                    TextUtils.color(plugin.getSettings().getString("language.gain-money", "")),
                    new String[][]{{"%amount%", String.valueOf(amount)},
                            {"%money%", amount == 1D ? plugin.getEconomy()
                                    .currencyNameSingular()
                                    : plugin.getEconomy()
                                    .currencyNamePlural()},
                            {"%currency%", plugin.getEconomy().format(amount)}}));
            return;
        }
        player.sendMessage(TextUtils.color(plugin.getSettings().getString("language.pay-failure", "")));
    }

    @Command(identifier = "epay", permissions = "mint.epay")
    public void ePayCommand(Player player, @Arg(name = "target") Player target, @Arg(name = "amount") double amount) {
        if (!plugin.getEconomy().has(player.getUniqueId().toString(), amount)) {
            player.sendMessage(TextUtils.color(plugin.getSettings().getString("language.pay-failure", "")));
            return;
        }
        if (plugin.getEconomy().withdrawPlayer(player.getUniqueId().toString(), amount).transactionSuccess() &&
                plugin.getEconomy().depositPlayer(target.getUniqueId().toString(), amount).transactionSuccess()) {
            player.sendMessage(TextUtils.args(
                    TextUtils.color(plugin.getSettings().getString("language.pay-success", "")),
                    new String[][]{{"%player%", target.getDisplayName()},
                            {"%currency%", plugin.getEconomy().format(Math.abs(amount))
                            }}));
            return;
        }
        player.sendMessage(TextUtils.color(plugin.getSettings().getString("language.pay-failure", "")));
    }

    @Command(identifier = "mint add", permissions = "mint.add", onlyPlayers = false)
    public void addSubcommand(CommandSender sender, @Arg(name = "target") Player target,
                              @Arg(name = "amount") double amount) {
        if (plugin.getEconomy().depositPlayer(target.getUniqueId().toString(), amount).transactionSuccess()) {
            sender.sendMessage(TextUtils.args(
                    TextUtils.color(plugin.getSettings().getString("language.add-money", "")),
                    new String[][]{{"%player%", target.getDisplayName()}}));
        }
    }

    @Command(identifier = "mint sub", permissions = "mint.sub", onlyPlayers = false)
    public void subSubcommand(CommandSender sender, @Arg(name = "target") Player target,
                              @Arg(name = "amount") double amount) {
        if (plugin.getEconomy().withdrawPlayer(target.getUniqueId().toString(), amount).transactionSuccess()) {
            sender.sendMessage(TextUtils.args(
                    TextUtils.color(plugin.getSettings().getString("language.sub-money", "")),
                    new String[][]{{"%player%", target.getDisplayName()}}));
        }
    }

    @Command(identifier = "mint spawn", permissions = "mint.spawn", onlyPlayers = false)
    public void spawnSubcommand(CommandSender sender, @Arg(name = "amount") int amount,
                                @Arg(name = "world") String worldName, @Arg(name = "x") int x,
                                @Arg(name = "y") int y, @Arg(name = "z") int z) {
        if (Bukkit.getWorld(worldName) == null) {
            sender.sendMessage(TextUtils.color(plugin.getSettings().getString("language.spawn-failure", "")));
            return;
        }
        HiltItemStack nugget = new HiltItemStack(Material.GOLD_NUGGET);
        nugget.setName(ChatColor.GOLD + "REWARD!");
        nugget.setLore(Arrays.asList(Math.round(Math.abs(amount)) + ""));
        World w = Bukkit.getWorld(worldName);
        w.dropItemNaturally(new Location(w, x, y, z), nugget);
        sender.sendMessage(TextUtils.args(
                TextUtils.color(plugin.getSettings().getString("language.spawn-success", "")),
                new String[][]{{"%currency%", plugin.getEconomy().format(Math.abs(amount))
                }}));
    }

    @Command(identifier = "mint balance", permissions = "mint.balance", onlyPlayers = false)
    public void balanceSubcommand(CommandSender sender, @Arg(name = "target") Player target) {
        MessageUtils.sendMessage(sender, "<white>%player%<green> has <white>%currency%<green>.",
                new String[][]{{"%player%", target.getDisplayName()},
                        {"%currency%", plugin.getEconomy().format(plugin.getEconomy().getBalance(target))}});
    }

    @Command(identifier = "pawn", permissions = "mint.pawn", onlyPlayers = true)
    public void pawnCommand(Player sender) {
        Inventory inventory = Bukkit.createInventory(null, InventoryType.CHEST,
                TextUtils.color(plugin.getSettings().getString("language.pawn-shop-name")));
        sender.openInventory(inventory);
    }

}
