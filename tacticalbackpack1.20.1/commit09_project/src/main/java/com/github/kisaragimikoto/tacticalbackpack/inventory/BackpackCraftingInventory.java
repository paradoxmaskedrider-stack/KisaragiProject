package com.github.kisaragimikoto.tacticalbackpack.inventory;

import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.TransientCraftingContainer;
import net.minecraft.world.item.ItemStack;

public class BackpackCraftingInventory extends TransientCraftingContainer {

    public BackpackCraftingInventory(AbstractContainerMenu menu) {
        super(menu, 3, 3);
    }

    public void clearAll() {
        for (int i = 0; i < getContainerSize(); i++) {
            setItem(i, ItemStack.EMPTY);
        }
    }
}