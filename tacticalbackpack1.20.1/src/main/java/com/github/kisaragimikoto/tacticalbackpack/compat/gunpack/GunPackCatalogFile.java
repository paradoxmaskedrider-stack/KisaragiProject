package com.github.kisaragimikoto.tacticalbackpack.compat.gunpack;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.minecraftforge.fml.loading.FMLPaths;

import java.io.IOException;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Loads a user-editable provider-neutral gun-pack catalog from config.
 * The file may contain CurseForge-approved direct download URLs or another
 * distribution endpoint chosen by the pack author. HTML scraping is not used.
 */
public final class GunPackCatalogFile {
    private static final Path DIRECTORY = FMLPaths.CONFIGDIR.get().resolve("tacticalbackpack");
    private static final Path CATALOG = DIRECTORY.resolve("gunpack_catalog.json");

    public static LoadResult reload() {
        try {
            ensureTemplate();
            JsonElement root = JsonParser.parseString(Files.readString(CATALOG, StandardCharsets.UTF_8));
            JsonArray packs = root.isJsonArray() ? root.getAsJsonArray()
                    : root.getAsJsonObject().has("packs") ? root.getAsJsonObject().getAsJsonArray("packs")
                    : new JsonArray();
            List<GunPackDescriptor> loaded = new ArrayList<>();
            List<String> errors = new ArrayList<>();
            int index = 0;
            for (JsonElement element : packs) {
                index++;
                try {
                    if (!element.isJsonObject()) throw new IllegalArgumentException("entry is not an object");
                    loaded.add(parse(element.getAsJsonObject()));
                } catch (Exception exception) {
                    errors.add("Entry " + index + ": " + exception.getMessage());
                }
            }
            GunPackCatalogRegistry.replaceAll(loaded);
            return new LoadResult(loaded.size(), List.copyOf(errors), CATALOG);
        } catch (Exception exception) {
            return new LoadResult(0, List.of(exception.getMessage()), CATALOG);
        }
    }

    public static Path path() {
        return CATALOG;
    }

    private static GunPackDescriptor parse(JsonObject object) {
        String id = required(object, "id");
        String name = required(object, "name");
        String version = optional(object, "version", "unknown");
        GunPlatform platform = GunPlatform.valueOf(required(object, "platform").toUpperCase(Locale.ROOT));
        String minecraft = optional(object, "minecraftVersion", "1.20.1");
        URI uri = URI.create(required(object, "downloadUrl"));
        if (!"https".equalsIgnoreCase(uri.getScheme())) throw new IllegalArgumentException("downloadUrl must use HTTPS");
        String sha256 = optional(object, "sha256", "");
        String source = optional(object, "source", "CurseForge");
        boolean restart = !object.has("requiresRestart") || object.get("requiresRestart").getAsBoolean();
        return new GunPackDescriptor(id, name, version, platform, minecraft, uri, sha256, source, restart);
    }

    private static String required(JsonObject object, String key) {
        if (!object.has(key) || object.get(key).getAsString().isBlank()) throw new IllegalArgumentException("missing " + key);
        return object.get(key).getAsString().trim();
    }

    private static String optional(JsonObject object, String key, String fallback) {
        return object.has(key) ? object.get(key).getAsString().trim() : fallback;
    }

    private static void ensureTemplate() throws IOException {
        Files.createDirectories(DIRECTORY);
        if (Files.exists(CATALOG)) return;
        Path temporary = DIRECTORY.resolve("gunpack_catalog.json.tmp");
        String template = """
                {
                  "packs": [
                    {
                      "id": "example_tacz_pack",
                      "name": "Example TaCZ Pack (replace this entry)",
                      "version": "1.0.0",
                      "platform": "TACZ",
                      "minecraftVersion": "1.20.1",
                      "downloadUrl": "https://example.invalid/example-pack.zip",
                      "sha256": "",
                      "source": "CurseForge",
                      "requiresRestart": true
                    }
                  ]
                }
                """;
        Files.writeString(temporary, template, StandardCharsets.UTF_8);
        try {
            Files.move(temporary, CATALOG, StandardCopyOption.ATOMIC_MOVE);
        } catch (IOException ignored) {
            Files.move(temporary, CATALOG, StandardCopyOption.REPLACE_EXISTING);
        }
    }

    public record LoadResult(int loaded, List<String> errors, Path path) {
        public boolean success() { return errors.isEmpty(); }
    }

    private GunPackCatalogFile() {}
}
