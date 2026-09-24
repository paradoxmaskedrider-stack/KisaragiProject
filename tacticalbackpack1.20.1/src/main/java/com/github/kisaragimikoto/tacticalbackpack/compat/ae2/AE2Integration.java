package com.github.kisaragimikoto.tacticalbackpack.compat.ae2;

import com.github.kisaragimikoto.tacticalbackpack.config.BackpackConfig;
import com.github.kisaragimikoto.tacticalbackpack.util.ModChecker;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

import java.util.List;
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
            return;
        }
        installApiBridge();
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

    /** Snapshot of item variants exposed by the linked ME network. */
    public static List<AE2ItemEntry> getAvailableItems(ServerPlayer player, ItemStack backpack) {
        AE2StorageAccess storage = resolveStorage(player, backpack);
        return storage.isOnline() ? storage.getAvailableItems() : List.of();
    }

    /** Push normal backpack storage slots into the linked ME network. */
    public static AE2BackpackTransferService.TransferResult pushAllToME(ServerPlayer player, ItemStack backpack) {
        AE2StorageAccess storage = resolveStorage(player, backpack);
        return AE2BackpackTransferService.pushAll(
                backpack,
                storage,
                BackpackConfig.AE2_TRANSFER_LIMIT.get()
        );
    }

    /** Pull a requested item type from ME into normal backpack storage slots. */
    public static int pullFromME(ServerPlayer player, ItemStack backpack, ItemStack template, int amount) {
        int limit = BackpackConfig.AE2_TRANSFER_LIMIT.get();
        int requested = limit <= 0 ? amount : Math.min(amount, limit);
        return AE2BackpackTransferService.pull(backpack, resolveStorage(player, backpack), template, requested);
    }

    /** Open/refresh a short-lived wireless session for the currently linked network. */
    public static boolean openWirelessSession(ServerPlayer player, ItemStack backpack) {
        Optional<AE2LinkData> link = getLink(backpack);
        if (!enabled || link.isEmpty() || !resolveStorage(player, backpack).isOnline()) return false;
        AE2WirelessSessionManager.open(player, link.get(), BackpackConfig.AE2_WIRELESS_SESSION_TICKS.get());
        return true;
    }

    public static void closeWirelessSession(ServerPlayer player) {
        AE2WirelessSessionManager.close(player);
    }

    private static void installApiBridge() {
        try {
            Class<?> installer = Class.forName(
                    "com.github.kisaragimikoto.tacticalbackpack.compat.ae2.AE2ApiBridgeInstaller"
            );
            installer.getMethod("install").invoke(null);
        } catch (ReflectiveOperationException | LinkageError ex) {
            AE2BridgeRegistry.reset();
            enabled = false;
        }
    }

    private AE2Integration() { }
}
