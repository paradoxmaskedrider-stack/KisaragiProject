package com.github.kisaragimikoto.tacticalbackpack.network;

import com.github.kisaragimikoto.tacticalbackpack.compat.ae2.AE2Integration;
import com.github.kisaragimikoto.tacticalbackpack.menu.TacticalBackpackMenu;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

/** Pulls an exact AE item variant selected in the ME browser. */
public final class AE2PullStackPacket {
    private final ItemStack template;
    private final int amount;
    private final String query;
    private final int page;

    public AE2PullStackPacket(ItemStack template, int amount, String query, int page) {
        this.template = template == null ? ItemStack.EMPTY : template.copy();
        if (!this.template.isEmpty()) this.template.setCount(1);
        this.amount = Math.max(1, Math.min(amount, 64 * 256));
        this.query = query == null ? "" : query;
        this.page = Math.max(0, page);
    }

    public static void encode(AE2PullStackPacket msg, FriendlyByteBuf buf) {
        CompoundTag tag = new CompoundTag();
        msg.template.save(tag);
        buf.writeNbt(tag);
        buf.writeVarInt(msg.amount);
        buf.writeUtf(msg.query, 128);
        buf.writeVarInt(msg.page);
    }

    public static AE2PullStackPacket decode(FriendlyByteBuf buf) {
        CompoundTag tag = buf.readNbt();
        ItemStack stack = tag == null ? ItemStack.EMPTY : ItemStack.of(tag);
        return new AE2PullStackPacket(stack, buf.readVarInt(), buf.readUtf(128), buf.readVarInt());
    }

    public static void handle(AE2PullStackPacket msg, Supplier<NetworkEvent.Context> ctxSupplier) {
        NetworkEvent.Context ctx = ctxSupplier.get();
        ctx.enqueueWork(() -> {
            ServerPlayer player = ctx.getSender();
            if (player == null || !(player.containerMenu instanceof TacticalBackpackMenu menu)) return;
            if (!AE2Integration.isEnabled() || msg.template.isEmpty()) return;

            menu.saveBackpackInventory();
            int moved = AE2Integration.pullFromME(player, menu.getBackpackStack(), msg.template, msg.amount);
            menu.reloadBackpackInventory();
            player.displayClientMessage(Component.translatable("message.tacticalbackpack.ae2.pulled", moved), false);
            AE2ItemListRequestPacket.sendPage(player, menu, msg.query, msg.page);
        });
        ctx.setPacketHandled(true);
    }
}
