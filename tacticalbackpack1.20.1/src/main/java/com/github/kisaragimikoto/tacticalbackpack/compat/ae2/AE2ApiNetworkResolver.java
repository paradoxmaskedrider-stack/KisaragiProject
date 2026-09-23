package com.github.kisaragimikoto.tacticalbackpack.compat.ae2;

import appeng.api.networking.IGridNode;
import appeng.api.networking.security.IActionHost;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;

/** Resolves TacticalBackpack link metadata to an AE2 15.x live grid. */
final class AE2ApiNetworkResolver implements AE2NetworkResolver {

    @Override
    public AE2StorageAccess resolve(ServerPlayer player, AE2LinkData linkData) {
        if (player == null || linkData == null) return AE2StorageAccess.OFFLINE;

        ResourceLocation dimensionId = linkData.dimension().orElse(null);
        if (dimensionId == null) return AE2StorageAccess.OFFLINE;

        ResourceKey<Level> dimensionKey = ResourceKey.create(Registries.DIMENSION, dimensionId);
        ServerLevel level = player.server.getLevel(dimensionKey);
        if (level == null || !level.hasChunkAt(linkData.pos())) {
            return AE2StorageAccess.OFFLINE;
        }

        BlockEntity blockEntity = level.getBlockEntity(linkData.pos());
        if (!(blockEntity instanceof IActionHost actionHost)) {
            return AE2StorageAccess.OFFLINE;
        }

        IGridNode node = actionHost.getActionableNode();
        if (node == null || !node.isActive()) {
            return AE2StorageAccess.OFFLINE;
        }

        var storageService = node.getGrid().getStorageService();
        if (storageService == null || storageService.getInventory() == null) {
            return AE2StorageAccess.OFFLINE;
        }

        return new AE2ApiStorageAccess(storageService.getInventory(), player, true);
    }
}
