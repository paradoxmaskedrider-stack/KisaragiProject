package com.github.kisaragimikoto.tacticalbackpack.menu.util;

import com.github.kisaragimikoto.tacticalbackpack.menu.factory.BackpackMenuFactory;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.ItemStack;

import net.minecraftforge.common.extensions.IForgeMenuType;

public final class MenuUtil {

    private MenuUtil() {}

    public static ItemStack readBackpack(FriendlyByteBuf buffer) {

        return buffer.readItem();

    }

    public static <T extends AbstractContainerMenu> MenuType<T> createBackpackMenu(
            BackpackMenuFactory<T> factory
    ) {

        return IForgeMenuType.create((windowId, inventory, buffer) ->

                factory.create(
                        windowId,
                        inventory,
                        readBackpack(buffer)
                ));

    }

}