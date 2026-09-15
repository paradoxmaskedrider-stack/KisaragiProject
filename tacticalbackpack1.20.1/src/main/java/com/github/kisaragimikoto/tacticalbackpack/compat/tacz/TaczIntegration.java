package com.github.kisaragimikoto.tacticalbackpack.compat.tacz;

import com.github.kisaragimikoto.tacticalbackpack.util.ModChecker;

public class TaczIntegration {

    private static boolean loaded = false;

    public static void init() {
        loaded = ModChecker.isTaczLoaded();

        if (!loaded) {
            System.out.println("[TacticalBackpack] TaCZ not loaded. Integration disabled.");
            return;
        }

        System.out.println("[TacticalBackpack] TaCZ loaded. Integration enabled.");
    }

    public static boolean isLoaded() {
        return loaded;
    }
}