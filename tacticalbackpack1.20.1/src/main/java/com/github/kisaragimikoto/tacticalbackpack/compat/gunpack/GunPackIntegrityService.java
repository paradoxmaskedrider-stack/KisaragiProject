package com.github.kisaragimikoto.tacticalbackpack.compat.gunpack;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HexFormat;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/** Read-only integrity and duplicate audit for installed and staged gun packs. */
public final class GunPackIntegrityService {
    public static AuditReport auditAll() {
        List<AuditEntry> entries = new ArrayList<>();
        for (GunPlatform platform : GunPlatform.values()) {
            collectManagedInstalled(entries, platform);
            collect(entries, platform, Location.BACKUP, GunPackPaths.backupDirectory(platform));
            collect(entries, platform, Location.IMPORT, GunPackPaths.importDirectory(platform));
        }
        collect(entries, null, Location.AUTO_IMPORT, GunPackPaths.autoImportDirectory());

        Map<String, Integer> hashCounts = new HashMap<>();
        for (AuditEntry entry : entries) {
            if (!entry.sha256().isBlank()) hashCounts.merge(entry.sha256(), 1, Integer::sum);
        }

        List<AuditEntry> normalized = entries.stream()
                .map(entry -> entry.withDuplicate(hashCounts.getOrDefault(entry.sha256(), 0) > 1))
                .sorted(Comparator.comparing((AuditEntry e) -> e.platform() == null ? "zzzz" : e.platform().name())
                        .thenComparing(e -> e.path().toString()))
                .toList();

        long duplicateFiles = normalized.stream().filter(AuditEntry::duplicate).count();
        long invalidFiles = normalized.stream().filter(entry -> !entry.valid()).count();
        return new AuditReport(List.copyOf(normalized), duplicateFiles, invalidFiles);
    }

    public static AuditReport auditPlatform(GunPlatform platform) {
        AuditReport all = auditAll();
        List<AuditEntry> filtered = all.entries().stream()
                .filter(entry -> entry.platform() == platform)
                .toList();
        long duplicateFiles = filtered.stream().filter(AuditEntry::duplicate).count();
        long invalidFiles = filtered.stream().filter(entry -> !entry.valid()).count();
        return new AuditReport(filtered, duplicateFiles, invalidFiles);
    }

    private static void collectManagedInstalled(List<AuditEntry> output, GunPlatform platform) {
        for (GunPackManager.InstalledPack pack : GunPackManager.installed(platform)) {
            if (Files.isRegularFile(pack.path())) {
                output.add(inspect(platform, Location.INSTALLED, pack.path()));
            }
        }
    }

    private static void collect(List<AuditEntry> output, GunPlatform platform, Location location, Path directory) {
        if (!Files.isDirectory(directory)) return;
        try (var stream = Files.list(directory)) {
            stream.filter(Files::isRegularFile)
                    .filter(path -> isPackFile(path.getFileName().toString()))
                    .forEach(path -> output.add(inspect(platform, location, path)));
        } catch (IOException ignored) { }
    }

    private static AuditEntry inspect(GunPlatform platform, Location location, Path path) {
        try {
            long size = Files.size(path);
            String hash = sha256(path);
            var validation = GunPackFileValidator.validate(path, "");
            return new AuditEntry(platform, location, path, size, hash, validation.valid(), validation.message(), false);
        } catch (IOException exception) {
            return new AuditEntry(platform, location, path, 0L, "", false, exception.getMessage(), false);
        }
    }

    private static String sha256(Path path) throws IOException {
        final MessageDigest digest;
        try {
            digest = MessageDigest.getInstance("SHA-256");
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException("SHA-256 is unavailable", exception);
        }
        try (InputStream input = Files.newInputStream(path)) {
            byte[] buffer = new byte[64 * 1024];
            int read;
            while ((read = input.read(buffer)) >= 0) {
                if (read > 0) digest.update(buffer, 0, read);
            }
        }
        return HexFormat.of().formatHex(digest.digest());
    }

    private static boolean isPackFile(String name) {
        String lower = name.toLowerCase(Locale.ROOT);
        return lower.endsWith(".zip") || lower.endsWith(".jar")
                || lower.endsWith(".zip.disabled") || lower.endsWith(".jar.disabled");
    }

    public enum Location { INSTALLED, BACKUP, IMPORT, AUTO_IMPORT }

    public record AuditEntry(GunPlatform platform, Location location, Path path, long sizeBytes,
                             String sha256, boolean valid, String message, boolean duplicate) {
        public AuditEntry withDuplicate(boolean value) {
            return new AuditEntry(platform, location, path, sizeBytes, sha256, valid, message, value);
        }

        public String shortHash() {
            return sha256.length() <= 12 ? sha256 : sha256.substring(0, 12);
        }
    }

    public record AuditReport(List<AuditEntry> entries, long duplicateFiles, long invalidFiles) { }

    private GunPackIntegrityService() { }
}
