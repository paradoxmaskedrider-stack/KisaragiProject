package com.github.kisaragimikoto.tacticalbackpack.compat.ae2;

import net.minecraft.world.item.ItemStack;

import java.util.List;

/**
 * Loader-safe abstraction over an AE2 ME inventory.
 * An AE2 API backed implementation is installed only when AE2 is present.
 */
public interface AE2StorageAccess {
    AE2StorageAccess OFFLINE = new AE2StorageAccess() {
        @Override public boolean isOnline() { return false; }
        @Override public int insert(ItemStack stack, boolean simulate) { return 0; }
        @Override public ItemStack extract(ItemStack template, int amount, boolean simulate) { return ItemStack.EMPTY; }
    };

    boolean isOnline();

    /** @return number of items accepted */
    int insert(ItemStack stack, boolean simulate);

    /** @return extracted stack, or ItemStack.EMPTY */
    ItemStack extract(ItemStack template, int amount, boolean simulate);

    /** Snapshot of item variants currently available in this ME inventory. */
    default List<AE2ItemEntry> getAvailableItems() {
        return List.of();
    }
}
