package com.github.kisaragimikoto.tacticalbackpack.inventory;

import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.IItemHandler;

import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;

import javax.annotation.Nullable;

public class BackpackItemCapabilityProvider implements ICapabilityProvider {

    private final LazyOptional<IItemHandler> handler;

    public BackpackItemCapabilityProvider(ItemStack stack) {
        handler = LazyOptional.of(() -> new BackpackItemHandler(stack));
    }

    @Override
    public <T> LazyOptional<T> getCapability(net.minecraftforge.common.capabilities.Capability<T> cap,
                                             @Nullable Direction side) {

        if (cap == ForgeCapabilities.ITEM_HANDLER) {
            return handler.cast();
        }

        return LazyOptional.empty();
    }
}