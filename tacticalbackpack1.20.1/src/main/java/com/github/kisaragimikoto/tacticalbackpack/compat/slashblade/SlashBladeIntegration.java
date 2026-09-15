package com.github.kisaragimikoto.tacticalbackpack.compat.slashblade;

import net.minecraftforge.fml.ModList;

public class SlashBladeIntegration {

    private static boolean loaded = false;

    public static void init() {

        if (!ModList.get().isLoaded("slashblade")
                && !ModList.get().isLoaded("slashbladeresharped")
                && !ModList.get().isLoaded("slashblade_addon")) {
            return;
        }

        loaded = true;
        registerBladeSlots();
    }

    private static void registerBladeSlots() {
        System.out.println("[TacticalBackpack] SlashBlade Integration Enabled");
    }

    public static boolean isEnabled() {
        return loaded;
    }
}