package com.github.kisaragimikoto.tacticalbackpack.network;

import com.github.kisaragimikoto.tacticalbackpack.compat.ae2.AE2Integration;
import com.github.kisaragimikoto.tacticalbackpack.compat.ae2.AE2ItemEntry;
import com.github.kisaragimikoto.tacticalbackpack.menu.TacticalBackpackMenu;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.PacketDistributor;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.function.Supplier;

/** Client -> server request for a filtered, paged ME item list. */
public final class AE2ItemListRequestPacket {
    public static final int PAGE_SIZE = 4;

    private final String query;
    private final int page;

    public AE2ItemListRequestPacket(String query, int page) {
        this.query = query == null ? "" : query.trim();
        this.page = Math.max(0, page);
    }

    public static void encode(AE2ItemListRequestPacket msg, FriendlyByteBuf buf) {
        buf.writeUtf(msg.query, 128);
        buf.writeVarInt(msg.page);
    }

    public static AE2ItemListRequestPacket decode(FriendlyByteBuf buf) {
        return new AE2ItemListRequestPacket(buf.readUtf(128), buf.readVarInt());
    }

    public static void handle(AE2ItemListRequestPacket msg, Supplier<NetworkEvent.Context> ctxSupplier) {
        NetworkEvent.Context ctx = ctxSupplier.get();
        ctx.enqueueWork(() -> {
            ServerPlayer player = ctx.getSender();
            if (player == null || !(player.containerMenu instanceof TacticalBackpackMenu menu)) return;
            sendPage(player, menu, msg.query, msg.page);
        });
        ctx.setPacketHandled(true);
    }

    public static void sendPage(ServerPlayer player, TacticalBackpackMenu menu, String rawQuery, int requestedPage) {
        String query = rawQuery == null ? "" : rawQuery.trim();
        String needle = query.toLowerCase(Locale.ROOT);
        ItemStack backpack = menu.getBackpackStack();

        List<AE2ItemEntry> source = AE2Integration.isEnabled()
                ? AE2Integration.getAvailableItems(player, backpack)
                : List.of();
        List<AE2ItemEntry> filtered = new ArrayList<>();
        for (AE2ItemEntry entry : source) {
            ItemStack stack = entry.stack();
            String id = BuiltInRegistries.ITEM.getKey(stack.getItem()).toString().toLowerCase(Locale.ROOT);
            String name = stack.getHoverName().getString().toLowerCase(Locale.ROOT);
            if (needle.isEmpty() || id.contains(needle) || name.contains(needle)) filtered.add(entry);
        }

        int totalPages = Math.max(1, (filtered.size() + PAGE_SIZE - 1) / PAGE_SIZE);
        int page = Math.min(Math.max(0, requestedPage), totalPages - 1);
        int from = Math.min(filtered.size(), page * PAGE_SIZE);
        int to = Math.min(filtered.size(), from + PAGE_SIZE);
        List<AE2ItemListPacket.Entry> pageEntries = new ArrayList<>();
        for (int i = from; i < to; i++) {
            AE2ItemEntry entry = filtered.get(i);
            pageEntries.add(new AE2ItemListPacket.Entry(entry.stack(), entry.amount()));
        }

        ModNetwork.CHANNEL.send(PacketDistributor.PLAYER.with(() -> player),
                new AE2ItemListPacket(query, page, totalPages, pageEntries));
    }
}
