package com.github.kisaragimikoto.tacticalbackpack.compat.ae2;

import net.minecraft.server.level.ServerPlayer;

import java.util.Objects;

/**
 * Keeps AE2 API implementation classes out of the core class-loading path.
 */
public final class AE2BridgeRegistry {
    private static volatile AE2NetworkResolver resolver = (player, link) -> AE2StorageAccess.OFFLINE;

    public static void install(AE2NetworkResolver newResolver) {
        resolver = Objects.requireNonNull(newResolver, "newResolver");
    }

    public static void reset() {
        resolver = (player, link) -> AE2StorageAccess.OFFLINE;
    }

    public static AE2StorageAccess resolve(ServerPlayer player, AE2LinkData link) {
        if (player == null || link == null) return AE2StorageAccess.OFFLINE;
        try {
            AE2StorageAccess access = resolver.resolve(player, link);
            return access == null ? AE2StorageAccess.OFFLINE : access;
        } catch (LinkageError | RuntimeException ignored) {
            return AE2StorageAccess.OFFLINE;
        }
    }

    private AE2BridgeRegistry() { }
}
