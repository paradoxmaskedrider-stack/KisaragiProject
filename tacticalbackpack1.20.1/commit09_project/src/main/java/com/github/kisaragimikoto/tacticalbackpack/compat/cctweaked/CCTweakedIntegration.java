package com.github.kisaragimikoto.tacticalbackpack.compat.cctweaked;

import net.minecraftforge.fml.ModList;

public class CCTweakedIntegration {

    private static boolean loaded = false;

    public static void init() {
        loaded = ModList.get().isLoaded("computercraft");
    }

    public static boolean isLoaded() {
        return loaded;
    }
}