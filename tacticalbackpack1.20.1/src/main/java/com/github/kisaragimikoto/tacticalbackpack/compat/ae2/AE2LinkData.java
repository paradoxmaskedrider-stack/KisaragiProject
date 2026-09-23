package com.github.kisaragimikoto.tacticalbackpack.compat.ae2;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.nbt.Tag;
import net.minecraft.core.BlockPos;

import java.util.Optional;
import java.util.UUID;

/**
 * Persistent AE2 link metadata stored on the backpack ItemStack.
 * The data deliberately contains no AE2 API types so a backpack remains readable
 * when AE2 is temporarily removed from a modpack.
 */
public record AE2LinkData(String dimensionId, long blockPos, String linkId) {
    public static final String TAG_ROOT = "TacticalBackpackAE2";
    private static final String TAG_DIMENSION = "Dimension";
    private static final String TAG_POS = "BlockPos";
    private static final String TAG_LINK_ID = "LinkId";

    public static AE2LinkData create(ResourceLocation dimension, BlockPos pos) {
        return new AE2LinkData(dimension.toString(), pos.asLong(), UUID.randomUUID().toString());
    }

    public BlockPos pos() {
        return BlockPos.of(blockPos);
    }

    public Optional<ResourceLocation> dimension() {
        return ResourceLocation.tryParse(dimensionId) == null
                ? Optional.empty()
                : Optional.of(ResourceLocation.tryParse(dimensionId));
    }

    public void save(ItemStack backpack) {
        CompoundTag root = backpack.getOrCreateTag();
        CompoundTag tag = new CompoundTag();
        tag.putString(TAG_DIMENSION, dimensionId);
        tag.putLong(TAG_POS, blockPos);
        tag.putString(TAG_LINK_ID, linkId);
        root.put(TAG_ROOT, tag);
    }

    public static Optional<AE2LinkData> load(ItemStack backpack) {
        CompoundTag root = backpack.getTag();
        if (root == null || !root.contains(TAG_ROOT, Tag.TAG_COMPOUND)) {
            return Optional.empty();
        }
        CompoundTag tag = root.getCompound(TAG_ROOT);
        String dimension = tag.getString(TAG_DIMENSION);
        String linkId = tag.getString(TAG_LINK_ID);
        if (dimension.isBlank()) {
            return Optional.empty();
        }
        return Optional.of(new AE2LinkData(dimension, tag.getLong(TAG_POS), linkId));
    }

    public static void clear(ItemStack backpack) {
        CompoundTag root = backpack.getTag();
        if (root != null) {
            root.remove(TAG_ROOT);
        }
    }
}
