package com.github.kisaragimikoto.tacticalbackpack.energy;

import net.minecraftforge.energy.IEnergyStorage;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraft.core.Direction;

import javax.annotation.Nullable;

public class BackpackEnergyProvider implements ICapabilityProvider {

    private final BackpackEnergyStorage energy = new BackpackEnergyStorage();
    private final LazyOptional<IEnergyStorage> optional = LazyOptional.of(() -> energy);

    @Override
    public <T> LazyOptional<T> getCapability(Capability<T> cap, @Nullable Direction side) {
        if (cap == ForgeCapabilities.ENERGY) {
            return optional.cast();
        }
        return LazyOptional.empty();
    }
}