package com.github.kisaragimikoto.tacticalbackpack.network;

import com.github.kisaragimikoto.tacticalbackpack.registry.ModItems;
import com.github.kisaragimikoto.tacticalbackpack.trade.BackpackTradeRegistry;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.List;
import java.util.function.Supplier;

public class BackpackTradeBuyPacket {

    private final int tradeIndex;

    public BackpackTradeBuyPacket(int tradeIndex) {
        this.tradeIndex = tradeIndex;
    }

    public static void encode(BackpackTradeBuyPacket msg, FriendlyByteBuf buf) {
        buf.writeInt(msg.tradeIndex);
    }

    public static BackpackTradeBuyPacket decode(FriendlyByteBuf buf) {
        return new BackpackTradeBuyPacket(buf.readInt());
    }

    public static void handle(BackpackTradeBuyPacket msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayer player = ctx.get().getSender();
            if (player == null) return;

            List<BackpackTradeRegistry.TradeEntry> trades = BackpackTradeRegistry.getAllTrades();

            if (msg.tradeIndex < 0 || msg.tradeIndex >= trades.size()) {
                player.displayClientMessage(Component.literal("Invalid trade."), true);
                return;
            }

            BackpackTradeRegistry.TradeEntry entry = trades.get(msg.tradeIndex);

            ResourceLocation itemId = ResourceLocation.tryParse(entry.itemId());
            if (itemId == null) {
                player.displayClientMessage(Component.literal("Invalid item id."), true);
                return;
            }

            Item item = ForgeRegistries.ITEMS.getValue(itemId);
            if (item == null) {
                player.displayClientMessage(Component.literal("Item not found."), true);
                return;
            }

            ItemStack result = new ItemStack(item);

            if (result.isEmpty()) {
                player.displayClientMessage(Component.literal("Cannot buy this item."), true);
                return;
            }

            if (result.is(ModItems.TRADE_COIN.get())) {
                player.displayClientMessage(Component.literal("Trade Coin cannot be bought."), true);
                return;
            }

            int price = Math.max(1, entry.price());

            if (!hasEnoughCoins(player, price)) {
                player.displayClientMessage(Component.literal("Not enough Trade Coins."), true);
                return;
            }

            consumeCoins(player, price);

            if (!player.getInventory().add(result)) {
                player.drop(result, false);
            }

            player.displayClientMessage(
                    Component.literal("Purchased: " + result.getHoverName().getString()),
                    true
            );
        });

        ctx.get().setPacketHandled(true);
    }

    private static boolean hasEnoughCoins(ServerPlayer player, int price) {
        int count = 0;

        for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
            ItemStack stack = player.getInventory().getItem(i);

            if (stack.is(ModItems.TRADE_COIN.get())) {
                count += stack.getCount();

                if (count >= price) {
                    return true;
                }
            }
        }

        return false;
    }

    private static void consumeCoins(ServerPlayer player, int price) {
        int remaining = price;

        for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
            ItemStack stack = player.getInventory().getItem(i);

            if (!stack.is(ModItems.TRADE_COIN.get())) {
                continue;
            }

            int take = Math.min(remaining, stack.getCount());
            stack.shrink(take);
            remaining -= take;

            if (remaining <= 0) {
                return;
            }
        }
    }
}