package com.github.kisaragimikoto.tacticalbackpack.inventory;

import net.minecraft.world.SimpleContainer;
import net.minecraft.world.item.ItemStack;

public class BackpackEquipmentContainer extends SimpleContainer {

    public static final int ELYTRA_SLOT = 0;

    public BackpackEquipmentContainer() {
        super(1);
    }

    public boolean hasElytra() {
        ItemStack stack = getItem(ELYTRA_SLOT);
        return !stack.isEmpty();
    }
}