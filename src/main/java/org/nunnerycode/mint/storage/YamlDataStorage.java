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
package org.nunnerycode.mint.storage;

import com.tealcube.minecraft.bukkit.shade.apache.commons.lang3.Validate;
import com.tealcube.minecraft.bukkit.shade.google.common.base.Optional;
import io.pixeloutlaw.minecraft.spigot.config.SmartYamlConfiguration;
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
