package com.github.kisaragimikoto.tacticalbackpack.network;

import com.github.kisaragimikoto.tacticalbackpack.item.BackpackZoneControllerItem;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;

import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class BackpackZoneSizePacket {

    public BackpackZoneSizePacket() {
    }

    public static void encode(BackpackZoneSizePacket msg, FriendlyByteBuf buf) {
    }

    public static BackpackZoneSizePacket decode(FriendlyByteBuf buf) {
        return new BackpackZoneSizePacket();
    }

    public static void handle(BackpackZoneSizePacket msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayer player = ctx.get().getSender();
            if (player == null) return;

            ItemStack mainHand = player.getItemInHand(InteractionHand.MAIN_HAND);
            ItemStack offHand = player.getItemInHand(InteractionHand.OFF_HAND);

            if (mainHand.getItem() instanceof BackpackZoneControllerItem) {
                BackpackZoneControllerItem.switchZoneSize(mainHand, player);
                return;
            }

            if (offHand.getItem() instanceof BackpackZoneControllerItem) {
                BackpackZoneControllerItem.switchZoneSize(offHand, player);
            }
        });

        ctx.get().setPacketHandled(true);
    }
}