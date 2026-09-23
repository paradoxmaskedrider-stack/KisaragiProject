package com.github.kisaragimikoto.tacticalbackpack.energy;

import net.minecraftforge.energy.EnergyStorage;

public class BackpackEnergyStorage extends EnergyStorage {

    public BackpackEnergyStorage() {
        super(1000000, 5000, 5000);
    }

    @Override
    public boolean canExtract() {
        return true;
    }

    @Override
    public boolean canReceive() {
        return true;
    }
}