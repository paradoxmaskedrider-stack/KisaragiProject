package com.github.kisaragimikoto.tacticalbackpack.compat.tacz;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public class TaczWorkbenchBridge {

    public static boolean canOpen(Player player, ItemStack backpack) {
        if (!TaczIntegration.isLoaded()) {
            return false;
        }

        if (backpack.isEmpty()) {
            return false;
        }

        // TODO: 後でTaCZ作業台モジュール判定を追加
        return true;
    }
}