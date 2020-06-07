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
package org.nunnerycode.mint;

import com.tealcube.minecraft.bukkit.facecore.plugin.FacePlugin;
import info.faceland.mint.MintCommand;
import info.faceland.mint.MintEconomy;
import info.faceland.mint.listeners.DeathListener;
import info.faceland.mint.listeners.ItemSpawnListener;
import info.faceland.mint.listeners.MintListener;
import info.faceland.mint.managers.MintManager;
import io.pixeloutlaw.minecraft.spigot.config.MasterConfiguration;
import io.pixeloutlaw.minecraft.spigot.config.VersionedConfiguration;
import io.pixeloutlaw.minecraft.spigot.config.VersionedSmartYamlConfiguration;
import java.io.File;
import java.text.DecimalFormat;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.event.HandlerList;
import org.bukkit.plugin.ServicePriority;
import org.bukkit.scheduler.BukkitTask;
import org.nunnerycode.mint.accounts.BankAccount;
import org.nunnerycode.mint.accounts.PlayerAccount;
import org.nunnerycode.mint.storage.DataStorage;
import org.nunnerycode.mint.storage.MySqlDataStorage;
import org.nunnerycode.mint.storage.YamlDataStorage;
import se.ranzdo.bukkit.methodcommand.CommandHandler;

public class MintPlugin extends FacePlugin {

  private static MintPlugin _INSTANCE;

  private MasterConfiguration settings;
  private MintEconomy economy;
  private MintManager manager;
  private DataStorage dataStorage;
  private BukkitTask saveTask;

  public static final DecimalFormat INT_FORMAT = new DecimalFormat("###,###,###");
  public static final DecimalFormat DEC_FORMAT = new DecimalFormat("###,###,###.##");

  public MintPlugin() {
    _INSTANCE = this;
    MintPlaceholders mintPlaceholders = new MintPlaceholders();
    mintPlaceholders.register();
  }

  public static MintPlugin getInstance() {
    return _INSTANCE;
  }

  @Override
  public void enable() {
    VersionedSmartYamlConfiguration configYAML =
        new VersionedSmartYamlConfiguration(new File(getDataFolder(), "config.yml"),
            getResource("config.yml"), VersionedConfiguration.VersionUpdateType.BACKUP_AND_UPDATE);
    if (configYAML.update()) {
      getLogger().info("Updating config.yml");
    }

    VersionedSmartYamlConfiguration rewardsYAML =
        new VersionedSmartYamlConfiguration(new File(getDataFolder(), "rewards.yml"),
            getResource("rewards.yml"), VersionedConfiguration.VersionUpdateType.BACKUP_AND_UPDATE);
    if (rewardsYAML.update()) {
      getLogger().info("Updating rewards.yml");
    }

    VersionedSmartYamlConfiguration languageYAML =
        new VersionedSmartYamlConfiguration(new File(getDataFolder(), "language.yml"),
            getResource("language.yml"),
            VersionedConfiguration.VersionUpdateType.BACKUP_AND_UPDATE);
    if (languageYAML.update()) {
      getLogger().info("Updating language.yml");
    }

    VersionedSmartYamlConfiguration pricesYAML =
        new VersionedSmartYamlConfiguration(new File(getDataFolder(), "prices.yml"),
            getResource("prices.yml"), VersionedConfiguration.VersionUpdateType.BACKUP_AND_UPDATE);
    if (pricesYAML.update()) {
      getLogger().info("Updating prices.yml");
    }

    settings = new MasterConfiguration();
    settings.load(configYAML, rewardsYAML, languageYAML, pricesYAML);

    manager = new MintManager();

    try {
      economy = MintEconomy.class.getConstructor(MintPlugin.class).newInstance(this);
      getServer().getServicesManager()
          .register(Economy.class, economy, this, ServicePriority.Normal);
    } catch (Exception e) {
      getServer().getPluginManager().disablePlugin(this);
      return;
    }

    if (settings.getBoolean("config.database.enabled")) {
      dataStorage = new MySqlDataStorage(settings.getString("config.database.host"),
          settings.getString("config.database.port"),
          settings.getString("config.database.database"),
          settings.getString("config.database.username"),
          settings.getString("config.database.password"));
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

    saveTask = Bukkit.getScheduler().runTaskTimer(this, () -> {
      dataStorage.savePlayerAccounts(manager.getPlayerAccounts());
      dataStorage.saveBankAccounts(manager.getBankAccounts());
    }, 20L * 112, 20L * 300);

    MintListener listener = new MintListener(this);
    ItemSpawnListener itemSpawnListener = new ItemSpawnListener(this);
    DeathListener deathListener = new DeathListener(this);

    Bukkit.getPluginManager().registerEvents(listener, this);
    Bukkit.getPluginManager().registerEvents(itemSpawnListener, this);
    Bukkit.getPluginManager().registerEvents(deathListener, this);

    new CommandHandler(this).registerCommands(new MintCommand(this));
  }

  @Override
  public void disable() {
    dataStorage.saveBankAccounts(manager.getBankAccounts());
    dataStorage.savePlayerAccounts(manager.getPlayerAccounts());
    dataStorage.shutdown();

    saveTask.cancel();
    HandlerList.unregisterAll(this);
  }

  public MasterConfiguration getSettings() {
    return settings;
  }

  public MintEconomy getEconomy() {
    return economy;
  }

  public MintManager getManager() {
    return manager;
  }
}
