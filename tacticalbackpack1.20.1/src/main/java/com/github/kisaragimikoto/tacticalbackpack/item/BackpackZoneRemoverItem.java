package com.github.kisaragimikoto.tacticalbackpack.item;

import com.github.kisaragimikoto.tacticalbackpack.world.BackpackBiomeZoneManager;
import com.github.kisaragimikoto.tacticalbackpack.world.ModDimensions;

import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class BackpackZoneRemoverItem extends Item {

    public BackpackZoneRemoverItem(Properties properties) {
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
            switchRemoveSize(stack, player);
            return InteractionResultHolder.consume(stack);
        }

        int size = getRemoveSize(stack);
        int x = player.blockPosition().getX();
        int z = player.blockPosition().getZ();

        BackpackBiomeZoneManager.deleteZone(serverLevel, x, z, size);

        player.displayClientMessage(
                Component.literal("Deleted zone / Size: " + size + "x" + size),
                true
        );

        return InteractionResultHolder.consume(stack);
    }

    private static int getRemoveSize(ItemStack stack) {
        int size = stack.getOrCreateTag().getInt("RemoveZoneSize");

        if (size <= 0) {
            size = 24;
            stack.getOrCreateTag().putInt("RemoveZoneSize", size);
        }

        return size;
    }

    private static void switchRemoveSize(ItemStack stack, Player player) {
        int current = getRemoveSize(stack);

        int next = switch (current) {
            case 16 -> 24;
            case 24 -> 32;
            case 32 -> 48;
            case 48 -> 64;
            default -> 16;
        };

        stack.getOrCreateTag().putInt("RemoveZoneSize", next);

        player.displayClientMessage(
                Component.literal("Remove Size: " + next + "x" + next),
                true
        );
    }

    @Override
    public boolean isFoil(ItemStack stack) {
        return true;
    }
}