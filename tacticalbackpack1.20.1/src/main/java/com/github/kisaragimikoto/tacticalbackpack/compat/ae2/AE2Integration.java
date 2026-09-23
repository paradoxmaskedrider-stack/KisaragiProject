package com.github.kisaragimikoto.tacticalbackpack.compat.ae2;

import com.github.kisaragimikoto.tacticalbackpack.config.BackpackConfig;
import com.github.kisaragimikoto.tacticalbackpack.util.ModChecker;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

import java.util.Optional;

/** Optional Applied Energistics 2 integration entry point. */
public final class AE2Integration {
    private static boolean loaded;
    private static boolean enabled;

    public static void init() {
        loaded = ModChecker.isAE2Loaded();
        enabled = loaded && BackpackConfig.ENABLE_AE2.get();
        if (!enabled) {
            AE2BridgeRegistry.reset();
        }
    }

    public static boolean isLoaded() {
        return loaded;
    }

    public static boolean isEnabled() {
        return enabled;
    }

    public static AE2LinkData link(ItemStack backpack, ResourceLocation dimension, BlockPos pos) {
        AE2LinkData data = AE2LinkData.create(dimension, pos);
        data.save(backpack);
        return data;
    }

    public static void unlink(ItemStack backpack) {
        AE2LinkData.clear(backpack);
    }

    public static Optional<AE2LinkData> getLink(ItemStack backpack) {
        return AE2LinkData.load(backpack);
    }

    public static boolean isLinked(ItemStack backpack) {
        return getLink(backpack).isPresent();
    }

    public static AE2StorageAccess resolveStorage(ServerPlayer player, ItemStack backpack) {
        if (!enabled) return AE2StorageAccess.OFFLINE;
        return getLink(backpack)
                .map(link -> AE2BridgeRegistry.resolve(player, link))
                .orElse(AE2StorageAccess.OFFLINE);
    }

    private AE2Integration() { }
}
