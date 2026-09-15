package com.github.kisaragimikoto.tacticalbackpack.compat.mekanism;

import net.minecraftforge.fml.ModList;

public class MekanismIntegration {

    private static boolean loaded = false;

    public static void init() {
        loaded =
                ModList.get().isLoaded("mekanism") ||
                        ModList.get().isLoaded("mekanismgenerators") ||
                        ModList.get().isLoaded("mekanismtools");
    }

    public static boolean isLoaded() {
        return loaded;
    }
}