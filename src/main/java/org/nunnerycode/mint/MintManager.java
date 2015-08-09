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
package org.nunnerycode.mint;

import com.tealcube.minecraft.bukkit.shade.apache.commons.lang3.Validate;
import org.nunnerycode.mint.accounts.BankAccount;
import org.nunnerycode.mint.accounts.PlayerAccount;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class MintManager {

    private final Map<UUID, PlayerAccount> playerAccountMap;
    private final Map<UUID, BankAccount> bankAccountMap;

    public MintManager() {
        playerAccountMap = new ConcurrentHashMap<>();
        bankAccountMap = new ConcurrentHashMap<>();
    }

    public double getPlayerBalance(UUID uuid) {
        Validate.notNull(uuid, "uuid cannot be null");
        if (!playerAccountMap.containsKey(uuid)) {
            playerAccountMap.put(uuid, new PlayerAccount(uuid));
        }
        return playerAccountMap.get(uuid).getBalance();
    }

    public void setPlayerBalance(UUID uuid, double balance) {
        Validate.notNull(uuid, "uuid cannot be null");
        PlayerAccount account =
                playerAccountMap.containsKey(uuid) ? playerAccountMap.get(uuid) : new PlayerAccount(uuid);
        account.setBalance(balance);
        playerAccountMap.put(uuid, account);
    }

    public double getBankBalance(UUID uuid) {
        Validate.notNull(uuid, "uuid cannot be null");
        if (!bankAccountMap.containsKey(uuid)) {
            bankAccountMap.put(uuid, new BankAccount(uuid));
        }
        return bankAccountMap.get(uuid).getBalance();
    }

    public void setBankBalance(UUID uuid, double balance) {
        Validate.notNull(uuid, "uuid cannot be null");
        BankAccount account =
                bankAccountMap.containsKey(uuid) ? bankAccountMap.get(uuid) : new BankAccount(uuid);
        account.setBalance(balance);
        bankAccountMap.put(uuid, account);
    }

    public boolean hasPlayerAccount(UUID uuid) {
        Validate.notNull(uuid, "uuid cannot be null");
        return playerAccountMap.containsKey(uuid);
    }

    public boolean hasBankAccount(UUID uuid) {
        Validate.notNull(uuid, "uuid cannot be null");
        return bankAccountMap.containsKey(uuid);
    }

    public void removeBankAccount(UUID uuid) {
        Validate.notNull(uuid);
        bankAccountMap.remove(uuid);
    }

    public List<String> banksAsStrings() {
        List<String> strings = new ArrayList<>();
        for (UUID uuid : bankAccountMap.keySet()) {
            strings.add(uuid.toString());
        }
        return strings;
    }

    public Set<PlayerAccount> getPlayerAccounts() {
        return new HashSet<>(playerAccountMap.values());
    }

    public Set<BankAccount> getBankAccounts() {
        return new HashSet<>(bankAccountMap.values());
    }

}
