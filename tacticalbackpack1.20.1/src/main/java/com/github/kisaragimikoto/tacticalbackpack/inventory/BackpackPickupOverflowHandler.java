package com.github.kisaragimikoto.tacticalbackpack.inventory;

import com.github.kisaragimikoto.tacticalbackpack.TacticalBackpack;
import com.github.kisaragimikoto.tacticalbackpack.compat.storage.ExternalStorageRouter;
import com.github.kisaragimikoto.tacticalbackpack.config.BackpackConfig;
import com.github.kisaragimikoto.tacticalbackpack.item.TacticalBackpackItem;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.entity.player.EntityItemPickupEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * Routes only pickup overflow. Vanilla inventory pickup always keeps priority.
 * When the normal player inventory has no room, the remainder is sent through
 * carried TacticalBackpacks and finally through their linked external storage.
 */
@Mod.EventBusSubscriber(modid = TacticalBackpack.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class BackpackPickupOverflowHandler {

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onItemPickup(EntityItemPickupEvent event) {
        if (!BackpackConfig.ENABLE_PICKUP_OVERFLOW.get()) return;
        if (!(event.getEntity() instanceof ServerPlayer player)) return;

        ItemEntity itemEntity = event.getItem();
        ItemStack worldStack = itemEntity.getItem();
        if (worldStack.isEmpty()) return;

        // Let Minecraft perform normal pickup while even one item can still fit.
        // If a stack only partly fits, vanilla leaves the remainder in the entity;
        // that remainder will be routed on the next pickup pass.
        if (hasPlayerInventoryRoom(player, worldStack)) return;

        ItemStack remaining = worldStack.copy();
        int original = remaining.getCount();

        // Nested packs are deliberately left on the ground when configured so.
        if (remaining.getItem() instanceof TacticalBackpackItem
                && BackpackConfig.OVERFLOW_SKIP_NESTED_BACKPACKS.get()) {
            return;
        }

        for (ItemStack backpack : player.getInventory().items) {
            remaining = routeThroughBackpack(player, backpack, remaining);
            if (remaining.isEmpty()) break;
        }
        if (!remaining.isEmpty()) {
            for (ItemStack backpack : player.getInventory().offhand) {
                remaining = routeThroughBackpack(player, backpack, remaining);
                if (remaining.isEmpty()) break;
            }
        }

        int moved = original - remaining.getCount();
        if (moved <= 0) return;

        // We handled only the overflow part, so prevent vanilla from processing the
        // same entity again during this touch. Any unhandled remainder stays in-world.
        event.setCanceled(true);
        if (remaining.isEmpty()) {
            itemEntity.discard();
        } else {
            itemEntity.setItem(remaining);
        }
    }

    private static ItemStack routeThroughBackpack(ServerPlayer player, ItemStack backpack, ItemStack input) {
        if (input.isEmpty() || backpack.isEmpty() || !(backpack.getItem() instanceof TacticalBackpackItem)) {
            return input;
        }

        if (BackpackConfig.PICKUP_OVERFLOW_REQUIRES_AUTO_PICKUP_MODULE.get()
                && !TacticalBackpackItem.hasAutoPickupModule(backpack)) {
            return input;
        }

        ItemStack remaining = BackpackInsertionService.insert(backpack, input);
        if (remaining.isEmpty()) {
            // Keep the configured reserve healthy immediately instead of waiting for
            // the periodic overflow tick.
            if (BackpackOverflowPolicy.shouldOverflow(backpack)) {
                ExternalStorageRouter.maintainFreeSlotReserve(player, backpack);
            }
            return ItemStack.EMPTY;
        }

        // The backpack itself is full. Use the same linked AE2 network as a direct
        // overflow sink when enabled. Anything AE2 cannot accept stays on the ground.
        if (BackpackConfig.PICKUP_OVERFLOW_TO_EXTERNAL_STORAGE.get()) {
            return ExternalStorageRouter.insert(player, backpack, remaining);
        }

        return remaining;
    }

    private static boolean hasPlayerInventoryRoom(ServerPlayer player, ItemStack stack) {
        for (ItemStack existing : player.getInventory().items) {
            if (existing.isEmpty()) return true;
            if (ItemStack.isSameItemSameTags(existing, stack)
                    && existing.getCount() < Math.min(existing.getMaxStackSize(), player.getInventory().getMaxStackSize())) {
                return true;
            }
        }
        return false;
    }

    private BackpackPickupOverflowHandler() { }
}
