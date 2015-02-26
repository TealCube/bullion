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
package org.nunnerycode.mint;

import com.tealcube.minecraft.bukkit.facecore.FacecorePlugin;
import com.tealcube.minecraft.bukkit.facecore.logging.PluginLogger;
import com.tealcube.minecraft.bukkit.facecore.plugin.FacePlugin;
import com.tealcube.minecraft.bukkit.facecore.shade.config.MasterConfiguration;
import com.tealcube.minecraft.bukkit.facecore.shade.config.VersionedSmartConfiguration;
import com.tealcube.minecraft.bukkit.facecore.shade.config.VersionedSmartYamlConfiguration;
import com.tealcube.minecraft.bukkit.kern.methodcommand.CommandHandler;
import info.faceland.mint.MintCommand;
import info.faceland.mint.MintEconomy;
import info.faceland.mint.MintListener;
import info.faceland.mint.MintRunnable;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.plugin.ServicePriority;
import org.nunnerycode.mint.accounts.BankAccount;
import org.nunnerycode.mint.accounts.PlayerAccount;
import org.nunnerycode.mint.storage.DataStorage;
import org.nunnerycode.mint.storage.MySqlDataStorage;
import org.nunnerycode.mint.storage.YamlDataStorage;

import java.io.File;

public class MintPlugin extends FacePlugin {
    private MasterConfiguration settings;
    private MintEconomy economy;
    private MintManager manager;
    private PluginLogger debugPrinter;
    private DataStorage dataStorage;

    public MintEconomy getEconomy() {
        return economy;
    }

    public FacecorePlugin facecorePlugin;

    @Override
    public void enable() {
        facecorePlugin = (FacecorePlugin) getServer().getPluginManager().getPlugin("facecore");
        debugPrinter = new PluginLogger(this);

        VersionedSmartYamlConfiguration configYAML =
                new VersionedSmartYamlConfiguration(new File(getDataFolder(), "config.yml"),
                        getResource("config.yml"), VersionedSmartConfiguration.VersionUpdateType.BACKUP_AND_UPDATE);
        if (configYAML.update()) {
            getLogger().info("Updating config.yml");
        }

        VersionedSmartYamlConfiguration rewardsYAML =
                new VersionedSmartYamlConfiguration(new File(getDataFolder(), "rewards.yml"),
                        getResource("rewards.yml"), VersionedSmartConfiguration.VersionUpdateType.BACKUP_AND_UPDATE);
        if (rewardsYAML.update()) {
            getLogger().info("Updating rewards.yml");
        }

        VersionedSmartYamlConfiguration languageYAML =
                new VersionedSmartYamlConfiguration(new File(getDataFolder(), "language.yml"),
                        getResource("language.yml"), VersionedSmartConfiguration.VersionUpdateType.BACKUP_AND_UPDATE);
        if (languageYAML.update()) {
            getLogger().info("Updating language.yml");
        }

        VersionedSmartYamlConfiguration pricesYAML =
                new VersionedSmartYamlConfiguration(new File(getDataFolder(), "prices.yml"),
                        getResource("prices.yml"), VersionedSmartConfiguration.VersionUpdateType.BACKUP_AND_UPDATE);
        if (pricesYAML.update()) {
            getLogger().info("Updating prices.yml");
        }

        settings = new MasterConfiguration();
        settings.load(configYAML, rewardsYAML, languageYAML, pricesYAML);

        manager = new MintManager();

        try {
            economy = MintEconomy.class.getConstructor(MintPlugin.class).newInstance(this);
            getServer().getServicesManager().register(Economy.class, economy, this, ServicePriority.Normal);
        } catch (Exception e) {
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        if (settings.getBoolean("config.database.enabled")) {
            dataStorage = new MySqlDataStorage(settings.getString("config.database.host"),
                    settings.getString("config.database.port"), settings.getString("config.database.database"),
                    settings.getString("config.database.username"), settings.getString("config.database.password"));
        } else {
            dataStorage = new YamlDataStorage(this);
        }
        dataStorage.initialize();

        for (PlayerAccount account : dataStorage.loadPlayerAccounts()) {
            manager.setPlayerBalance(account.getOwner(), account.getBalance());
        }
        for (BankAccount account : dataStorage.loadBankAccounts()) {
            manager.setBankBalance(account.getOwner(), account.getBalance());
        }

        MintRunnable mintRunnable = new MintRunnable(this);
        mintRunnable.runTaskTimer(this, 0, 20L * 5);

        Bukkit.getScheduler().runTaskTimer(this, new Runnable() {
            @Override
            public void run() {
                dataStorage.saveBankAccounts(manager.getBankAccounts());
                dataStorage.savePlayerAccounts(manager.getPlayerAccounts());
            }
        }, 20L * 300, 20L * 300);

        MintListener listener = new MintListener(this);
        Bukkit.getPluginManager().registerEvents(listener, this);
        new CommandHandler(this).registerCommands(new MintCommand(this));
    }

    @Override
    public void disable() {
        dataStorage.saveBankAccounts(manager.getBankAccounts());
        dataStorage.savePlayerAccounts(manager.getPlayerAccounts());
        dataStorage.shutdown();
    }

    public MasterConfiguration getSettings() {
        return settings;
    }

    public PluginLogger getDebugPrinter() {
        return debugPrinter;
    }

    public FacecorePlugin getFacecore() {
        return facecorePlugin;
    }

    public MintManager getManager() {
        return manager;
    }
}
