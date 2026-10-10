package com.github.kisaragimikoto.tacticalbackpack.inventory;

import com.github.kisaragimikoto.tacticalbackpack.item.TacticalBackpackItem;
import com.github.kisaragimikoto.tacticalbackpack.registry.ModItems;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.util.HashSet;
import java.util.Set;
import java.util.function.Predicate;

/**
 * Attachment utilities for TacticalBackpack.
 *
 * Attachments are active only while installed in one of the dedicated
 * attachment slots. Merely storing a module in the normal backpack storage
 * does not activate it.
 */
public final class BackpackAttachmentHelper {
    private static final int MAX_DEPTH = 32;

    private BackpackAttachmentHelper() {}

    public static boolean isAttachment(ItemStack stack) {
        if (stack == null || stack.isEmpty()) return false;

        return stack.is(ModItems.AUTO_PICKUP_MODULE.get())
                || stack.is(ModItems.FILTER_MODULE.get())
                || stack.is(ModItems.SORT_MODULE.get())
                || stack.is(ModItems.POCKET_CORE.get())
                || stack.is(ModItems.STABILITY_MODULE.get())
                || stack.is(ModItems.WORKBENCH_MODULE.get())
                || stack.is(ModItems.GUN_WORKBENCH_MODULE.get())
                || stack.is(ModItems.MEMORY_CARD.get())
                || stack.is(ModItems.SIZE_UPGRADE_1.get())
                || stack.is(ModItems.SIZE_UPGRADE_2.get());
    }

    public static boolean hasInstalled(ItemStack rootBackpack, Item item) {
        return anyInstalled(rootBackpack, stack -> stack.is(item));
    }

    /**
     * Checks installed attachment slots on the root backpack and on any nested
     * TacticalBackpacks stored in its normal storage area.
     */
    public static boolean anyInstalled(ItemStack rootBackpack, Predicate<ItemStack> predicate) {
        return anyInstalled(rootBackpack, predicate, new HashSet<>(), 0);
    }

    private static boolean anyInstalled(ItemStack backpack,
                                        Predicate<ItemStack> predicate,
                                        Set<String> visited,
                                        int depth) {
        if (backpack.isEmpty()
                || !(backpack.getItem() instanceof TacticalBackpackItem)
                || depth > MAX_DEPTH) {
            return false;
        }

        String id = TacticalBackpackItem.getOrCreateBackpackId(backpack);
        if (!visited.add(id)) return false;

        BackpackAttachmentContainer attachments = new BackpackAttachmentContainer(backpack);

        // Only the dedicated attachment slots activate modules/upgrades.
        for (int offset = 0; offset < attachments.getContainerSize(); offset++) {
            ItemStack installed = attachments.getItem(offset);
            if (!installed.isEmpty() && predicate.test(installed)) {
                return true;
            }
        }

        BackpackInventory inventory = TacticalBackpackItem.getInventory(backpack);
        int storageSize = BackpackInventory.getStorageSize(backpack);

        // Nested backpacks may contribute their own installed attachments.
        for (int slotIndex = 0; slotIndex < storageSize; slotIndex++) {
            ItemStack child = inventory.getItem(slotIndex);
            if (child.getItem() instanceof TacticalBackpackItem
                    && anyInstalled(child, predicate, visited, depth + 1)) {
                return true;
            }
        }

        return false;
    }
}
