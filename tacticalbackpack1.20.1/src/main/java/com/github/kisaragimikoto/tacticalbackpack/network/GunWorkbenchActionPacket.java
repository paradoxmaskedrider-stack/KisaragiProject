package com.github.kisaragimikoto.tacticalbackpack.network;

import com.github.kisaragimikoto.tacticalbackpack.item.TacticalBackpackItem;
import com.github.kisaragimikoto.tacticalbackpack.inventory.BackpackInventory;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class GunWorkbenchActionPacket {

    public static final int ASSEMBLE = 0;
    public static final int CLEAR = 1;

    private final int action;

    public GunWorkbenchActionPacket(int action) {
        this.action = action;
    }

    public static void encode(GunWorkbenchActionPacket msg, FriendlyByteBuf buf) {
        buf.writeInt(msg.action);
    }

    public static GunWorkbenchActionPacket decode(FriendlyByteBuf buf) {
        return new GunWorkbenchActionPacket(buf.readInt());
    }

    public static void handle(GunWorkbenchActionPacket msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayer player = ctx.get().getSender();
            if (player == null) return;

            ItemStack backpack = TacticalBackpackItem.getEquippedBackpack(player);
            if (backpack.isEmpty()) return;

            BackpackInventory inv = TacticalBackpackItem.getInventory(backpack);

            if (msg.action == CLEAR) {
                for (int i = 0; i < 6; i++) {
                    inv.setItem(i, ItemStack.EMPTY);
                }

                TacticalBackpackItem.saveInventory(backpack, inv);

                player.displayClientMessage(
                        Component.literal("Gun Workbench cleared"),
                        true
                );
                return;
            }

            if (msg.action == ASSEMBLE) {
                player.displayClientMessage(
                        Component.literal("Assemble clicked: TaCZ recipe support coming soon"),
                        true
                );

                TacticalBackpackItem.saveInventory(backpack, inv);
            }
        });

        ctx.get().setPacketHandled(true);
    }
}