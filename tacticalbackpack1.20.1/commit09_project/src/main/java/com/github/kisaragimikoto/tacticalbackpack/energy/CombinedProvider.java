package com.github.kisaragimikoto.tacticalbackpack.energy;

import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.LazyOptional;

import net.minecraft.core.Direction;

import javax.annotation.Nullable;

public class CombinedProvider implements ICapabilityProvider {

    private final ICapabilityProvider[] providers;

    public CombinedProvider(ICapabilityProvider... providers) {
        this.providers = providers;
    }

    @Override
    public <T> LazyOptional<T> getCapability(
            net.minecraftforge.common.capabilities.Capability<T> cap,
            @Nullable Direction side) {

        for (ICapabilityProvider provider : providers) {
            LazyOptional<T> result = provider.getCapability(cap, side);
            if (result.isPresent()) {
                return result;
            }
        }

        return LazyOptional.empty();
    }
}