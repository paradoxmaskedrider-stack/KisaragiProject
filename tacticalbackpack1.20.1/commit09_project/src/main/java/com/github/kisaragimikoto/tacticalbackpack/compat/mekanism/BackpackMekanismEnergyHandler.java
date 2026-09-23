package com.github.kisaragimikoto.tacticalbackpack.compat.mekanism;

public class BackpackMekanismEnergyHandler {

    private static final double JOULES_PER_FE = 2.5D;

    public static long toJoules(int fe) {
        return Math.round(fe * JOULES_PER_FE);
    }

    public static int toFE(long joules) {
        return (int) Math.round(joules / JOULES_PER_FE);
    }

    public static long insertFEAsJoules(Object container, int fe) {
        return toJoules(fe);
    }

    public static int extractFEFromJoules(Object container, long joules) {
        return toFE(joules);
    }
}
