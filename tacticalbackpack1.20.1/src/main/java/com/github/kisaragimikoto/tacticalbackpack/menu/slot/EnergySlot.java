package com.github.kisaragimikoto.tacticalbackpack.menu.slot;

import net.minecraft.world.inventory.Slot;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;

public class EnergySlot extends Slot {

    public EnergySlot(Container container, int index, int x, int y) {
        super(container, index, x, y);
    }

    @Override
    public boolean mayPlace(ItemStack stack) {
        String id = stack.getItem().toString().toLowerCase();
        return id.contains("battery")
                || id.contains("energy")
                || id.contains("mekanism");
    }
}