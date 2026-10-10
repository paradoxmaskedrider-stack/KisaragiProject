package com.github.kisaragimikoto.tacticalbackpack.compat.storage;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

/** Loader-safe external storage sink used by backpack overflow routing. */
public interface ExternalStorageProvider {
    String id();

    int priority();

    /** True when this provider is enabled and the backpack has a usable link. */
    boolean isAvailable(ServerPlayer player, ItemStack backpack);

    /**
     * Inserts as much as possible and returns the unaccepted remainder.
     * Implementations must never mutate the supplied input instance.
     */
    ItemStack insert(ServerPlayer player, ItemStack backpack, ItemStack input);
}
