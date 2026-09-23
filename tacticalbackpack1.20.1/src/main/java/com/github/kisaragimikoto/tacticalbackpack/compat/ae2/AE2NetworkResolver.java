package com.github.kisaragimikoto.tacticalbackpack.compat.ae2;

import net.minecraft.server.level.ServerPlayer;

/** Resolves a saved backpack link to a live AE2 storage endpoint. */
@FunctionalInterface
public interface AE2NetworkResolver {
    AE2StorageAccess resolve(ServerPlayer player, AE2LinkData linkData);
}
