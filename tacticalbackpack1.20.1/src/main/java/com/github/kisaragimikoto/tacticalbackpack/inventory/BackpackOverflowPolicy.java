package com.github.kisaragimikoto.tacticalbackpack.inventory;

import com.github.kisaragimikoto.tacticalbackpack.config.BackpackConfig;
import net.minecraft.world.item.ItemStack;

/**
 * Central policy for deciding when the backpack should start routing items to
 * an external storage provider. Provider adapters (AE2, Mekanism, Create, ... )
 * stay separate from this policy so optional integrations remain loader-safe.
 */
public final class BackpackOverflowPolicy {

    public static boolean enabled() {
        return BackpackConfig.ENABLE_AUTO_OVERFLOW.get();
    }

    public static int keepFreeSlots() {
        return Math.max(0, Math.min(BackpackConfig.OVERFLOW_KEEP_FREE_SLOTS.get(),
                BackpackConfig.MAX_STORAGE_SLOTS.get()));
    }

    public static boolean allowAE2() {
        return enabled() && BackpackConfig.OVERFLOW_TO_AE2.get();
    }

    public static int checkIntervalTicks() {
        return Math.max(1, BackpackConfig.OVERFLOW_CHECK_INTERVAL_TICKS.get());
    }

    public static boolean skipNestedBackpacks() {
        return BackpackConfig.OVERFLOW_SKIP_NESTED_BACKPACKS.get();
    }

    public static int countFreeStorageSlots(ItemStack backpack) {
        if (backpack == null || backpack.isEmpty()) return 0;
        BackpackInventory inventory = new BackpackInventory(backpack);
        int storageSize = BackpackInventory.getStorageSize(backpack);
        int free = 0;
        for (int slot = 0; slot < storageSize; slot++) {
            if (inventory.getItem(slot).isEmpty()) free++;
        }
        return free;
    }

    public static boolean shouldOverflow(ItemStack backpack) {
        return enabled() && countFreeStorageSlots(backpack) <= keepFreeSlots();
    }

    private BackpackOverflowPolicy() { }
}
