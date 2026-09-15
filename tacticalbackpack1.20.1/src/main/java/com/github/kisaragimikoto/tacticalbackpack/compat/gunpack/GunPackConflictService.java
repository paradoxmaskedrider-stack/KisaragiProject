package com.github.kisaragimikoto.tacticalbackpack.compat.gunpack;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/** Read-only collision scan for enabled ZIP/JAR gun packs. */
public final class GunPackConflictService {
    public static ConflictReport scan(GunPlatform platform) {
        Map<String, List<Path>> owners = new HashMap<>();
        List<String> errors = new ArrayList<>();
        Path directory = GunPackPaths.installDirectory(platform);
        if (!Files.isDirectory(directory)) return new ConflictReport(platform, List.of(), List.of());

        try (var stream = Files.list(directory)) {
            stream.filter(Files::isRegularFile)
                    .filter(GunPackConflictService::enabledArchive)
                    .forEach(path -> collect(path, owners, errors));
        } catch (IOException exception) {
            errors.add(exception.getMessage());
        }

        List<Conflict> conflicts = owners.entrySet().stream()
                .filter(entry -> entry.getValue().size() > 1)
                .map(entry -> new Conflict(entry.getKey(), List.copyOf(entry.getValue())))
                .sorted(java.util.Comparator.comparing(Conflict::internalPath))
                .toList();
        return new ConflictReport(platform, conflicts, List.copyOf(errors));
    }

    private static void collect(Path path, Map<String, List<Path>> owners, List<String> errors) {
        try (ZipFile zip = new ZipFile(path.toFile())) {
            Set<String> seen = new TreeSet<>();
            var entries = zip.entries();
            while (entries.hasMoreElements()) {
                ZipEntry entry = entries.nextElement();
                if (entry.isDirectory()) continue;
                String normalized = entry.getName().replace('\\', '/').toLowerCase(Locale.ROOT);
                if (!relevant(normalized) || !seen.add(normalized)) continue;
                owners.computeIfAbsent(normalized, ignored -> new ArrayList<>()).add(path);
            }
        } catch (IOException exception) {
            errors.add(path.getFileName() + ": " + exception.getMessage());
        }
    }

    private static boolean relevant(String path) {
        return path.endsWith(".json") || path.endsWith(".png") || path.endsWith(".ogg")
                || path.endsWith(".mcmeta") || path.endsWith(".lang");
    }

    private static boolean enabledArchive(Path path) {
        String name = path.getFileName().toString().toLowerCase(Locale.ROOT);
        return (name.endsWith(".zip") || name.endsWith(".jar")) && !name.endsWith(".disabled");
    }

    public record Conflict(String internalPath, List<Path> owners) { }
    public record ConflictReport(GunPlatform platform, List<Conflict> conflicts, List<String> errors) { }
    private GunPackConflictService() { }
}
