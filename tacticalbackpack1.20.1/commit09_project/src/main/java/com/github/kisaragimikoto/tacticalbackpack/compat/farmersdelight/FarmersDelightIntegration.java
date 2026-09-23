package com.github.kisaragimikoto.tacticalbackpack.compat.farmersdelight;

import net.minecraftforge.fml.ModList;

public class FarmersDelightIntegration {

    private static boolean loaded = false;

    public static void init() {
        loaded =
                ModList.get().isLoaded("farmersdelight") ||
                        ModList.get().isLoaded("nethersdelight") ||
                        ModList.get().isLoaded("endsdelight");
    }

    public static boolean isLoaded() {
        return loaded;
    }
}