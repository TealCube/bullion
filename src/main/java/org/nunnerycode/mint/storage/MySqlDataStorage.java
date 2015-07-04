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

import com.tealcube.minecraft.bukkit.facecore.database.MySqlDatabasePool;
import com.tealcube.minecraft.bukkit.kern.apache.commons.lang3.Validate;
import com.tealcube.minecraft.bukkit.kern.io.CloseableRegistry;
import com.tealcube.minecraft.bukkit.kern.shade.google.common.base.Optional;
import org.nunnerycode.mint.accounts.BankAccount;
import org.nunnerycode.mint.accounts.PlayerAccount;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class MySqlDataStorage implements DataStorage {

    private final String host;
    private final String port;
    private final String database;
    private final String username;
    private final String password;
    private MySqlDatabasePool mySqlPool;

    public MySqlDataStorage(String host, String port, String database, String username, String password) {
        this.host = host;
        this.port = port;
        this.database = database;
        this.username = username;
        this.password = password;
    }

    @Override
    public void initialize() {
        if (mySqlPool != null) {
            return;
        }
        mySqlPool = new MySqlDatabasePool(host, port, database, username, password);
        mySqlPool.initialize();
        migrate();
    }

    private void migrate() {
        CloseableRegistry registry = new CloseableRegistry();
        Connection connection = mySqlPool.getConnection();

        try {
            boolean tablesExist = tryQuery(connection, "SELECT * FROM mint_players LIMIT 1");
            if (!tablesExist) {
                tryUpdate(connection, "create table mint_players (ID varchar(100) PRIMARY KEY, BALANCE decimal(20, 2) not null)");
            }
        } finally {
            registry.closeQuietly();
        }


        registry.closeQuietly();
    }

    private boolean tryUpdate(Connection conn, String sql) {
        CloseableRegistry registry = new CloseableRegistry();
        try {
            Statement statement = registry.register(conn.createStatement());
            statement.executeUpdate(sql);
            return true;
        } catch (SQLException ex) {
            return false;
        } finally {
            registry.closeQuietly();
        }
    }

    private boolean tryQuery(Connection conn, String sql) {
        CloseableRegistry registry = new CloseableRegistry();
        try {
            Statement statement = registry.register(conn.createStatement());
            statement.executeQuery(sql);
            return true;
        } catch (SQLException ex) {
            return false;
        } finally {
            registry.closeQuietly();
        }
    }

    @Override
    public void shutdown() {
        if (mySqlPool == null) {
            return;
        }
        mySqlPool.shutdown();
        mySqlPool = null;
    }

    @Override
    public Optional<PlayerAccount> loadPlayerAccount(UUID uuid) {
        Validate.notNull(uuid, "uuid cannot be null");
        Optional<PlayerAccount> accountOptional = Optional.absent();
        if (mySqlPool == null) {
            throw new IllegalStateException("connection pool is not initialized");
        }
        Connection connection = mySqlPool.getConnection();
        CloseableRegistry registry = new CloseableRegistry();
        try {
            PreparedStatement statement = connection.prepareStatement("SELECT * FROM mint_players WHERE ID = ?");
            statement = registry.register(statement);
            statement.setString(1, uuid.toString());
            ResultSet resultSet = statement.executeQuery();
            resultSet = registry.register(resultSet);
            PlayerAccount account = new PlayerAccount(uuid);
            while (resultSet.next()) {
                account = new PlayerAccount(uuid, resultSet.getDouble("BALANCE"));
            }
            accountOptional = Optional.of(account);
        } catch (SQLException ignored) {
            // do nothing
        } finally {
            registry.closeQuietly();
        }
        return accountOptional;
    }

    @Override
    public Optional<BankAccount> loadBankAccount(UUID uuid) {
        Validate.notNull(uuid, "uuid cannot be null");
        Optional<BankAccount> accountOptional = Optional.absent();
        if (mySqlPool == null) {
            throw new IllegalStateException("connection pool is not initialized");
        }
        Connection connection = mySqlPool.getConnection();
        CloseableRegistry registry = new CloseableRegistry();
        try {
            PreparedStatement statement = connection.prepareStatement("SELECT * FROM mint_banks WHERE ID = ?");
            statement = registry.register(statement);
            statement.setString(1, uuid.toString());
            ResultSet resultSet = statement.executeQuery();
            resultSet = registry.register(resultSet);
            BankAccount account = new BankAccount(uuid);
            while (resultSet.next()) {
                account = new BankAccount(uuid, resultSet.getDouble("BALANCE"));
            }
            accountOptional = Optional.of(account);
        } catch (SQLException ignored) {
            // do nothing
        } finally {
            registry.closeQuietly();
        }
        return accountOptional;
    }

    @Override
    public boolean savePlayerAccount(PlayerAccount account) {
        Validate.notNull(account, "account cannot be null");
        if (mySqlPool == null) {
            throw new IllegalStateException("connection pool is not initialized");
        }
        Connection connection = mySqlPool.getConnection();
        CloseableRegistry registry = new CloseableRegistry();
        boolean successful = false;
        try {
            PreparedStatement statement = connection.prepareStatement(
                    "INSERT INTO mint_players (ID, BALANCE) VALUES (?, ?) ON DUPLICATE KEY UPDATE BALANCE=?");
            statement = registry.register(statement);
            statement.setString(1, account.getOwner().toString());
            statement.setDouble(2, account.getBalance());
            statement.setDouble(3, account.getBalance());
            successful = statement.executeUpdate() != 0;
        } catch (SQLException ignored) {
            // do nothing
        } finally {
            registry.closeQuietly();
        }
        return successful;
    }

    @Override
    public boolean saveBankAccount(BankAccount account) {
        Validate.notNull(account, "account cannot be null");
        if (mySqlPool == null) {
            throw new IllegalStateException("connection pool is not initialized");
        }
        Connection connection = mySqlPool.getConnection();
        CloseableRegistry registry = new CloseableRegistry();
        boolean successful = false;
        try {
            PreparedStatement statement = connection.prepareStatement(
                    "INSERT INTO mint_banks (ID, BALANCE) VALUES (?, ?) ON DUPLICATE KEY UPDATE BALANCE=?");
            statement = registry.register(statement);
            statement.setString(1, account.getOwner().toString());
            statement.setDouble(2, account.getBalance());
            statement.setDouble(3, account.getBalance());
            successful = statement.executeUpdate() != 0;
        } catch (SQLException ignored) {
            // do nothing
        } finally {
            registry.closeQuietly();
        }
        return successful;
    }

    @Override
    public Set<PlayerAccount> loadPlayerAccounts() {
        Set<PlayerAccount> accounts = new HashSet<>();
        if (mySqlPool == null) {
            throw new IllegalStateException("connection pool is not initialized");
        }
        Connection connection = mySqlPool.getConnection();
        CloseableRegistry registry = new CloseableRegistry();
        try {
            PreparedStatement statement = connection.prepareStatement("SELECT * FROM mint_players");
            statement = registry.register(statement);
            ResultSet resultSet = statement.executeQuery();
            resultSet = registry.register(resultSet);
            while (resultSet.next()) {
                accounts.add(new PlayerAccount(UUID.fromString(resultSet.getString("ID")),
                        resultSet.getDouble("BALANCE")));
            }
        } catch (SQLException ignored) {
            // do nothing
        } finally {
            registry.closeQuietly();
        }
        return accounts;
    }

    @Override
    public Set<BankAccount> loadBankAccounts() {
        Set<BankAccount> accounts = new HashSet<>();
        if (mySqlPool == null) {
            throw new IllegalStateException("connection pool is not initialized");
        }
        Connection connection = mySqlPool.getConnection();
        CloseableRegistry registry = new CloseableRegistry();
        try {
            PreparedStatement statement = connection.prepareStatement("SELECT * FROM mint_banks");
            statement = registry.register(statement);
            ResultSet resultSet = statement.executeQuery();
            resultSet = registry.register(resultSet);
            while (resultSet.next()) {
                accounts.add(new BankAccount(UUID.fromString(resultSet.getString("ID")),
                        resultSet.getDouble("BALANCE")));
            }
        } catch (SQLException ignored) {
            // do nothing
        } finally {
            registry.closeQuietly();
        }
        return accounts;
    }

    @Override
    public boolean savePlayerAccounts(Set<PlayerAccount> accounts) {
        Validate.notNull(accounts, "accounts cannot be null");
        if (mySqlPool == null) {
            throw new IllegalStateException("connection pool is not initialized");
        }
        Connection connection = mySqlPool.getConnection();
        CloseableRegistry registry = new CloseableRegistry();
        try {
            PreparedStatement statement = connection.prepareStatement(
                    "INSERT INTO mint_players (ID, BALANCE) VALUES (?, ?) ON DUPLICATE KEY UPDATE BALANCE=?");
            statement = registry.register(statement);
            for (PlayerAccount account : accounts) {
                statement.setString(1, account.getOwner().toString());
                statement.setDouble(2, account.getBalance());
                statement.setDouble(3, account.getBalance());
                statement.executeUpdate();
            }
        } catch (SQLException ignored) {
            // do nothing
        } finally {
            registry.closeQuietly();
        }
        return true;
    }

    @Override
    public boolean saveBankAccounts(Set<BankAccount> accounts) {
        Validate.notNull(accounts, "accounts cannot be null");
        if (mySqlPool == null) {
            throw new IllegalStateException("connection pool is not initialized");
        }
        Connection connection = mySqlPool.getConnection();
        CloseableRegistry registry = new CloseableRegistry();
        try {
            PreparedStatement statement = connection.prepareStatement(
                    "INSERT INTO mint_banks (ID, BALANCE) VALUES (?, ?) ON DUPLICATE KEY UPDATE BALANCE=?");
            statement = registry.register(statement);
            for (BankAccount account : accounts) {
                statement.setString(1, account.getOwner().toString());
                statement.setDouble(2, account.getBalance());
                statement.setDouble(3, account.getBalance());
                statement.executeUpdate();
            }
        } catch (SQLException ignored) {
            // do nothing
        } finally {
            registry.closeQuietly();
        }
        return true;
    }
}
