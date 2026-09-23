package com.github.kisaragimikoto.tacticalbackpack.inventory;

import net.minecraft.world.SimpleContainer;
import net.minecraft.world.item.ItemStack;

public class BackpackSharedStorage extends SimpleContainer {

    private static final BackpackSharedStorage INSTANCE = new BackpackSharedStorage();

    public static BackpackSharedStorage get() {
        return INSTANCE;
    }

    private BackpackSharedStorage() {
        super(81); // 9x9
    }

    @Override
    public boolean canPlaceItem(int slot, ItemStack stack) {
        return true;
    }
}