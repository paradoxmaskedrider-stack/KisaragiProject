package com.github.kisaragimikoto.tacticalbackpack.compat.ae2;

import net.minecraft.world.item.ItemStack;

/** A displayable item variant currently available in the linked ME network. */
public record AE2ItemEntry(ItemStack stack, long amount) {
    public AE2ItemEntry {
        stack = stack == null ? ItemStack.EMPTY : stack.copy();
        if (!stack.isEmpty()) stack.setCount(1);
        amount = Math.max(0L, amount);
    }
}
