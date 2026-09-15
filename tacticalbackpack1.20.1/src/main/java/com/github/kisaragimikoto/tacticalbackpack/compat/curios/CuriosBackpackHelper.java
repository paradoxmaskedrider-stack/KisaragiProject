package com.github.kisaragimikoto.tacticalbackpack.compat.curios;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.fml.ModList;

public class CuriosBackpackHelper {

    public static boolean isBackpackEquipped(Player player) {

        if (!ModList.get().isLoaded("curios")) {
            return false;
        }

        // TODO Curios API接続予定
        return false;
    }
}