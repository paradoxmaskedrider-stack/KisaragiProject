package com.github.kisaragimikoto.tacticalbackpack.compat.l2;

import net.minecraftforge.fml.ModList;

public class L2Integration {

    private static boolean loaded = false;

    public static void init() {

        if (!ModList.get().isLoaded("l2library")
                && !ModList.get().isLoaded("l2hostility")
                && !ModList.get().isLoaded("l2weaponry")
                && !ModList.get().isLoaded("l2complements")
                && !ModList.get().isLoaded("l2archery")) {
            return;
        }

        loaded = true;
        registerSlots();
    }

    private static void registerSlots() {
        // L2アクセサリースロット登録
        System.out.println("[TacticalBackpack] L2 Accessories Enabled");
    }

    public static boolean isEnabled() {
        return loaded;
    }
}