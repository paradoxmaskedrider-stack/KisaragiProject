package com.github.kisaragimikoto.tacticalbackpack.menu.factory;

import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;

@FunctionalInterface
public interface BackpackMenuFactory<T extends AbstractContainerMenu> {

    T create(
            int windowId,
            Inventory inventory,
            ItemStack backpack
    );

}