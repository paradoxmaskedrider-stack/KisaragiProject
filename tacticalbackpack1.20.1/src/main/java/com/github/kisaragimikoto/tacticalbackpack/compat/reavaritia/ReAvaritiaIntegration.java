package com.github.kisaragimikoto.tacticalbackpack.compat.reavaritia;

import net.minecraftforge.fml.ModList;

public class ReAvaritiaIntegration {

    private static boolean loaded = false;

    public static void init() {
        loaded =
                ModList.get().isLoaded("reavaritia") ||
                        ModList.get().isLoaded("avaritia");
    }

    public static boolean isLoaded() {
        return loaded;
    }
}