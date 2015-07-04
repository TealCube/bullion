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
package org.nunnerycode.mint.accounts;

import java.util.UUID;

public class BankAccount implements Account {
    private final UUID owner;
    private double balance;

    public BankAccount(UUID owner) {
        this(owner, 0D);
    }

    public BankAccount(UUID owner, double balance) {
        this.owner = owner;
        this.balance = balance;
    }

    @Override
    public UUID getOwner() {
        return owner;
    }

    @Override
    public double getBalance() {
        return balance;
    }

    @Override
    public void setBalance(double newBalance) {
        this.balance = newBalance;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        BankAccount that = (BankAccount) o;

        return Double.compare(that.balance, balance) == 0 &&
                !(owner != null ? !owner.equals(that.owner) : that.owner != null);
    }

    @Override
    public int hashCode() {
        int result;
        long temp;
        result = owner != null ? owner.hashCode() : 0;
        temp = Double.doubleToLongBits(balance);
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        return result;
    }
}
