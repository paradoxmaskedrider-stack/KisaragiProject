package com.github.kisaragimikoto.tacticalbackpack.item;

import com.github.kisaragimikoto.tacticalbackpack.world.BackpackBiomeZoneManager;
import com.github.kisaragimikoto.tacticalbackpack.world.BackpackBiomeZoneType;
import com.github.kisaragimikoto.tacticalbackpack.world.ModDimensions;

import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class BackpackZoneControllerItem extends Item {

    public BackpackZoneControllerItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);

        if (level.isClientSide) {
            return InteractionResultHolder.success(stack);
        }

        if (!(level instanceof ServerLevel serverLevel)) {
            return InteractionResultHolder.pass(stack);
        }

        if (!serverLevel.dimension().equals(ModDimensions.BACKPACK_INNER_WORLD)) {
            player.displayClientMessage(
                    Component.literal("Only usable inside Backpack Inner World."),
                    true
            );
            return InteractionResultHolder.fail(stack);
        }

        if (player.isShiftKeyDown()) {
            switchZoneType(stack, player);
            return InteractionResultHolder.consume(stack);
        }

        BackpackBiomeZoneType type = getCurrentZoneType(stack);
        int size = getCurrentZoneSize(stack);

        int x = player.blockPosition().getX();
        int z = player.blockPosition().getZ();

        BackpackBiomeZoneManager.generateZone(serverLevel, type, x, z, size);

        player.displayClientMessage(
                Component.literal("Generated: " + type.getDisplayName() + " / Size: " + size + "x" + size),
                true
        );

        return InteractionResultHolder.consume(stack);
    }

    private static void switchZoneType(ItemStack stack, Player player) {
        BackpackBiomeZoneType[] types = BackpackBiomeZoneType.values();

        int index = stack.getOrCreateTag().getInt("ZoneTypeIndex");
        int nextIndex = (index + 1) % types.length;

        stack.getOrCreateTag().putInt("ZoneTypeIndex", nextIndex);

        BackpackBiomeZoneType nextType = types[nextIndex];

        player.displayClientMessage(
                Component.literal("Zone Type: " + nextType.getDisplayName()),
                true
        );
    }

    public static void switchZoneSize(ItemStack stack, Player player) {
        int current = getCurrentZoneSize(stack);

        int next = switch (current) {
            case 16 -> 24;
            case 24 -> 32;
            case 32 -> 48;
            case 48 -> 64;
            default -> 16;
        };

        stack.getOrCreateTag().putInt("ZoneSize", next);

        player.displayClientMessage(
                Component.literal("Zone Size: " + next + "x" + next),
                true
        );
    }

    private static BackpackBiomeZoneType getCurrentZoneType(ItemStack stack) {
        BackpackBiomeZoneType[] types = BackpackBiomeZoneType.values();

        int index = stack.getOrCreateTag().getInt("ZoneTypeIndex");

        if (index < 0 || index >= types.length) {
            index = 0;
            stack.getOrCreateTag().putInt("ZoneTypeIndex", index);
        }

        return types[index];
    }

    private static int getCurrentZoneSize(ItemStack stack) {
        int size = stack.getOrCreateTag().getInt("ZoneSize");

        if (size <= 0) {
            size = 24;
            stack.getOrCreateTag().putInt("ZoneSize", size);
        }

        return size;
    }

    @Override
    public boolean isFoil(ItemStack stack) {
        return true;
    }
}