package com.github.kisaragimikoto.tacticalbackpack.compat.tconstruct;

import net.minecraftforge.fml.ModList;

public class TinkersIntegration {

    private static boolean loaded = false;

    public static void init() {
        loaded =
                ModList.get().isLoaded("tconstruct") ||
                        ModList.get().isLoaded("ticex");
    }

    public static boolean isLoaded() {
        return loaded;
    }
}