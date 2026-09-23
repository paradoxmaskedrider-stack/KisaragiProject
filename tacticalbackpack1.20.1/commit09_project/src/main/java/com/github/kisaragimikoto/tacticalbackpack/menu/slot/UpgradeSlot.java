package com.github.kisaragimikoto.tacticalbackpack.menu.slot;

import com.github.kisaragimikoto.tacticalbackpack.registry.ModItems;

import net.minecraft.world.inventory.Slot;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;

public class UpgradeSlot extends Slot {

    public UpgradeSlot(Container container, int index, int x, int y) {
        super(container, index, x, y);
    }

    @Override
    public boolean mayPlace(ItemStack stack) {
        return stack.is(ModItems.AUTO_PICKUP_MODULE.get())
                || stack.is(ModItems.FILTER_MODULE.get())
                || stack.is(ModItems.SORT_MODULE.get())
                || stack.is(ModItems.WORKBENCH_MODULE.get())
                || stack.is(ModItems.STABILITY_MODULE.get())
                || stack.is(ModItems.MEMORY_CARD.get());
    }

    @Override
    public int getMaxStackSize() {
        return 1;
    }
}