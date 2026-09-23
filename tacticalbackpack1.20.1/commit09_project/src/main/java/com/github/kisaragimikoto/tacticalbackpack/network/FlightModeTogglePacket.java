package com.github.kisaragimikoto.tacticalbackpack.network;

import com.github.kisaragimikoto.tacticalbackpack.item.TacticalBackpackItem;
import com.github.kisaragimikoto.tacticalbackpack.trade.FlightModeUtil;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class FlightModeTogglePacket {

    public FlightModeTogglePacket() {
    }

    public static void encode(FlightModeTogglePacket msg, FriendlyByteBuf buf) {
    }

    public static FlightModeTogglePacket decode(FriendlyByteBuf buf) {
        return new FlightModeTogglePacket();
    }

    public static void handle(FlightModeTogglePacket msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayer player = ctx.get().getSender();
            if (player == null) return;

            ItemStack backpack = TacticalBackpackItem.getEquippedBackpack(player);
            if (backpack.isEmpty()) return;

            FlightModeUtil.toggle(backpack);

            player.displayClientMessage(
                    Component.literal("Flight Mode: " + FlightModeUtil.getMode(backpack)),
                    true
            );
        });

        ctx.get().setPacketHandled(true);
    }
}