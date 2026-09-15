package com.github.kisaragimikoto.tacticalbackpack.compat.epicfight;

import net.minecraftforge.fml.ModList;

public class EpicFightIntegration {

    private static boolean loaded = false;

    public static void init() {
        loaded =
                ModList.get().isLoaded("epicfight") ||
                        ModList.get().isLoaded("epicfightmod") ||
                        ModList.get().isLoaded("epicfight-addon");
    }

    public static boolean isLoaded() {
        return loaded;
    }
}