package com.github.kisaragimikoto.tacticalbackpack.network;

import com.github.kisaragimikoto.tacticalbackpack.compat.ae2.AE2BackpackTransferService;
import com.github.kisaragimikoto.tacticalbackpack.compat.ae2.AE2Integration;
import com.github.kisaragimikoto.tacticalbackpack.compat.ae2.AE2LinkData;
import com.github.kisaragimikoto.tacticalbackpack.compat.ae2.AE2StorageAccess;
import com.github.kisaragimikoto.tacticalbackpack.menu.TacticalBackpackMenu;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraftforge.network.NetworkEvent;

import java.util.Optional;
import java.util.function.Supplier;

/** Server-authoritative AE2 actions issued from the backpack screen. */
public final class AE2BackpackActionPacket {
    public enum Action {
        LINK_TARGET,
        UNLINK,
        PUSH_ALL,
        PULL_ITEM,
        WIRELESS_OPEN,
        WIRELESS_CLOSE
    }

    private final Action action;
    private final BlockPos targetPos;
    private final String itemId;
    private final int amount;

    public AE2BackpackActionPacket(Action action, BlockPos targetPos, String itemId, int amount) {
        this.action = action;
        this.targetPos = targetPos == null ? BlockPos.ZERO : targetPos.immutable();
        this.itemId = itemId == null ? "" : itemId;
        this.amount = Math.max(0, amount);
    }

    public static AE2BackpackActionPacket link(BlockPos pos) {
        return new AE2BackpackActionPacket(Action.LINK_TARGET, pos, "", 0);
    }

    public static AE2BackpackActionPacket simple(Action action) {
        return new AE2BackpackActionPacket(action, BlockPos.ZERO, "", 0);
    }

    public static AE2BackpackActionPacket pull(String itemId, int amount) {
        return new AE2BackpackActionPacket(Action.PULL_ITEM, BlockPos.ZERO, itemId, amount);
    }

    public static void encode(AE2BackpackActionPacket msg, FriendlyByteBuf buf) {
        buf.writeEnum(msg.action);
        buf.writeBlockPos(msg.targetPos);
        buf.writeUtf(msg.itemId, 256);
        buf.writeVarInt(msg.amount);
    }

    public static AE2BackpackActionPacket decode(FriendlyByteBuf buf) {
        return new AE2BackpackActionPacket(
                buf.readEnum(Action.class),
                buf.readBlockPos(),
                buf.readUtf(256),
                buf.readVarInt()
        );
    }

    public static void handle(AE2BackpackActionPacket msg, Supplier<NetworkEvent.Context> ctxSupplier) {
        NetworkEvent.Context ctx = ctxSupplier.get();
        ctx.enqueueWork(() -> {
            ServerPlayer player = ctx.getSender();
            if (player == null || !(player.containerMenu instanceof TacticalBackpackMenu menu)) return;

            ItemStack backpack = menu.getBackpackStack();
            if (backpack.isEmpty()) return;

            if (!AE2Integration.isEnabled()) {
                player.displayClientMessage(Component.translatable("message.tacticalbackpack.ae2.disabled"), false);
                return;
            }

            menu.saveBackpackInventory();

            switch (msg.action) {
                case LINK_TARGET -> handleLink(player, menu, backpack, msg.targetPos);
                case UNLINK -> {
                    AE2Integration.closeWirelessSession(player);
                    AE2Integration.unlink(backpack);
                    player.displayClientMessage(Component.translatable("message.tacticalbackpack.ae2.unlinked"), false);
                }
                case PUSH_ALL -> {
                    AE2BackpackTransferService.TransferResult result = AE2Integration.pushAllToME(player, backpack);
                    menu.reloadBackpackInventory();
                    if (!result.online()) {
                        player.displayClientMessage(Component.translatable("message.tacticalbackpack.ae2.offline"), false);
                    } else {
                        player.displayClientMessage(Component.translatable(
                                "message.tacticalbackpack.ae2.pushed", result.moved(), result.touchedSlots()), false);
                    }
                }
                case PULL_ITEM -> handlePull(player, menu, backpack, msg.itemId, msg.amount);
                case WIRELESS_OPEN -> {
                    boolean opened = AE2Integration.openWirelessSession(player, backpack);
                    player.displayClientMessage(Component.translatable(opened
                            ? "message.tacticalbackpack.ae2.wireless_open"
                            : "message.tacticalbackpack.ae2.offline"), false);
                }
                case WIRELESS_CLOSE -> {
                    AE2Integration.closeWirelessSession(player);
                    player.displayClientMessage(Component.translatable("message.tacticalbackpack.ae2.wireless_closed"), false);
                }
            }

            menu.broadcastChanges();
        });
        ctx.setPacketHandled(true);
    }

    private static void handleLink(ServerPlayer player, TacticalBackpackMenu menu,
                                   ItemStack backpack, BlockPos target) {
        // Do not allow a client to link arbitrary remote coordinates.
        if (!player.level().isLoaded(target) || player.distanceToSqr(
                target.getX() + 0.5D,
                target.getY() + 0.5D,
                target.getZ() + 0.5D) > 64.0D) {
            player.displayClientMessage(Component.translatable("message.tacticalbackpack.ae2.link_invalid"), false);
            return;
        }

        Optional<AE2LinkData> previous = AE2Integration.getLink(backpack);
        AE2Integration.link(backpack, player.level().dimension().location(), target);
        AE2StorageAccess storage = AE2Integration.resolveStorage(player, backpack);
        if (!storage.isOnline()) {
            AE2Integration.unlink(backpack);
            previous.ifPresent(link -> link.save(backpack));
            player.displayClientMessage(Component.translatable("message.tacticalbackpack.ae2.link_invalid"), false);
            return;
        }

        player.displayClientMessage(Component.translatable(
                "message.tacticalbackpack.ae2.linked",
                target.getX(), target.getY(), target.getZ()), false);
        menu.broadcastChanges();
    }

    private static void handlePull(ServerPlayer player, TacticalBackpackMenu menu,
                                   ItemStack backpack, String rawId, int requestedAmount) {
        ResourceLocation id = ResourceLocation.tryParse(rawId == null ? "" : rawId.trim());
        if (id == null) {
            player.displayClientMessage(Component.translatable("message.tacticalbackpack.ae2.bad_item"), false);
            return;
        }

        Optional<Item> item = BuiltInRegistries.ITEM.getOptional(id).filter(value -> value != Items.AIR);
        if (item.isEmpty()) {
            player.displayClientMessage(Component.translatable("message.tacticalbackpack.ae2.bad_item"), false);
            return;
        }

        int amount = Math.max(1, Math.min(requestedAmount, 64 * 256));
        int moved = AE2Integration.pullFromME(player, backpack, new ItemStack(item.get()), amount);
        menu.reloadBackpackInventory();
        player.displayClientMessage(Component.translatable("message.tacticalbackpack.ae2.pulled", moved), false);
    }

}
