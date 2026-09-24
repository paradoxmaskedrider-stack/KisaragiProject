package com.github.kisaragimikoto.tacticalbackpack.network;

import com.github.kisaragimikoto.tacticalbackpack.client.AE2ClientInventoryCache;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

/** Server -> client page of ME item variants. */
public final class AE2ItemListPacket {
    public record Entry(ItemStack stack, long amount) { }

    private final String query;
    private final int page;
    private final int totalPages;
    private final List<Entry> entries;

    public AE2ItemListPacket(String query, int page, int totalPages, List<Entry> entries) {
        this.query = query == null ? "" : query;
        this.page = Math.max(0, page);
        this.totalPages = Math.max(1, totalPages);
        this.entries = List.copyOf(entries);
    }

    public String query() { return query; }
    public int page() { return page; }
    public int totalPages() { return totalPages; }
    public List<Entry> entries() { return entries; }

    public static void encode(AE2ItemListPacket msg, FriendlyByteBuf buf) {
        buf.writeUtf(msg.query, 128);
        buf.writeVarInt(msg.page);
        buf.writeVarInt(msg.totalPages);
        buf.writeVarInt(msg.entries.size());
        for (Entry entry : msg.entries) {
            CompoundTag tag = new CompoundTag();
            entry.stack.save(tag);
            buf.writeNbt(tag);
            buf.writeVarLong(Math.max(0L, entry.amount));
        }
    }

    public static AE2ItemListPacket decode(FriendlyByteBuf buf) {
        String query = buf.readUtf(128);
        int page = buf.readVarInt();
        int totalPages = buf.readVarInt();
        int size = buf.readVarInt();
        if (size < 0 || size > 16) throw new IllegalArgumentException("Invalid AE2 item page size: " + size);
        List<Entry> entries = new ArrayList<>(size);
        for (int i = 0; i < size; i++) {
            CompoundTag tag = buf.readNbt();
            ItemStack stack = tag == null ? ItemStack.EMPTY : ItemStack.of(tag);
            entries.add(new Entry(stack, buf.readVarLong()));
        }
        return new AE2ItemListPacket(query, page, totalPages, entries);
    }

    public static void handle(AE2ItemListPacket msg, Supplier<NetworkEvent.Context> ctxSupplier) {
        NetworkEvent.Context ctx = ctxSupplier.get();
        ctx.enqueueWork(() -> DistExecutor.unsafeRunWhenOn(Dist.CLIENT,
                () -> () -> ClientHandler.handle(msg)));
        ctx.setPacketHandled(true);
    }

    private static final class ClientHandler {
        private static void handle(AE2ItemListPacket msg) {
            AE2ClientInventoryCache.accept(msg);
        }
    }

    private AE2ItemListPacket() {
        this("", 0, 1, List.of());
    }
}
