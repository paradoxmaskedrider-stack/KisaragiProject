package com.github.kisaragimikoto.tacticalbackpack.inventory;

import com.github.kisaragimikoto.tacticalbackpack.item.TacticalBackpackItem;
import net.minecraft.world.item.ItemStack;

import java.util.HashSet;
import java.util.Set;
import java.util.function.Predicate;

/**
 * Safe recursive lookup for backpacks stored inside other backpacks.
 * Nested backpacks contribute their contents and installed modules, while a
 * stable backpack id prevents cycles and accidental self-reference.
 */
public final class BackpackContents {
    private static final int MAX_DEPTH = 32;

    private BackpackContents() {}

    public static boolean anyMatch(ItemStack rootBackpack, Predicate<ItemStack> predicate) {
        return anyMatch(rootBackpack, predicate, new HashSet<>(), 0);
    }

    private static boolean anyMatch(ItemStack backpack, Predicate<ItemStack> predicate,
                                    Set<String> visited, int depth) {
        if (backpack.isEmpty() || !(backpack.getItem() instanceof TacticalBackpackItem) || depth > MAX_DEPTH) {
            return false;
        }

        String id = TacticalBackpackItem.getOrCreateBackpackId(backpack);
        if (!visited.add(id)) {
            return false;
        }

        BackpackInventory inventory = TacticalBackpackItem.getInventory(backpack);
        for (int i = 0; i < inventory.getContainerSize(); i++) {
            ItemStack child = inventory.getItem(i);
            if (child.isEmpty()) continue;
            if (predicate.test(child)) return true;
            if (child.getItem() instanceof TacticalBackpackItem
                    && anyMatch(child, predicate, visited, depth + 1)) {
                return true;
            }
        }
        return false;
    }

    public static int countStorageSlots(ItemStack rootBackpack) {
        return countStorageSlots(rootBackpack, new HashSet<>(), 0);
    }

    private static int countStorageSlots(ItemStack backpack, Set<String> visited, int depth) {
        if (backpack.isEmpty() || !(backpack.getItem() instanceof TacticalBackpackItem) || depth > MAX_DEPTH) {
            return 0;
        }
        String id = TacticalBackpackItem.getOrCreateBackpackId(backpack);
        if (!visited.add(id)) return 0;

        int total = BackpackInventory.getStorageSize(backpack);
        BackpackInventory inventory = TacticalBackpackItem.getInventory(backpack);
        int ownStorage = BackpackInventory.getStorageSize(backpack);
        for (int i = 0; i < ownStorage; i++) {
            ItemStack child = inventory.getItem(i);
            if (child.getItem() instanceof TacticalBackpackItem) {
                total += countStorageSlots(child, visited, depth + 1);
            }
        }
        return Math.min(total, 50000);
    }
}
