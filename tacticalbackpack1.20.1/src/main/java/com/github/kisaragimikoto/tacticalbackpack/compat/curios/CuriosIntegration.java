package com.github.kisaragimikoto.tacticalbackpack.compat.curios;

import com.github.kisaragimikoto.tacticalbackpack.util.ModChecker;

public class CuriosIntegration {

    private static boolean loaded = false;

    public static void init() {
        loaded = ModChecker.isCuriosLoaded();

        if (!loaded) {
            System.out.println("[TacticalBackpack] Curios not loaded. Integration disabled.");
            return;
        }

        System.out.println("[TacticalBackpack] Curios loaded. Integration enabled.");
    }

    public static boolean isLoaded() {
        return loaded;
    }
}