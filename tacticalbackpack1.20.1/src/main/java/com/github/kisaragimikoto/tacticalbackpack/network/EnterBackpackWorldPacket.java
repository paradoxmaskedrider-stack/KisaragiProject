package com.github.kisaragimikoto.tacticalbackpack.network;

import com.github.kisaragimikoto.tacticalbackpack.item.TacticalBackpackItem;
import com.github.kisaragimikoto.tacticalbackpack.world.BackpackWorldTeleporter;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class EnterBackpackWorldPacket {

    public EnterBackpackWorldPacket() {
    }

    public static void encode(EnterBackpackWorldPacket msg, FriendlyByteBuf buf) {
    }

    public static EnterBackpackWorldPacket decode(FriendlyByteBuf buf) {
        return new EnterBackpackWorldPacket();
    }

    public static void handle(EnterBackpackWorldPacket msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayer player = ctx.get().getSender();
            if (player == null) return;

            ItemStack backpack = TacticalBackpackItem.getEquippedBackpack(player);
            if (backpack.isEmpty()) return;

            BackpackWorldTeleporter.enter(player);
        });

        ctx.get().setPacketHandled(true);
    }
}