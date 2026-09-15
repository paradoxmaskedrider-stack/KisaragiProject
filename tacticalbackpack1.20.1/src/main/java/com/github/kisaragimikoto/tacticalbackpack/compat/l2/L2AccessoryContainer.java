package com.github.kisaragimikoto.tacticalbackpack.compat.l2;

import net.minecraft.world.SimpleContainer;

public class L2AccessoryContainer extends SimpleContainer {

    public static final int RING_1 = 0;
    public static final int RING_2 = 1;
    public static final int AMULET = 2;
    public static final int CHARM = 3;
    public static final int RELIC = 4;

    public L2AccessoryContainer() {
        super(5);
    }
}
