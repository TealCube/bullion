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

import com.tealcube.minecraft.bukkit.kern.apache.commons.lang3.Validate;
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
