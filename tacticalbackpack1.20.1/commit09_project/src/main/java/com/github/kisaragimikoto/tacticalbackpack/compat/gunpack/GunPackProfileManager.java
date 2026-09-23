package com.github.kisaragimikoto.tacticalbackpack.compat.gunpack;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonParseException;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

/** Saves, loads and atomically applies named Gun Pack environments. */
public final class GunPackProfileManager {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    public static List<String> listProfiles() {
        try {
            GunPackPaths.ensureDirectories();
            try (var stream = Files.list(GunPackPaths.profiles())) {
                return stream.filter(Files::isRegularFile)
                        .map(path -> path.getFileName().toString())
                        .filter(name -> name.endsWith(".json"))
                        .map(name -> name.substring(0, name.length() - 5))
                        .sorted(String.CASE_INSENSITIVE_ORDER)
                        .toList();
            }
        } catch (IOException exception) {
            return List.of();
        }
    }

    public static Result capture(String name) {
        String safe = sanitize(name);
        if (safe.isBlank()) return new Result(false, "Profile name is empty.", 0);
        List<GunPackProfile.Entry> entries = new ArrayList<>();
        for (GunPlatform platform : GunPlatform.values()) {
            for (GunPackManager.InstalledPack pack : GunPackManager.installed(platform)) {
                if (pack.enabled()) entries.add(new GunPackProfile.Entry(platform, pack.fileName()));
            }
        }
        GunPackProfile profile = new GunPackProfile(safe, entries);
        Path target = profilePath(safe);
        Path temporary = target.resolveSibling(target.getFileName() + ".tmp");
        try {
            GunPackPaths.ensureDirectories();
            try (Writer writer = Files.newBufferedWriter(temporary, StandardCharsets.UTF_8)) {
                GSON.toJson(profile, writer);
            }
            try {
                Files.move(temporary, target, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
            } catch (IOException unsupportedAtomicMove) {
                Files.move(temporary, target, StandardCopyOption.REPLACE_EXISTING);
            }
            return new Result(true, "Profile saved: " + safe, entries.size());
        } catch (IOException exception) {
            try { Files.deleteIfExists(temporary); } catch (IOException ignored) { }
            return new Result(false, exception.getMessage(), 0);
        }
    }

    public static Optional<GunPackProfile> load(String name) {
        Path path = profilePath(sanitize(name));
        if (!Files.isRegularFile(path)) return Optional.empty();
        try (Reader reader = Files.newBufferedReader(path, StandardCharsets.UTF_8)) {
            GunPackProfile profile = GSON.fromJson(reader, GunPackProfile.class);
            return Optional.ofNullable(profile);
        } catch (IOException | JsonParseException exception) {
            return Optional.empty();
        }
    }

    public static Result apply(String name) {
        Optional<GunPackProfile> loaded = load(name);
        if (loaded.isEmpty()) return new Result(false, "Profile was not found or is invalid.", 0);
        GunPackProfile profile = loaded.get();
        int changed = 0;
        List<String> failures = new ArrayList<>();

        for (GunPlatform platform : GunPlatform.values()) {
            List<String> wanted = profile.entries().stream()
                    .filter(entry -> entry.platform() == platform)
                    .map(GunPackProfile.Entry::fileName)
                    .toList();
            for (GunPackManager.InstalledPack pack : GunPackManager.installed(platform)) {
                boolean shouldEnable = wanted.stream().anyMatch(nameInProfile -> nameInProfile.equalsIgnoreCase(pack.fileName()));
                if (pack.enabled() != shouldEnable) {
                    GunPackManager.OperationResult result = GunPackManager.setEnabled(pack, shouldEnable);
                    if (result.success()) changed++;
                    else failures.add(platform.id() + ":" + pack.fileName() + " (" + result.message() + ")");
                }
            }
            for (String wantedName : wanted) {
                boolean exists = GunPackManager.installed(platform).stream()
                        .anyMatch(pack -> pack.fileName().equalsIgnoreCase(wantedName));
                if (!exists) failures.add(platform.id() + ":" + wantedName + " (not installed)");
            }
        }

        if (!failures.isEmpty()) {
            return new Result(false, "Applied with problems: " + String.join(", ", failures), changed);
        }
        return new Result(true, "Profile applied. Restart or reload may be required.", changed);
    }

    public static Result delete(String name) {
        String safe = sanitize(name);
        try {
            boolean deleted = Files.deleteIfExists(profilePath(safe));
            return new Result(deleted, deleted ? "Profile deleted: " + safe : "Profile was not found.", deleted ? 1 : 0);
        } catch (IOException exception) {
            return new Result(false, exception.getMessage(), 0);
        }
    }

    private static Path profilePath(String safeName) {
        return GunPackPaths.profiles().resolve(safeName + ".json");
    }

    private static String sanitize(String name) {
        if (name == null) return "";
        String clean = name.trim().toLowerCase(Locale.ROOT).replaceAll("[^a-z0-9._-]+", "_");
        while (clean.startsWith(".")) clean = clean.substring(1);
        return clean.length() > 64 ? clean.substring(0, 64) : clean;
    }

    public record Result(boolean success, String message, int changedCount) { }
    private GunPackProfileManager() { }
}
