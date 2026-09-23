package com.github.kisaragimikoto.tacticalbackpack.compat.ae2;

import appeng.api.config.Actionable;
import appeng.api.networking.security.IActionSource;
import appeng.api.stacks.AEItemKey;
import appeng.api.storage.MEStorage;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

/**
 * AE2 15.x backed storage adapter.
 * This class is only loaded when Applied Energistics 2 is present.
 */
final class AE2ApiStorageAccess implements AE2StorageAccess {
    private final MEStorage storage;
    private final IActionSource source;
    private final boolean online;

    AE2ApiStorageAccess(MEStorage storage, ServerPlayer player, boolean online) {
        this.storage = storage;
        this.source = IActionSource.ofPlayer(player);
        this.online = online && storage != null;
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

    private static int clampToInt(long value, int requested) {
        if (value <= 0) return 0;
        return (int) Math.min(Math.min(value, requested), Integer.MAX_VALUE);
    }
}
