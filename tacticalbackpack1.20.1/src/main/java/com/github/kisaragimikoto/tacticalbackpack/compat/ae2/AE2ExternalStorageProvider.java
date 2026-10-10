package com.github.kisaragimikoto.tacticalbackpack.compat.ae2;

import com.github.kisaragimikoto.tacticalbackpack.compat.storage.ExternalStorageProvider;
import com.github.kisaragimikoto.tacticalbackpack.config.BackpackConfig;
import com.github.kisaragimikoto.tacticalbackpack.inventory.BackpackOverflowPolicy;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

/** External-storage adapter that exposes the existing AE2 link to the common router. */
public final class AE2ExternalStorageProvider implements ExternalStorageProvider {
    @Override
    public String id() {
        return "ae2";
    }

    @Override
    public int priority() {
        return 100;
    }

    @Override
    public boolean isAvailable(ServerPlayer player, ItemStack backpack) {
        return player != null
                && BackpackOverflowPolicy.allowAE2()
                && AE2Integration.isEnabled()
                && AE2Integration.isLinked(backpack)
                && AE2Integration.resolveStorage(player, backpack).isOnline();
    }

    @Override
    public ItemStack insert(ServerPlayer player, ItemStack backpack, ItemStack input) {
        return AE2BackpackTransferService.insertExternal(
                AE2Integration.resolveStorage(player, backpack),
                input,
                BackpackConfig.AE2_TRANSFER_LIMIT.get()
        );
    }
}
