package com.github.kisaragimikoto.tacticalbackpack.network;

import com.github.kisaragimikoto.tacticalbackpack.menu.BackpackWorkbenchMenu;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class UpdateWorkbenchSearchPacket {

    private final String searchText;

    public UpdateWorkbenchSearchPacket(String searchText) {
        this.searchText = searchText;
    }

    public static void encode(UpdateWorkbenchSearchPacket packet, FriendlyByteBuf buf) {
        buf.writeUtf(packet.searchText);
    }

    public static UpdateWorkbenchSearchPacket decode(FriendlyByteBuf buf) {
        return new UpdateWorkbenchSearchPacket(buf.readUtf(32767));
    }

    public static void handle(UpdateWorkbenchSearchPacket packet, Supplier<NetworkEvent.Context> ctxSupplier) {
        NetworkEvent.Context ctx = ctxSupplier.get();

        ctx.enqueueWork(() -> {
            ServerPlayer player = ctx.getSender();
            if (player == null) {
                return;
            }

            if (player.containerMenu instanceof BackpackWorkbenchMenu menu) {
                menu.setSearchText(packet.searchText);
            }
        });

        ctx.setPacketHandled(true);
    }
}