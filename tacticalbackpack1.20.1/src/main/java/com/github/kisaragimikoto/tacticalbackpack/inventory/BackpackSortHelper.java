package com.github.kisaragimikoto.tacticalbackpack.inventory;

import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class BackpackSortHelper {

    public static void sortStorage(BackpackInventory inventory, int storageSize) {

        List<ItemStack> stacks = new ArrayList<>();

        for (int i = 0; i < storageSize; i++) {
            ItemStack stack = inventory.getItem(i);
            if (!stack.isEmpty()) {
                stacks.add(stack.copy());
            }
            inventory.setItem(i, ItemStack.EMPTY);
        }

        stacks.sort(Comparator
                .comparing((ItemStack s) -> s.getItem().toString())
                .thenComparingInt(ItemStack::getCount));

        List<ItemStack> merged = new ArrayList<>();

        for (ItemStack stack : stacks) {
            boolean mergedIn = false;

            for (ItemStack existing : merged) {
                if (ItemStack.isSameItemSameTags(existing, stack)
                        && existing.getCount() < existing.getMaxStackSize()) {

                    int move = Math.min(stack.getCount(), existing.getMaxStackSize() - existing.getCount());
                    existing.grow(move);
                    stack.shrink(move);

                    if (stack.isEmpty()) {
                        mergedIn = true;
                        break;
                    }
                }
            }

            if (!stack.isEmpty()) {
                merged.add(stack.copy());
            } else if (mergedIn) {
                // no-op
            }
        }

        int index = 0;
        for (ItemStack stack : merged) {
            if (index >= storageSize) {
                break;
            }
            inventory.setItem(index, stack);
            index++;
        }
    }
}