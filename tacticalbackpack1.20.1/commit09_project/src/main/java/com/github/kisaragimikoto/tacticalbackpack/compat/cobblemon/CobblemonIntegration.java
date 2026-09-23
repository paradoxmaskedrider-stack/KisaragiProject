package com.github.kisaragimikoto.tacticalbackpack.compat.cobblemon;

import net.minecraftforge.fml.ModList;

public class CobblemonIntegration {

    private static boolean loaded = false;

    public static void init() {
        loaded = ModList.get().isLoaded("cobblemon");

        if (!loaded) {
            System.out.println("[TacticalBackpack] Cobblemon not loaded. Integration disabled.");
            return;
        }

        System.out.println("[TacticalBackpack] Cobblemon loaded. Integration enabled.");
    }

    public static boolean isLoaded() {
        return loaded;
    }
}