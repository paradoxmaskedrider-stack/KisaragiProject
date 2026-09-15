package com.github.kisaragimikoto.tacticalbackpack.compat.tacz;

import net.minecraft.resources.ResourceLocation;

import java.util.List;

public class TaczRecipeEntry {

    private final ResourceLocation id;
    private final String resultItem;
    private final int resultCount;
    private final List<String> ingredients;

    public TaczRecipeEntry(ResourceLocation id, String resultItem, int resultCount, List<String> ingredients) {
        this.id = id;
        this.resultItem = resultItem;
        this.resultCount = resultCount;
        this.ingredients = ingredients;
    }

    public ResourceLocation getId() {
        return id;
    }

    public String getResultItem() {
        return resultItem;
    }

    public int getResultCount() {
        return resultCount;
    }

    public List<String> getIngredients() {
        return ingredients;
    }
}