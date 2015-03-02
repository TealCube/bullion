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
package org.nunnerycode.mint.storage;

import com.tealcube.minecraft.bukkit.facecore.shade.config.SmartYamlConfiguration;
import com.tealcube.minecraft.bukkit.kern.apache.commons.lang3.Validate;
import com.tealcube.minecraft.bukkit.kern.shade.google.common.base.Optional;
import org.bukkit.configuration.ConfigurationSection;
import org.nunnerycode.mint.MintPlugin;
import org.nunnerycode.mint.accounts.BankAccount;
import org.nunnerycode.mint.accounts.PlayerAccount;

import java.io.File;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class YamlDataStorage implements DataStorage {
    private final SmartYamlConfiguration dataYAML;

    public YamlDataStorage(MintPlugin plugin) {
        this.dataYAML = new SmartYamlConfiguration(new File(plugin.getDataFolder(), "data.json"));
    }

    @Override
    public void initialize() {
        this.dataYAML.load();
    }

    @Override
    public void shutdown() {
        this.dataYAML.save();
    }

    @Override
    public Optional<PlayerAccount> loadPlayerAccount(UUID uuid) {
        Validate.notNull(uuid, "uuid cannot be null");
        Optional<PlayerAccount> accountOptional = Optional.absent();
        if (dataYAML.isSet("player." + uuid.toString())) {
            PlayerAccount account = new PlayerAccount(uuid, dataYAML.getDouble("player." + uuid.toString()));
            accountOptional = Optional.of(account);
        }
        return accountOptional;
    }

    @Override
    public Optional<BankAccount> loadBankAccount(UUID uuid) {
        Validate.notNull(uuid, "uuid cannot be null");
        Optional<BankAccount> accountOptional = Optional.absent();
        if (dataYAML.isSet("player." + uuid.toString())) {
            BankAccount account = new BankAccount(uuid, dataYAML.getDouble("bank." + uuid.toString()));
            accountOptional = Optional.of(account);
        }
        return accountOptional;
    }

    @Override
    public boolean savePlayerAccount(PlayerAccount account) {
        Validate.notNull(account, "account cannot be null");
        dataYAML.set("player." + account.getOwner().toString(), account.getBalance());
        dataYAML.save();
        return true;
    }

    @Override
    public boolean saveBankAccount(BankAccount account) {
        Validate.notNull(account, "account cannot be null");
        dataYAML.set("bank." + account.getOwner().toString(), account.getBalance());
        dataYAML.save();
        return true;
    }

    @Override
    public Set<PlayerAccount> loadPlayerAccounts() {
        Set<PlayerAccount> accounts = new HashSet<>();
        dataYAML.load();
        if (!dataYAML.isConfigurationSection("player")) {
            return accounts;
        }
        ConfigurationSection cs = dataYAML.getConfigurationSection("player");
        for (String key : cs.getKeys(false)) {
            UUID uniqueID = UUID.fromString(key);
            double balance = cs.getDouble(key);
            PlayerAccount account = new PlayerAccount(uniqueID, balance);
            accounts.add(account);
        }
        return accounts;
    }

    @Override
    public Set<BankAccount> loadBankAccounts() {
        Set<BankAccount> accounts = new HashSet<>();
        dataYAML.load();
        if (!dataYAML.isConfigurationSection("bank")) {
            return accounts;
        }
        ConfigurationSection cs = dataYAML.getConfigurationSection("bank");
        for (String key : cs.getKeys(false)) {
            UUID uniqueID = UUID.fromString(key);
            double balance = cs.getDouble(key);
            BankAccount account = new BankAccount(uniqueID, balance);
            accounts.add(account);
        }
        return accounts;
    }

    @Override
    public boolean savePlayerAccounts(Set<PlayerAccount> accounts) {
        Validate.notNull(accounts, "accounts cannot be null");
        for (PlayerAccount account : accounts) {
            dataYAML.set("player." + account.getOwner().toString(), account.getBalance());
        }
        dataYAML.save();
        return true;
    }

    @Override
    public boolean saveBankAccounts(Set<BankAccount> accounts) {
        Validate.notNull(accounts, "accounts cannot be null");
        for (BankAccount account : accounts) {
            dataYAML.set("bank." + account.getOwner().toString(), account.getBalance());
        }
        dataYAML.save();
        return true;
    }
}
