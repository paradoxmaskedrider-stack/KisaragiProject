package com.github.kisaragimikoto.tacticalbackpack.network;

import com.github.kisaragimikoto.tacticalbackpack.menu.BackpackTradeMenu;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.inventory.AbstractContainerMenu;

import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class BackpackTradeSearchPacket {

    private final String searchText;

    public BackpackTradeSearchPacket(String searchText) {
        this.searchText = searchText;
    }

    public static void encode(BackpackTradeSearchPacket msg, FriendlyByteBuf buf) {
        buf.writeUtf(msg.searchText, 64);
    }

    public static BackpackTradeSearchPacket decode(FriendlyByteBuf buf) {
        return new BackpackTradeSearchPacket(buf.readUtf(64));
    }

    public static void handle(BackpackTradeSearchPacket msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayer player = ctx.get().getSender();
            if (player == null) return;

            AbstractContainerMenu menu = player.containerMenu;

            if (menu instanceof BackpackTradeMenu tradeMenu) {
                tradeMenu.setSearchText(msg.searchText);
            }
        });

        ctx.get().setPacketHandled(true);
    }
}