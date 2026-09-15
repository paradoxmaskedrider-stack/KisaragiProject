package com.github.kisaragimikoto.tacticalbackpack.network;

import com.github.kisaragimikoto.tacticalbackpack.registry.ModItems;
import com.github.kisaragimikoto.tacticalbackpack.trade.BackpackTradePriceUtil;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class BackpackTradeSellPacket {

    public BackpackTradeSellPacket() {
    }

    public static void encode(BackpackTradeSellPacket msg, FriendlyByteBuf buf) {
    }

    public static BackpackTradeSellPacket decode(FriendlyByteBuf buf) {
        return new BackpackTradeSellPacket();
    }

    public static void handle(BackpackTradeSellPacket msg, Supplier<NetworkEvent.Context> ctx) {

        ctx.get().enqueueWork(() -> {

            ServerPlayer player = ctx.get().getSender();

            if (player == null) {
                return;
            }

            ItemStack held = player.getMainHandItem();

            if (held.isEmpty()) {
                player.displayClientMessage(
                        Component.literal("Hold an item to sell."),
                        true
                );
                return;
            }

            // ===== 売却禁止 =====

            if (held.is(ModItems.TACTICAL_BACKPACK.get())
                    || held.is(ModItems.TRADE_COIN.get())
                    || held.is(ModItems.BACKPACK_RETURN_DEVICE.get())
                    || held.is(ModItems.BACKPACK_ZONE_CONTROLLER.get())
                    || held.is(ModItems.BACKPACK_ZONE_REMOVER.get())
                    || held.is(ModItems.BACKPACK_TRADE_TERMINAL.get())) {

                player.displayClientMessage(
                        Component.literal("This item cannot be sold."),
                        true
                );

                return;
            }

            int price = Math.max(
                    1,
                    BackpackTradePriceUtil.getPrice(held.getItem()) / 2
            );

            held.shrink(1);

            ItemStack coins = new ItemStack(
                    ModItems.TRADE_COIN.get(),
                    price
            );

            if (!player.getInventory().add(coins)) {
                player.drop(coins, false);
            }

            player.displayClientMessage(
                    Component.literal(
                            "Sold item for "
                                    + price
                                    + " Trade Coins."
                    ),
                    true
            );
        });

        ctx.get().setPacketHandled(true);
    }
}