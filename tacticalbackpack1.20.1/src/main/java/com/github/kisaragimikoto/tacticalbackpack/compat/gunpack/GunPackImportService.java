package com.github.kisaragimikoto.tacticalbackpack.compat.gunpack;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

/** Safely imports user-provided local ZIP/JAR packs from per-platform inbox folders. */
public final class GunPackImportService {
    public static List<ImportCandidate> scan(GunPlatform platform) {
        Path directory = GunPackPaths.importDirectory(platform);
        if (!Files.isDirectory(directory)) return List.of();
        List<ImportCandidate> result = new ArrayList<>();
        try (var stream = Files.list(directory)) {
            stream.filter(Files::isRegularFile)
                    .filter(path -> isPackFile(path.getFileName().toString()))
                    .forEach(path -> {
                        try { result.add(new ImportCandidate(platform, path, Files.size(path))); }
                        catch (IOException ignored) { }
                    });
        } catch (IOException ignored) { }
        result.sort(Comparator.comparing(candidate -> candidate.path().getFileName().toString()));
        return List.copyOf(result);
    }


    public static List<AutoImportCandidate> scanAutoInbox() {
        Path directory = GunPackPaths.autoImportDirectory();
        if (!Files.isDirectory(directory)) return List.of();
        List<AutoImportCandidate> result = new ArrayList<>();
        try (var stream = Files.list(directory)) {
            stream.filter(Files::isRegularFile)
                    .filter(path -> isPackFile(path.getFileName().toString()))
                    .forEach(path -> {
                        try {
                            result.add(new AutoImportCandidate(path, Files.size(path), GunPackPlatformDetector.detect(path)));
                        } catch (IOException ignored) { }
                    });
        } catch (IOException ignored) { }
        result.sort(Comparator.comparing(candidate -> candidate.path().getFileName().toString()));
        return List.copyOf(result);
    }

    public static ImportResult importAutoDetected() {
        int imported = 0;
        int failed = 0;
        List<String> messages = new ArrayList<>();
        for (AutoImportCandidate candidate : scanAutoInbox()) {
            if (candidate.detection().platform().isEmpty()) {
                failed++;
                messages.add(candidate.path().getFileName() + ": " + candidate.detection().message());
                continue;
            }
            ImportResult result = importOne(new ImportCandidate(candidate.detection().platform().get(), candidate.path(), candidate.sizeBytes()));
            if (result.success()) imported++; else failed++;
            messages.add(result.message());
        }
        if (imported == 0 && failed == 0) return new ImportResult(true, 0, 0, "No local packs found in " + GunPackPaths.autoImportDirectory());
        return new ImportResult(failed == 0, imported, failed, "Auto-imported " + imported + ", unresolved/failed " + failed + ". " + String.join(" | ", messages));
    }

    public static ImportResult importAll(GunPlatform platform) {
        int imported = 0;
        int failed = 0;
        List<String> messages = new ArrayList<>();
        for (ImportCandidate candidate : scan(platform)) {
            ImportResult result = importOne(candidate);
            if (result.success()) imported++; else failed++;
            messages.add(result.message());
        }
        if (imported == 0 && failed == 0) return new ImportResult(true, 0, 0, "No local packs found in " + GunPackPaths.importDirectory(platform));
        return new ImportResult(failed == 0, imported, failed,
                "Imported " + imported + ", failed " + failed + ". " + String.join(" | ", messages));
    }

    public static ImportResult importOne(ImportCandidate candidate) {
        if (candidate == null || !Files.isRegularFile(candidate.path())) return new ImportResult(false, 0, 1, "Import file was not found.");
        var validation = GunPackFileValidator.validate(candidate.path(), "");
        if (!validation.valid()) {
            try {
                GunPackPaths.ensureDirectories();
                Path target = uniqueTarget(GunPackPaths.quarantine(), candidate.path().getFileName().toString());
                Files.move(candidate.path(), target, StandardCopyOption.REPLACE_EXISTING);
                return new ImportResult(false, 0, 1, validation.message() + " Moved to quarantine.");
            } catch (IOException exception) {
                return new ImportResult(false, 0, 1, validation.message() + " Quarantine failed: " + exception.getMessage());
            }
        }
        try {
            GunPackPaths.ensureDirectories();
            Path deployDir = GunPackPaths.deploymentDirectory(candidate.platform(), candidate.path().getFileName().toString());
            Files.createDirectories(deployDir);
            Path target = deployDir.resolve(candidate.path().getFileName());
            if (Files.exists(target)) {
                String stamp = Long.toString(Instant.now().toEpochMilli());
                Files.move(target, GunPackPaths.backupDirectory(candidate.platform()).resolve(stamp + "-" + target.getFileName()), StandardCopyOption.REPLACE_EXISTING);
            }
            Files.move(candidate.path(), target, StandardCopyOption.REPLACE_EXISTING);
            GunPackInstallRegistry.recordImported(candidate.platform(), target);
            return new ImportResult(true, 1, 0, "Imported " + target.getFileName() + " for " + candidate.platform().displayName() + ". Restart or reload may be required.");
        } catch (IOException exception) {
            return new ImportResult(false, 0, 1, "Import failed: " + exception.getMessage());
        }
    }

    private static Path uniqueTarget(Path directory, String fileName) {
        Path target = directory.resolve(fileName);
        if (!Files.exists(target)) return target;
        return directory.resolve(Instant.now().toEpochMilli() + "-" + fileName);
    }

    private static boolean isPackFile(String name) {
        String lower = name.toLowerCase(Locale.ROOT);
        return lower.endsWith(".zip") || lower.endsWith(".jar");
    }

    public record ImportCandidate(GunPlatform platform, Path path, long sizeBytes) { }
    public record AutoImportCandidate(Path path, long sizeBytes, GunPackPlatformDetector.Detection detection) { }
    public record ImportResult(boolean success, int imported, int failed, String message) { }
    private GunPackImportService() { }
}
