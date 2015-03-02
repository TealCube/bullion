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

import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.economy.EconomyResponse;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.nunnerycode.mint.MintPlugin;

import java.text.DecimalFormat;
import java.util.List;
import java.util.UUID;

public class MintEconomy implements Economy {

    private static final DecimalFormat DF = new DecimalFormat("#.##");
    private MintPlugin plugin;

    public MintEconomy(MintPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }

    @Override
    public String getName() {
        return "Mint";
    }

    @Override
    public boolean hasBankSupport() {
        return true;
    }

    @Override
    public int fractionalDigits() {
        return 0;
    }

    @Override
    public String format(double v) {
        if (v == 1.00D) {
            return String.format("%s %s", DF.format(v), currencyNameSingular());
        }
        return String.format("%s %s", DF.format(v), currencyNamePlural());
    }

    @Override
    public String currencyNamePlural() {
        return plugin.getSettings().getString("config.currency-plural", "Bits");
    }

    @Override
    public String currencyNameSingular() {
        return plugin.getSettings().getString("config.currency-singular", "Bit");
    }

    @Override
    public boolean hasAccount(String s) {
        return plugin.getManager().hasPlayerAccount(UUID.fromString(s)) ||
                createPlayerAccount(s);
    }

    @Override
    public boolean hasAccount(OfflinePlayer player) {
        return hasAccount(player.getUniqueId().toString());
    }

    @Override
    public boolean hasAccount(String s, String s2) {
        return hasAccount(s);
    }

    @Override
    public boolean hasAccount(OfflinePlayer player, String worldName) {
        return hasAccount(player);
    }

    @Override
    public double getBalance(String s) {
        if (!hasAccount(s)) {
            return 0;
        }
        return plugin.getManager().getPlayerBalance(UUID.fromString(s));
    }

    @Override
    public double getBalance(OfflinePlayer player) {
        return getBalance(player.getUniqueId().toString());
    }

    @Override
    public double getBalance(String s, String s2) {
        return getBalance(s);
    }

    @Override
    public double getBalance(OfflinePlayer player, String world) {
        return getBalance(player);
    }

    @Override
    public boolean has(String s, double v) {
        return hasAccount(s) && getBalance(s) >= v;
    }

    @Override
    public boolean has(OfflinePlayer player, double amount) {
        return has(player.getUniqueId().toString(), amount);
    }

    @Override
    public boolean has(String s, String s2, double v) {
        return has(s, v);
    }

    @Override
    public boolean has(OfflinePlayer player, String worldName, double amount) {
        return has(player, amount);
    }

    @Override
    public EconomyResponse withdrawPlayer(String s, double v) {
        if (!hasAccount(s)) {
            createPlayerAccount(s);
        }
        UUID uuid = UUID.fromString(s);
        double balance = plugin.getManager().getPlayerBalance(uuid);
        if (!has(s, v)) {
            return new EconomyResponse(v, balance, EconomyResponse.ResponseType.FAILURE, null);
        }
        plugin.getManager().setPlayerBalance(uuid, balance - Math.abs(v));
        Bukkit.getPluginManager().callEvent(new MintEvent(s));
        return new EconomyResponse(v, balance - Math.abs(v), EconomyResponse.ResponseType.SUCCESS, null);
    }

    @Override
    public EconomyResponse withdrawPlayer(OfflinePlayer player, double amount) {
        return withdrawPlayer(player.getUniqueId().toString(), amount);
    }

    @Override
    public EconomyResponse withdrawPlayer(String s, String s2, double v) {
        return withdrawPlayer(s, v);
    }

    @Override
    public EconomyResponse withdrawPlayer(OfflinePlayer player, String worldName, double amount) {
        return withdrawPlayer(player, amount);
    }

    @Override
    public EconomyResponse depositPlayer(String s, double v) {
        if (!hasAccount(s)) {
            createPlayerAccount(s);
        }
        UUID uuid = UUID.fromString(s);
        double balance = plugin.getManager().getPlayerBalance(uuid);
        plugin.getManager().setPlayerBalance(uuid, balance + Math.abs(v));
        Bukkit.getPluginManager().callEvent(new MintEvent(s));
        return new EconomyResponse(v, balance + Math.abs(v), EconomyResponse.ResponseType.SUCCESS, null);
    }

    @Override
    public EconomyResponse depositPlayer(OfflinePlayer player, double amount) {
        return depositPlayer(player.getUniqueId().toString(), amount);
    }

    @Override
    public EconomyResponse depositPlayer(String s, String s2, double v) {
        return depositPlayer(s, v);
    }

    @Override
    public EconomyResponse depositPlayer(OfflinePlayer player, String worldName, double amount) {
        return depositPlayer(player, amount);
    }

    @Override
    public EconomyResponse createBank(String s, String s2) {
        UUID uuid = UUID.fromString(s);
        plugin.getManager().setBankBalance(uuid, 0D);
        return new EconomyResponse(0D, plugin.getManager().getBankBalance(uuid),
                EconomyResponse.ResponseType.SUCCESS, null);
    }

