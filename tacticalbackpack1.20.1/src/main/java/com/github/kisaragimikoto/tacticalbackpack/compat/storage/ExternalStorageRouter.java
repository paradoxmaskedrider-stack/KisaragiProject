package com.github.kisaragimikoto.tacticalbackpack.compat.storage;

import com.github.kisaragimikoto.tacticalbackpack.compat.ae2.AE2ExternalStorageProvider;
import com.github.kisaragimikoto.tacticalbackpack.inventory.BackpackInventory;
import com.github.kisaragimikoto.tacticalbackpack.inventory.BackpackOverflowPolicy;
import com.github.kisaragimikoto.tacticalbackpack.item.TacticalBackpackItem;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/** Central ordered router for all optional external storage backends. */
public final class ExternalStorageRouter {
    private static final List<ExternalStorageProvider> PROVIDERS = new ArrayList<>();
    private static boolean defaultsInstalled;

    public static synchronized void installDefaults() {
        if (defaultsInstalled) return;
        register(new AE2ExternalStorageProvider());
        register(new ForgeItemHandlerStorageProvider());
        defaultsInstalled = true;
    }

    public static synchronized void register(ExternalStorageProvider provider) {
        if (provider == null) return;
        PROVIDERS.removeIf(existing -> existing.id().equals(provider.id()));
        PROVIDERS.add(provider);
        PROVIDERS.sort(Comparator.comparingInt(ExternalStorageProvider::priority));
    }

    public static ItemStack insert(ServerPlayer player, ItemStack backpack, ItemStack input) {
        installDefaults();
        if (input == null || input.isEmpty()) return ItemStack.EMPTY;

        ItemStack remaining = input.copy();
        for (ExternalStorageProvider provider : snapshot()) {
            if (remaining.isEmpty()) break;
            try {
                if (!provider.isAvailable(player, backpack)) continue;
                remaining = provider.insert(player, backpack, remaining);
                if (remaining == null) remaining = ItemStack.EMPTY;
            } catch (RuntimeException | LinkageError ignored) {
                // Optional integrations must never make normal inventory handling fatal.
            }
        }
        return remaining;
    }

    /** Frees complete backpack slots until the configured reserve is satisfied. */
    public static int maintainFreeSlotReserve(ServerPlayer player, ItemStack backpack) {
        installDefaults();
        if (player == null || backpack == null || backpack.isEmpty()
                || !(backpack.getItem() instanceof TacticalBackpackItem)
                || !BackpackOverflowPolicy.shouldOverflow(backpack)) {
            return 0;
        }

        BackpackInventory inv = TacticalBackpackItem.getInventory(backpack);
        int storageSize = BackpackInventory.getStorageSize(backpack);
        int targetFree = Math.min(storageSize, BackpackOverflowPolicy.keepFreeSlots());
        int free = countFree(inv, storageSize);
        int moved = 0;

        for (int slot = storageSize - 1; slot >= 0 && free < targetFree; slot--) {
            ItemStack current = inv.getItem(slot);
            if (current.isEmpty()) continue;
            if (BackpackOverflowPolicy.skipNestedBackpacks()
                    && current.getItem() instanceof TacticalBackpackItem) {
                continue;
            }

            ItemStack remaining = insert(player, backpack, current);
            int accepted = current.getCount() - remaining.getCount();
            if (accepted <= 0) continue;

            inv.setItem(slot, remaining);
            moved += accepted;
            if (remaining.isEmpty()) free++;
        }

        if (moved > 0) TacticalBackpackItem.saveInventory(backpack, inv);
        return moved;
    }

    public static boolean hasAvailableProvider(ServerPlayer player, ItemStack backpack) {
        installDefaults();
        for (ExternalStorageProvider provider : snapshot()) {
            try {
                if (provider.isAvailable(player, backpack)) return true;
            } catch (RuntimeException | LinkageError ignored) { }
        }
        return false;
    }

    private static int countFree(BackpackInventory inv, int storageSize) {
        int free = 0;
        for (int i = 0; i < storageSize; i++) if (inv.getItem(i).isEmpty()) free++;
        return free;
    }

    private static synchronized List<ExternalStorageProvider> snapshot() {
        return List.copyOf(PROVIDERS);
    }

    private ExternalStorageRouter() { }
}
