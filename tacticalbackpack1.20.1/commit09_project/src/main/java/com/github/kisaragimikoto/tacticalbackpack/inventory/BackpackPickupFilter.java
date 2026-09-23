package com.github.kisaragimikoto.tacticalbackpack.inventory;

import net.minecraft.world.item.ItemStack;
import java.util.HashSet;
import java.util.Set;

public class BackpackPickupFilter {

    private static final Set<String> FILTER = new HashSet<>();

    public static void add(ItemStack stack) {
        FILTER.add(stack.getItem().toString());
    }

    public static boolean matches(ItemStack stack) {
        if (FILTER.isEmpty()) return true;
        return FILTER.contains(stack.getItem().toString());
    }
}