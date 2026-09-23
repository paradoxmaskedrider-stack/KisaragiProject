package com.github.kisaragimikoto.tacticalbackpack.menu.slot;

import com.github.kisaragimikoto.tacticalbackpack.registry.ModItems;

import net.minecraft.world.inventory.Slot;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;

public class SizeSlot extends Slot {

    public SizeSlot(Container container, int index, int x, int y) {
        super(container, index, x, y);
    }

    @Override
    public boolean mayPlace(ItemStack stack) {
        return stack.is(ModItems.SIZE_UPGRADE_1.get())
                || stack.is(ModItems.SIZE_UPGRADE_2.get());
    }

    @Override
    public int getMaxStackSize() {
        return 1;
    }
}