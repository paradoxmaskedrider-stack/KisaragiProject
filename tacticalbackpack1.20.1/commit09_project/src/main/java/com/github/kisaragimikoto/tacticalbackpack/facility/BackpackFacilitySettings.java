package com.github.kisaragimikoto.tacticalbackpack.facility;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;

/** Per-backpack facility switches. Missing values default to enabled. */
public final class BackpackFacilitySettings {
    private static final String ROOT_TAG = "FacilitySettings";

    private BackpackFacilitySettings() {}

    public static boolean isEnabled(ItemStack backpack, BackpackFacility facility) {
        CompoundTag root = backpack.getTag();
        if (root == null || !root.contains(ROOT_TAG, CompoundTag.TAG_COMPOUND)) {
            return true;
        }
        CompoundTag settings = root.getCompound(ROOT_TAG);
        return !settings.contains(facility.key()) || settings.getBoolean(facility.key());
    }

    public static void setEnabled(ItemStack backpack, BackpackFacility facility, boolean enabled) {
        CompoundTag root = backpack.getOrCreateTag();
        CompoundTag settings = root.contains(ROOT_TAG, CompoundTag.TAG_COMPOUND)
                ? root.getCompound(ROOT_TAG)
                : new CompoundTag();
        settings.putBoolean(facility.key(), enabled);
        root.put(ROOT_TAG, settings);
        backpack.setTag(root);
    }

    public static boolean toggle(ItemStack backpack, BackpackFacility facility) {
        boolean next = !isEnabled(backpack, facility);
        setEnabled(backpack, facility, next);
        return next;
    }
}
