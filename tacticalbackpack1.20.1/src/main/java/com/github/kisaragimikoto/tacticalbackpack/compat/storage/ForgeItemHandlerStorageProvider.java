package com.github.kisaragimikoto.tacticalbackpack.compat.storage;

import com.github.kisaragimikoto.tacticalbackpack.config.BackpackConfig;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.items.IItemHandler;

import java.util.Optional;

/**
 * Generic Forge capability provider. This covers Create/Mekanism/other blocks
 * that expose ForgeCapabilities.ITEM_HANDLER without hard-linking their APIs.
 */
public final class ForgeItemHandlerStorageProvider implements ExternalStorageProvider {
    @Override
    public String id() {
        return "forge_item_handler";
    }

    @Override
    public int priority() {
        return 200;
    }

    @Override
    public boolean isAvailable(ServerPlayer player, ItemStack backpack) {
        return BackpackConfig.OVERFLOW_TO_ITEM_HANDLER.get() && resolve(player, backpack).isPresent();
    }

    @Override
    public ItemStack insert(ServerPlayer player, ItemStack backpack, ItemStack input) {
        Optional<IItemHandler> handler = resolve(player, backpack);
        if (handler.isEmpty() || input == null || input.isEmpty()) {
            return input == null ? ItemStack.EMPTY : input.copy();
        }

        ItemStack remaining = input.copy();
        IItemHandler target = handler.get();
        for (int slot = 0; slot < target.getSlots() && !remaining.isEmpty(); slot++) {
            ItemStack simulated = target.insertItem(slot, remaining.copy(), true);
            int couldInsert = remaining.getCount() - simulated.getCount();
            if (couldInsert <= 0) continue;

            ItemStack offered = remaining.copy();
            offered.setCount(couldInsert);
            ItemStack rejected = target.insertItem(slot, offered, false);
            int accepted = couldInsert - rejected.getCount();
            if (accepted > 0) remaining.shrink(accepted);
        }
        return remaining;
    }

    private static Optional<IItemHandler> resolve(ServerPlayer player, ItemStack backpack) {
        if (player == null || backpack == null || backpack.isEmpty()) return Optional.empty();
        Optional<ExternalItemHandlerLinkData> link = ExternalItemHandlerLinkData.load(backpack);
        if (link.isEmpty()) return Optional.empty();

        ResourceLocation dimensionId = ResourceLocation.tryParse(link.get().dimensionId());
        if (dimensionId == null || player.getServer() == null) return Optional.empty();
        ResourceKey<Level> key = ResourceKey.create(Registries.DIMENSION, dimensionId);
        ServerLevel level = player.getServer().getLevel(key);
        if (level == null) return Optional.empty();

        BlockPos pos = link.get().pos();
        if (!level.hasChunkAt(pos)) return Optional.empty();
        BlockEntity blockEntity = level.getBlockEntity(pos);
        if (blockEntity == null) return Optional.empty();

        return blockEntity.getCapability(ForgeCapabilities.ITEM_HANDLER, link.get().side()).resolve();
    }
}
