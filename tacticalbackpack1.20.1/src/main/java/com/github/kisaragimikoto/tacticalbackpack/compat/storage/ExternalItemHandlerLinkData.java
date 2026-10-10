package com.github.kisaragimikoto.tacticalbackpack.compat.storage;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

import java.util.Optional;

/** Generic Forge IItemHandler link metadata, independent of any specific storage mod. */
public record ExternalItemHandlerLinkData(String dimensionId, long blockPos, String sideName) {
    private static final String ROOT = "TacticalBackpackExternalStorage";
    private static final String DIMENSION = "Dimension";
    private static final String POS = "BlockPos";
    private static final String SIDE = "Side";

    public static void save(ItemStack backpack, ResourceLocation dimension, BlockPos pos, Direction side) {
        if (backpack == null || backpack.isEmpty() || dimension == null || pos == null) return;
        CompoundTag root = backpack.getOrCreateTag().getCompound(ROOT);
        root.putString(DIMENSION, dimension.toString());
        root.putLong(POS, pos.asLong());
        root.putString(SIDE, side == null ? "" : side.getName());
        backpack.getOrCreateTag().put(ROOT, root);
    }

    public static Optional<ExternalItemHandlerLinkData> load(ItemStack backpack) {
        if (backpack == null || backpack.isEmpty() || backpack.getTag() == null
                || !backpack.getTag().contains(ROOT)) return Optional.empty();
        CompoundTag root = backpack.getTag().getCompound(ROOT);
        String dimension = root.getString(DIMENSION);
        if (dimension.isBlank() || !root.contains(POS)) return Optional.empty();
        return Optional.of(new ExternalItemHandlerLinkData(dimension, root.getLong(POS), root.getString(SIDE)));
    }

    public static void clear(ItemStack backpack) {
        if (backpack != null && !backpack.isEmpty() && backpack.getTag() != null) {
            backpack.getTag().remove(ROOT);
        }
    }

    public BlockPos pos() {
        return BlockPos.of(blockPos);
    }

    public Direction side() {
        if (sideName == null || sideName.isBlank()) return null;
        return Direction.byName(sideName);
    }
}
