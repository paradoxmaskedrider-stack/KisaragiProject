package com.github.kisaragimikoto.tacticalbackpack.compat.gunpack;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.minecraftforge.fml.loading.FMLPaths;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/** Persistent metadata for packs installed by TacticalBackpack. */
public final class GunPackInstallRegistry {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Path FILE = FMLPaths.CONFIGDIR.get().resolve("tacticalbackpack").resolve("gunpack_installed.json");

    public static synchronized void record(GunPackDescriptor descriptor, Path installedFile) throws IOException {
        List<Entry> entries = new ArrayList<>(load());
        entries.removeIf(entry -> entry.id().equals(descriptor.id()) && entry.platform() == descriptor.platform());
        entries.add(new Entry(descriptor.id(), descriptor.name(), descriptor.version(), descriptor.platform(),
                installedFile.getFileName().toString(), true, Instant.now().toEpochMilli()));
        save(entries);
    }

    public static synchronized void recordImported(GunPlatform platform, Path installedFile) throws IOException {
        String fileName = installedFile.getFileName().toString();
        List<Entry> entries = new ArrayList<>(load());
        entries.removeIf(entry -> entry.platform() == platform && entry.fileName().equals(fileName));
        entries.add(new Entry("local:" + fileName, fileName, "local", platform, fileName, true, Instant.now().toEpochMilli()));
        save(entries);
    }

    public static synchronized List<Entry> load() {
        if (!Files.isRegularFile(FILE)) return List.of();
        try {
            JsonElement parsed = JsonParser.parseString(Files.readString(FILE, StandardCharsets.UTF_8));
            JsonArray array = parsed.isJsonArray() ? parsed.getAsJsonArray()
                    : parsed.getAsJsonObject().has("packs") ? parsed.getAsJsonObject().getAsJsonArray("packs") : new JsonArray();
            List<Entry> result = new ArrayList<>();
            for (JsonElement element : array) {
                if (!element.isJsonObject()) continue;
                JsonObject object = element.getAsJsonObject();
                try {
                    result.add(new Entry(
                            object.get("id").getAsString(),
                            object.has("name") ? object.get("name").getAsString() : object.get("id").getAsString(),
                            object.has("version") ? object.get("version").getAsString() : "unknown",
                            GunPlatform.valueOf(object.get("platform").getAsString()),
                            object.get("fileName").getAsString(),
                            !object.has("enabled") || object.get("enabled").getAsBoolean(),
                            object.has("installedAt") ? object.get("installedAt").getAsLong() : 0L));
                } catch (RuntimeException ignored) { }
            }
            return List.copyOf(result);
        } catch (Exception ignored) {
            return List.of();
        }
    }

    public static synchronized Optional<Entry> find(String id, GunPlatform platform) {
        return load().stream().filter(entry -> entry.id().equals(id) && entry.platform() == platform).findFirst();
    }

    public static synchronized void setEnabled(String fileName, GunPlatform platform, boolean enabled) throws IOException {
        List<Entry> entries = new ArrayList<>(load());
        for (int i = 0; i < entries.size(); i++) {
            Entry entry = entries.get(i);
            if (entry.platform() == platform && entry.fileName().equals(fileName)) entries.set(i, entry.withEnabled(enabled));
        }
        save(entries);
    }


    public static synchronized void remove(String fileName, GunPlatform platform) throws IOException {
        List<Entry> entries = new ArrayList<>(load());
        entries.removeIf(entry -> entry.platform() == platform && entry.fileName().equals(fileName));
        save(entries);
    }

    private static void save(List<Entry> entries) throws IOException {
        Files.createDirectories(FILE.getParent());
        JsonArray array = new JsonArray();
        for (Entry entry : entries) {
            JsonObject object = new JsonObject();
            object.addProperty("id", entry.id());
            object.addProperty("name", entry.name());
            object.addProperty("version", entry.version());
            object.addProperty("platform", entry.platform().name());
            object.addProperty("fileName", entry.fileName());
            object.addProperty("enabled", entry.enabled());
            object.addProperty("installedAt", entry.installedAt());
            array.add(object);
        }
        JsonObject root = new JsonObject();
        root.add("packs", array);
        Path temp = FILE.resolveSibling(FILE.getFileName() + ".tmp");
        Files.writeString(temp, GSON.toJson(root), StandardCharsets.UTF_8);
        try { Files.move(temp, FILE, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE); }
        catch (IOException ignored) { Files.move(temp, FILE, StandardCopyOption.REPLACE_EXISTING); }
    }

    public record Entry(String id, String name, String version, GunPlatform platform, String fileName, boolean enabled, long installedAt) {
        public Entry withEnabled(boolean value) { return new Entry(id, name, version, platform, fileName, value, installedAt); }
    }
    private GunPackInstallRegistry() { }
}
