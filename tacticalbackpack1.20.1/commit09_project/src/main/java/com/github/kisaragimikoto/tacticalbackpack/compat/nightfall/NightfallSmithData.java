package com.github.kisaragimikoto.tacticalbackpack.compat.nightfall;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;

/** TacticalBackpack-owned smith metadata. Foreign weapon data remains opaque and untouched. */
public final class NightfallSmithData {
    private static final String ROOT = "TacticalBackpackNightfallSmith";
    private static final String LEVEL = "UpgradeLevel";
    private static final String RANK = "Rank";
    private static final String AWAKENED = "Awakened";

    private NightfallSmithData() {}

    public static Snapshot read(ItemStack stack) {
        CompoundTag root = stack.getTagElement(ROOT);
        if (root == null) return new Snapshot(0, 0, false);
        return new Snapshot(
                Math.max(0, root.getInt(LEVEL)),
                Math.max(0, root.getInt(RANK)),
                root.getBoolean(AWAKENED)
        );
    }

    public static boolean write(ItemStack stack, int level, int rank, boolean awakened) {
        if (stack.isEmpty() || !NightfallWeaponBridge.isNightfallItem(stack)) return false;
        CompoundTag root = stack.getOrCreateTagElement(ROOT);
        root.putInt(LEVEL, Math.max(0, Math.min(level, 1000)));
        root.putInt(RANK, Math.max(0, Math.min(rank, 100)));
        root.putBoolean(AWAKENED, awakened);
        return true;
    }

    public static int upgrade(ItemStack stack, int amount) {
        Snapshot current = read(stack);
        int next = Math.max(0, Math.min(1000, current.level() + amount));
        return write(stack, next, current.rank(), current.awakened()) ? next : -1;
    }

    public record Snapshot(int level, int rank, boolean awakened) {}
}
