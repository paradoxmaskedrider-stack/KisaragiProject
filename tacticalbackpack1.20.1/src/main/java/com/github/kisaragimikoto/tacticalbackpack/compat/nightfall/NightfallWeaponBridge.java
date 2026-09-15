package com.github.kisaragimikoto.tacticalbackpack.compat.nightfall;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

import java.util.Set;

/** Namespace-based weapon recognition. No Nightfall internals or foreign NBT are modified. */
public final class NightfallWeaponBridge {
    private static final Set<String> KNOWN_NAMESPACES = Set.of(
            "epicfight_nightfall", "epicfightnightfall", "nightfall", "efn"
    );

    private NightfallWeaponBridge() {}

    public static boolean isNightfallItem(ItemStack stack) {
        if (stack.isEmpty()) return false;
        ResourceLocation id = BuiltInRegistries.ITEM.getKey(stack.getItem());
        return id != null && KNOWN_NAMESPACES.contains(id.getNamespace());
    }

    public static ResourceLocation itemId(ItemStack stack) {
        return stack.isEmpty() ? new ResourceLocation("minecraft", "air")
                : BuiltInRegistries.ITEM.getKey(stack.getItem());
    }
}
