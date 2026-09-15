package com.github.kisaragimikoto.tacticalbackpack.menu.factory;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;

@FunctionalInterface
public interface BackpackMenuSupplier<T extends AbstractContainerMenu> {

    T create(
            int windowId,
            Inventory inventory,
            FriendlyByteBuf buffer
    );

}