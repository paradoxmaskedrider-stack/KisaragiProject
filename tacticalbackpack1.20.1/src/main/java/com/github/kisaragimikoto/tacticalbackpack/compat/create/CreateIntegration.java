package com.github.kisaragimikoto.tacticalbackpack.compat.create;

import net.minecraftforge.fml.ModList;

public class CreateIntegration {

    private static boolean loaded = false;

    public static void init() {
        loaded =
                ModList.get().isLoaded("create") ||
                        ModList.get().isLoaded("createaddition") ||
                        ModList.get().isLoaded("createdeco");
    }

    public static boolean isLoaded() {
        return loaded;
    }
}