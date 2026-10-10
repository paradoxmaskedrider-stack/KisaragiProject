package com.github.kisaragimikoto.tacticalbackpack.compat.ae2;

import com.github.kisaragimikoto.tacticalbackpack.TacticalBackpack;
import com.github.kisaragimikoto.tacticalbackpack.inventory.BackpackOverflowPolicy;
import com.github.kisaragimikoto.tacticalbackpack.compat.storage.ExternalStorageRouter;
import com.github.kisaragimikoto.tacticalbackpack.item.TacticalBackpackItem;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/** Server-side periodic automatic overflow for carried, AE2-linked backpacks. */
@Mod.EventBusSubscriber(modid = TacticalBackpack.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class AE2OverflowHandler {

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END || event.player.level().isClientSide) return;
        if (!(event.player instanceof ServerPlayer player)) return;
        if (!BackpackOverflowPolicy.enabled()) return;

        int interval = BackpackOverflowPolicy.checkIntervalTicks();
        if (player.tickCount % interval != 0) return;

        // Main inventory includes the hotbar. Process every carried backpack so
        // separately linked packs can maintain their own free-slot reserve.
        for (ItemStack stack : player.getInventory().items) {
            process(player, stack);
        }
        for (ItemStack stack : player.getInventory().offhand) {
            process(player, stack);
        }
    }

    private static void process(ServerPlayer player, ItemStack stack) {
        if (stack.isEmpty() || !(stack.getItem() instanceof TacticalBackpackItem)) return;
        if (!BackpackOverflowPolicy.shouldOverflow(stack)) return;
        ExternalStorageRouter.maintainFreeSlotReserve(player, stack);
    }

    private AE2OverflowHandler() { }
}