    @Override
    public EconomyResponse createBank(String name, OfflinePlayer player) {
        return createBank(name, player.getUniqueId().toString());
    }

    @Override
    public EconomyResponse deleteBank(String s) {
        plugin.getManager().removeBankAccount(UUID.fromString(s));
        return new EconomyResponse(0D, 0D, EconomyResponse.ResponseType.SUCCESS, null);
    }

    @Override
    public EconomyResponse bankBalance(String s) {
        UUID uuid = UUID.fromString(s);
        if (plugin.getManager().hasBankAccount(uuid)) {
            double balance = plugin.getManager().getBankBalance(uuid);
            return new EconomyResponse(balance, balance, EconomyResponse.ResponseType.SUCCESS, null);
        }
        return new EconomyResponse(0D, 0D, EconomyResponse.ResponseType.FAILURE, null);
    }

    @Override
    public EconomyResponse bankHas(String s, double v) {
        EconomyResponse response = bankBalance(s);
        if (response.transactionSuccess()) {
            if (response.balance >= v) {
                return new EconomyResponse(0D, response.balance, EconomyResponse.ResponseType.SUCCESS, null);
            }
            return new EconomyResponse(0D, response.balance, EconomyResponse.ResponseType.FAILURE, null);
        }
        return new EconomyResponse(0D, response.balance, EconomyResponse.ResponseType.FAILURE, null);
    }

    @Override
    public EconomyResponse bankWithdraw(String s, double v) {
        EconomyResponse response = bankBalance(s);
        UUID uuid = UUID.fromString(s);
        double balance = plugin.getManager().getBankBalance(uuid);
        if (response.transactionSuccess()) {
            plugin.getManager().setBankBalance(uuid, balance - Math.abs(v));
            return new EconomyResponse(v, balance - Math.abs(v), EconomyResponse.ResponseType.SUCCESS, null);
        }
        return new EconomyResponse(0D, 0D, EconomyResponse.ResponseType.FAILURE, null);
    }

    @Override
    public EconomyResponse bankDeposit(String s, double v) {
        EconomyResponse response = bankBalance(s);
        UUID uuid = UUID.fromString(s);
        double balance = plugin.getManager().getBankBalance(uuid);
        if (response.transactionSuccess()) {
            plugin.getManager().setBankBalance(uuid, balance + Math.abs(v));
            return new EconomyResponse(v, balance + Math.abs(v), EconomyResponse.ResponseType.SUCCESS, null);
        }
        return new EconomyResponse(0D, 0D, EconomyResponse.ResponseType.FAILURE, null);
    }

    @Override
    public EconomyResponse isBankOwner(String s, String s2) {
        if (s.equals(s2)) {
            return new EconomyResponse(0D, 0D, EconomyResponse.ResponseType.SUCCESS, null);
        }
        return new EconomyResponse(0D, 0D, EconomyResponse.ResponseType.FAILURE, null);
    }

    @Override
    public EconomyResponse isBankOwner(String name, OfflinePlayer player) {
        return isBankOwner(name, player.getUniqueId().toString());
    }

    @Override
    public EconomyResponse isBankMember(String s, String s2) {
        return isBankOwner(s, s2);
    }

    @Override
    public EconomyResponse isBankMember(String name, OfflinePlayer player) {
        return isBankMember(name, player.getUniqueId().toString());
    }

    @Override
    public List<String> getBanks() {
        return plugin.getManager().banksAsStrings();
    }

    @Override
    public boolean createPlayerAccount(String s) {
        UUID uuid = UUID.fromString(s);
        plugin.getManager().setPlayerBalance(uuid, 0D);
        Bukkit.getPluginManager().callEvent(new MintEvent(s));
        return true;
    }

    @Override
    public boolean createPlayerAccount(OfflinePlayer player) {
        return createPlayerAccount(player.getUniqueId().toString());
    }

    @Override
    public boolean createPlayerAccount(String s, String s2) {
        return createPlayerAccount(s);
    }

    @Override
    public boolean createPlayerAccount(OfflinePlayer player, String worldName) {
        return createPlayerAccount(player);
    }

    public EconomyResponse setBalance(OfflinePlayer player, int v) {
        return setBalance(player.getUniqueId().toString(), v);
    }

    public EconomyResponse setBalance(String s, double v) {
        if (!hasAccount(s)) {
            createPlayerAccount(s);
        }
        UUID uuid = UUID.fromString(s);
        double d = plugin.getManager().getPlayerBalance(uuid);
        plugin.getManager().setPlayerBalance(uuid, v);
        Bukkit.getPluginManager().callEvent(new MintEvent(s));
        return new EconomyResponse(d - v, v, EconomyResponse.ResponseType.SUCCESS, null);
    }

}
