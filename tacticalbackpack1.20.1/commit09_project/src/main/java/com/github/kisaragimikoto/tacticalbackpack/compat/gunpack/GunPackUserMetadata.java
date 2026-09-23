package com.github.kisaragimikoto.tacticalbackpack.compat.gunpack;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.minecraftforge.fml.loading.FMLPaths;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

/** Persistent user-owned favorites and tags. Never modifies a downloaded pack. */
public final class GunPackUserMetadata {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Path FILE = FMLPaths.CONFIGDIR.get().resolve("tacticalbackpack/gunpack_user_metadata.json");

    public static synchronized boolean isFavorite(String platform, String packId) {
        return entry(platform, packId).favorite;
    }

    public static synchronized boolean setFavorite(String platform, String packId, boolean favorite) {
        Store store = load();
        Entry entry = store.getOrCreate(key(platform, packId));
        entry.favorite = favorite;
        save(store);
        return favorite;
    }

    public static synchronized List<String> tags(String platform, String packId) {
        return List.copyOf(entry(platform, packId).tags);
    }

    public static synchronized List<String> addTag(String platform, String packId, String tag) {
        String normalized = normalizeTag(tag);
        if (normalized.isBlank()) return tags(platform, packId);
        Store store = load();
        Entry entry = store.getOrCreate(key(platform, packId));
        entry.tags.add(normalized);
        save(store);
        return List.copyOf(entry.tags);
    }


    public static synchronized List<String> setTags(String platform, String packId, java.util.Collection<String> tags) {
        Store store = load();
        Entry entry = store.getOrCreate(key(platform, packId));
        entry.tags.clear();
        if (tags != null) {
            for (String tag : tags) {
                String normalized = normalizeTag(tag);
                if (!normalized.isBlank()) entry.tags.add(normalized);
            }
        }
        save(store);
        return List.copyOf(entry.tags);
    }
    public static synchronized List<String> removeTag(String platform, String packId, String tag) {
        Store store = load();
        Entry entry = store.getOrCreate(key(platform, packId));
        entry.tags.remove(normalizeTag(tag));
        save(store);
        return List.copyOf(entry.tags);
    }

    public static synchronized List<String> favoriteKeys() {
        Store store = load();
        List<String> result = new ArrayList<>();
        store.entries.entrySet().stream()
                .filter(e -> e.getValue().favorite)
                .map(java.util.Map.Entry::getKey)
                .sorted(String.CASE_INSENSITIVE_ORDER)
                .forEach(result::add);
        return List.copyOf(result);
    }

    public static Path path() { return FILE; }

    private static Entry entry(String platform, String packId) {
        return load().getOrCreate(key(platform, packId));
    }

    private static String key(String platform, String packId) {
        return platform.trim().toLowerCase(Locale.ROOT) + ":" + packId.trim().toLowerCase(Locale.ROOT);
    }

    private static String normalizeTag(String tag) {
        if (tag == null) return "";
        return tag.trim().toLowerCase(Locale.ROOT).replaceAll("[^a-z0-9_\\-ぁ-んァ-ヶ一-龠]", "_");
    }

    private static Store load() {
        try {
            if (!Files.exists(FILE)) return new Store();
            JsonObject root = JsonParser.parseString(Files.readString(FILE, StandardCharsets.UTF_8)).getAsJsonObject();
            Store store = GSON.fromJson(root, Store.class);
            return store == null ? new Store() : store;
        } catch (Exception ignored) {
            return new Store();
        }
    }

    private static void save(Store store) {
        try {
            Files.createDirectories(FILE.getParent());
            Path temporary = FILE.resolveSibling(FILE.getFileName() + ".tmp");
            Files.writeString(temporary, GSON.toJson(store), StandardCharsets.UTF_8);
            try {
                Files.move(temporary, FILE, StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING);
            } catch (IOException ignored) {
                Files.move(temporary, FILE, StandardCopyOption.REPLACE_EXISTING);
            }
        } catch (IOException exception) {
            throw new IllegalStateException("Could not save Gun Pack metadata", exception);
        }
    }

    private static final class Store {
        private java.util.Map<String, Entry> entries = new java.util.LinkedHashMap<>();
        private Entry getOrCreate(String key) { return entries.computeIfAbsent(key, ignored -> new Entry()); }
    }

    private static final class Entry {
        private boolean favorite;
        private Set<String> tags = new LinkedHashSet<>();
    }

    private GunPackUserMetadata() { }
}
