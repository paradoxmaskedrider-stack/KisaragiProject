package com.github.kisaragimikoto.tacticalbackpack.compat.tacz;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.fml.ModList;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class TaczRecipeLoader {

    private static final Gson GSON = new Gson();

    public static void loadBuiltinRecipes() {
        if (!ModList.get().isLoaded("tacz")) {
            return;
        }

        TaczRecipeRegistry.clear();

        // まずは自MOD側の互換JSONを読む
        loadJson("/data/tacticalbackpack/tacz_recipes/example_gun.json",
                new ResourceLocation("tacticalbackpack", "example_gun"));
    }

    private static void loadJson(String path, ResourceLocation id) {
        try (InputStream stream = TaczRecipeLoader.class.getResourceAsStream(path)) {
            if (stream == null) {
                return;
            }

            JsonObject root = JsonParser.parseReader(
                    new InputStreamReader(stream, StandardCharsets.UTF_8)
            ).getAsJsonObject();

            String resultItem = root.get("result").getAsString();
            int resultCount = root.has("count") ? root.get("count").getAsInt() : 1;

            List<String> ingredients = new ArrayList<>();
            JsonArray array = root.getAsJsonArray("ingredients");
            for (int i = 0; i < array.size(); i++) {
                ingredients.add(array.get(i).getAsString());
            }

            TaczRecipeRegistry.register(
                    new TaczRecipeEntry(id, resultItem, resultCount, ingredients)
            );

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}