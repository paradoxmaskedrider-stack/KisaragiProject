package com.github.kisaragimikoto.tacticalbackpack.compat.ae2;

import com.github.kisaragimikoto.tacticalbackpack.inventory.BackpackInventory;
import com.github.kisaragimikoto.tacticalbackpack.item.TacticalBackpackItem;
import net.minecraft.world.item.ItemStack;

/** Item transfer operations shared by the GUI/network packet layer. */
public final class AE2BackpackTransferService {

    public record TransferResult(int moved, int touchedSlots, boolean online) { }

    public static TransferResult pushAll(ItemStack backpack, AE2StorageAccess storage, int maxItems) {
        if (backpack == null || backpack.isEmpty() || storage == null || !storage.isOnline()) {
            return new TransferResult(0, 0, false);
        }

        BackpackInventory inv = TacticalBackpackItem.getInventory(backpack);
        int storageSize = BackpackInventory.getStorageSize(backpack);
        int remainingLimit = maxItems <= 0 ? Integer.MAX_VALUE : maxItems;
        int moved = 0;
        int touched = 0;

        for (int slot = 0; slot < storageSize && remainingLimit > 0; slot++) {
            ItemStack current = inv.getItem(slot);
            if (current.isEmpty()) continue;

            int offerCount = Math.min(current.getCount(), remainingLimit);
            ItemStack offered = current.copy();
            offered.setCount(offerCount);
            int accepted = Math.max(0, Math.min(offerCount, storage.insert(offered, false)));
            if (accepted <= 0) continue;

            current.shrink(accepted);
            inv.setItem(slot, current);
            moved += accepted;
            remainingLimit -= accepted;
            touched++;
        }

        TacticalBackpackItem.saveInventory(backpack, inv);
        return new TransferResult(moved, touched, true);
    }

    public static int pull(ItemStack backpack, AE2StorageAccess storage, ItemStack template, int amount) {
        if (backpack == null || backpack.isEmpty() || storage == null || !storage.isOnline()
                || template == null || template.isEmpty() || amount <= 0) {
            return 0;
        }

        BackpackInventory inv = TacticalBackpackItem.getInventory(backpack);
        int storageSize = BackpackInventory.getStorageSize(backpack);
        ItemStack simulated = storage.extract(template, amount, true);
        if (simulated.isEmpty()) return 0;

        int room = calculateRoom(inv, storageSize, simulated);
        int wanted = Math.min(amount, room);
        if (wanted <= 0) return 0;

        ItemStack extracted = storage.extract(template, wanted, false);
        if (extracted.isEmpty()) return 0;

        int inserted = insertIntoBackpack(inv, storageSize, extracted.copy());
        TacticalBackpackItem.saveInventory(backpack, inv);
        return inserted;
    }

    private static int calculateRoom(BackpackInventory inv, int storageSize, ItemStack stack) {
        int room = 0;
        for (int slot = 0; slot < storageSize; slot++) {
            ItemStack existing = inv.getItem(slot);
            if (existing.isEmpty()) {
                room += stack.getMaxStackSize();
            } else if (ItemStack.isSameItemSameTags(existing, stack)) {
                room += Math.max(0, Math.min(existing.getMaxStackSize(), inv.getMaxStackSize()) - existing.getCount());
            }
            if (room >= stack.getCount()) return room;
        }
        return room;
    }

    private static int insertIntoBackpack(BackpackInventory inv, int storageSize, ItemStack stack) {
        int original = stack.getCount();

        for (int slot = 0; slot < storageSize && !stack.isEmpty(); slot++) {
            ItemStack existing = inv.getItem(slot);
            if (!existing.isEmpty() && ItemStack.isSameItemSameTags(existing, stack)) {
                int max = Math.min(existing.getMaxStackSize(), inv.getMaxStackSize());
                int move = Math.min(stack.getCount(), max - existing.getCount());
                if (move > 0) {
                    existing.grow(move);
                    stack.shrink(move);
                    inv.setItem(slot, existing);
                }
            }
        }

        for (int slot = 0; slot < storageSize && !stack.isEmpty(); slot++) {
            if (inv.getItem(slot).isEmpty()) {
                int move = Math.min(stack.getCount(), Math.min(stack.getMaxStackSize(), inv.getMaxStackSize()));
                ItemStack placed = stack.copy();
                placed.setCount(move);
                inv.setItem(slot, placed);
                stack.shrink(move);
            }
        }

        return original - stack.getCount();
    }

    private AE2BackpackTransferService() { }
}
