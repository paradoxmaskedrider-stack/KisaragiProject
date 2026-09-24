package com.github.kisaragimikoto.tacticalbackpack.compat.ae2;

import appeng.api.config.Actionable;
import appeng.api.networking.security.IActionSource;
import appeng.api.networking.storage.IStorageService;
import appeng.api.stacks.AEItemKey;
import appeng.api.storage.MEStorage;
import appeng.api.stacks.AEKey;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * AE2 15.x backed storage adapter.
 * This class is only loaded when Applied Energistics 2 is present.
 */
final class AE2ApiStorageAccess implements AE2StorageAccess {
    private final IStorageService storageService;
    private final MEStorage storage;
    private final IActionSource source;
    private final boolean online;

    AE2ApiStorageAccess(IStorageService storageService, ServerPlayer player, boolean online) {
        this.storageService = storageService;
        this.storage = storageService == null ? null : storageService.getInventory();
        this.source = IActionSource.ofPlayer(player);
        this.online = online && this.storage != null;
    }

    @Override
    public boolean isOnline() {
        return online;
    }

    @Override
    public int insert(ItemStack stack, boolean simulate) {
        if (!online || stack == null || stack.isEmpty()) return 0;
        AEItemKey key = AEItemKey.of(stack);
        if (key == null) return 0;

        long inserted = storage.insert(
                key,
                stack.getCount(),
                Actionable.ofSimulate(simulate),
                source
        );
        return clampToInt(inserted, stack.getCount());
    }

    @Override
    public ItemStack extract(ItemStack template, int amount, boolean simulate) {
        if (!online || template == null || template.isEmpty() || amount <= 0) {
            return ItemStack.EMPTY;
        }
        AEItemKey key = AEItemKey.of(template);
        if (key == null) return ItemStack.EMPTY;

        long extracted = storage.extract(
                key,
                amount,
                Actionable.ofSimulate(simulate),
                source
        );
        if (extracted <= 0) return ItemStack.EMPTY;
        return key.toStack(clampToInt(extracted, amount));
    }

    @Override
    public List<AE2ItemEntry> getAvailableItems() {
        if (!online) return List.of();

        List<AE2ItemEntry> result = new ArrayList<>();
        for (var entry : storageService.getCachedInventory()) {
            AEKey rawKey = entry.getKey();
            long amount = entry.getLongValue();
            if (!(rawKey instanceof AEItemKey key) || amount <= 0) continue;
            ItemStack stack = key.toStack(1);
            if (!stack.isEmpty()) result.add(new AE2ItemEntry(stack, amount));
        }
        result.sort(Comparator.comparing(e -> e.stack().getHoverName().getString(), String.CASE_INSENSITIVE_ORDER));
        return result;
    }

    private static int clampToInt(long value, int requested) {
        if (value <= 0) return 0;
        return (int) Math.min(Math.min(value, requested), Integer.MAX_VALUE);
    }
}
