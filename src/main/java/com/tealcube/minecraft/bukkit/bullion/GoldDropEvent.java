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
package com.tealcube.minecraft.bukkit.bullion;

import info.faceland.mint.MintEvent;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

public class GoldDropEvent extends MintEvent {

    private final Player killer;
    private final LivingEntity livingEntity;
    private double amount;

    public GoldDropEvent(Player killer, LivingEntity livingEntity, double amount) {
        super(killer != null ? killer.getUniqueId().toString() : "");
        this.killer = killer;
        this.livingEntity = livingEntity;
        this.amount = amount;
    }

    public Player getKiller() {
        return killer;
    }

    public LivingEntity getLivingEntity() {
        return livingEntity;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

}
