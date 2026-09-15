package com.github.kisaragimikoto.tacticalbackpack.compat.curios;

import net.minecraft.world.entity.player.Player;

public class CuriosHelper {

    public static boolean isBackpackEquipped(Player player) {
        if (!CuriosIntegration.isLoaded()) return false;

        // Curios API呼び出し予定箇所
        return false;
    }
}