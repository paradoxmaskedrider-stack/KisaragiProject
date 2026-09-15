package com.github.kisaragimikoto.tacticalbackpack.network;

import com.github.kisaragimikoto.tacticalbackpack.item.TacticalBackpackItem;
import com.github.kisaragimikoto.tacticalbackpack.registry.ModSounds;
import com.github.kisaragimikoto.tacticalbackpack.trade.FlightModeUtil;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.item.ItemStack;

import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class JetpackTogglePacket {

    public JetpackTogglePacket() {
    }

    public static void encode(JetpackTogglePacket msg, FriendlyByteBuf buf) {
    }

    public static JetpackTogglePacket decode(FriendlyByteBuf buf) {
        return new JetpackTogglePacket();
    }

    public static void handle(JetpackTogglePacket msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayer player = ctx.get().getSender();
            if (player == null) return;

            ItemStack backpack = TacticalBackpackItem.getEquippedBackpack(player);
            if (backpack.isEmpty()) return;

            FlightModeUtil.toggleJetpack(backpack);

            boolean enabled = FlightModeUtil.isJetpackEnabled(backpack);

            player.level().playSound(
                    null,
                    player.blockPosition(),
                    enabled ? ModSounds.JETPACK_START.get() : ModSounds.JETPACK_STOP.get(),
                    SoundSource.PLAYERS,
                    1.0F,
                    enabled ? 1.0F : 0.8F
            );
        });

        ctx.get().setPacketHandled(true);
    }
}