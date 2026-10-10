package com.github.kisaragimikoto.tacticalbackpack.inventory;

import com.github.kisaragimikoto.tacticalbackpack.item.TacticalBackpackItem;
import net.minecraft.world.item.ItemStack;

/** Loss-safe insertion helper for the normal storage portion of a backpack. */
public final class BackpackInsertionService {

    /**
     * Inserts as much of {@code input} as possible and returns the remainder.
     * Special attachment/upgrade slots are never touched.
     */
    public static ItemStack insert(ItemStack backpack, ItemStack input) {
        if (backpack == null || backpack.isEmpty() || input == null || input.isEmpty()) {
            return input == null ? ItemStack.EMPTY : input.copy();
        }

        BackpackInventory inventory = TacticalBackpackItem.getInventory(backpack);
        int storageSize = BackpackInventory.getStorageSize(backpack);
        ItemStack remaining = input.copy();

        // Merge first so existing stacks are filled before consuming a new slot.
        for (int slot = 0; slot < storageSize && !remaining.isEmpty(); slot++) {
            ItemStack existing = inventory.getItem(slot);
            if (existing.isEmpty() || !ItemStack.isSameItemSameTags(existing, remaining)) continue;

            int max = Math.min(existing.getMaxStackSize(), inventory.getMaxStackSize());
            int room = max - existing.getCount();
            if (room <= 0) continue;

            int move = Math.min(room, remaining.getCount());
            existing.grow(move);
            remaining.shrink(move);
            inventory.setItem(slot, existing);
        }

        // Then consume empty storage slots.
        for (int slot = 0; slot < storageSize && !remaining.isEmpty(); slot++) {
            if (!inventory.getItem(slot).isEmpty()) continue;

            int move = Math.min(remaining.getCount(), Math.min(remaining.getMaxStackSize(), inventory.getMaxStackSize()));
            ItemStack placed = remaining.copy();
            placed.setCount(move);
            inventory.setItem(slot, placed);
            remaining.shrink(move);
        }

        TacticalBackpackItem.saveInventory(backpack, inventory);
        return remaining;
    }

    public static boolean hasRoom(ItemStack backpack, ItemStack stack) {
        if (backpack == null || backpack.isEmpty() || stack == null || stack.isEmpty()) return false;
        BackpackInventory inventory = TacticalBackpackItem.getInventory(backpack);
        int storageSize = BackpackInventory.getStorageSize(backpack);
        for (int slot = 0; slot < storageSize; slot++) {
            ItemStack existing = inventory.getItem(slot);
            if (existing.isEmpty()) return true;
            if (ItemStack.isSameItemSameTags(existing, stack)
                    && existing.getCount() < Math.min(existing.getMaxStackSize(), inventory.getMaxStackSize())) {
                return true;
            }
        }
        return false;
    }

    private BackpackInsertionService() { }
}
