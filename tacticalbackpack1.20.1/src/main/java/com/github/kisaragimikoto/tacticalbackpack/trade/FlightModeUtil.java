package com.github.kisaragimikoto.tacticalbackpack.trade;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;

public class FlightModeUtil {

    public static final String MODE_JETPACK = "JETPACK";
    public static final String MODE_ELYTRA = "ELYTRA";

    private static final String TAG_FLIGHT_MODE = "FlightMode";
    private static final String TAG_JETPACK_ENABLED = "JetpackEnabled";

    public static String getMode(ItemStack stack) {
        CompoundTag tag = stack.getOrCreateTag();

        if (!tag.contains(TAG_FLIGHT_MODE)) {
            tag.putString(TAG_FLIGHT_MODE, MODE_JETPACK);
        }

        return tag.getString(TAG_FLIGHT_MODE);
    }

    public static boolean isJetpack(ItemStack stack) {
        return MODE_JETPACK.equals(getMode(stack));
    }

    public static boolean isElytra(ItemStack stack) {
        return MODE_ELYTRA.equals(getMode(stack));
    }

    public static void toggle(ItemStack stack) {
        CompoundTag tag = stack.getOrCreateTag();

        if (isJetpack(stack)) {
            tag.putString(TAG_FLIGHT_MODE, MODE_ELYTRA);
        } else {
            tag.putString(TAG_FLIGHT_MODE, MODE_JETPACK);
        }
    }

    public static boolean isJetpackEnabled(ItemStack stack) {
        CompoundTag tag = stack.getOrCreateTag();

        if (!tag.contains(TAG_JETPACK_ENABLED)) {
            tag.putBoolean(TAG_JETPACK_ENABLED, true);
        }

        return tag.getBoolean(TAG_JETPACK_ENABLED);
    }

    public static void toggleJetpack(ItemStack stack) {
        CompoundTag tag = stack.getOrCreateTag();
        tag.putBoolean(TAG_JETPACK_ENABLED, !isJetpackEnabled(stack));
    }
}