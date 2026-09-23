package com.github.kisaragimikoto.tacticalbackpack.network;

import com.github.kisaragimikoto.tacticalbackpack.world.BackpackWorldTeleporter;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;

import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class ReturnFromBackpackWorldPacket {

    public ReturnFromBackpackWorldPacket() {
    }

    public static void encode(ReturnFromBackpackWorldPacket msg, FriendlyByteBuf buf) {
    }

    public static ReturnFromBackpackWorldPacket decode(FriendlyByteBuf buf) {
        return new ReturnFromBackpackWorldPacket();
    }

    public static void handle(ReturnFromBackpackWorldPacket msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayer player = ctx.get().getSender();
            if (player == null) return;

            BackpackWorldTeleporter.returnBack(player);
        });

        ctx.get().setPacketHandled(true);
    }
}