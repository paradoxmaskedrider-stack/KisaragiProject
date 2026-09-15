package com.github.kisaragimikoto.tacticalbackpack.compat.tacz;

import net.minecraft.resources.ResourceLocation;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;

public class TaczRecipeRegistry {

    private static final Map<ResourceLocation, TaczRecipeEntry> RECIPES = new LinkedHashMap<>();

    public static void clear() {
        RECIPES.clear();
    }

    public static void register(TaczRecipeEntry entry) {
        RECIPES.put(entry.getId(), entry);
    }

    public static Collection<TaczRecipeEntry> getAll() {
        return RECIPES.values();
    }

    public static boolean isEmpty() {
        return RECIPES.isEmpty();
    }
}