package com.github.kisaragimikoto.tacticalbackpack.compat.gunpack;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.time.Instant;

/** Shared backend for the in-game Gun Pack Manager screen. */
public final class GunPackManager {
    public static void init() {
        try { GunPackPaths.ensureDirectories(); }
        catch (IOException ignored) { }
        GunPackCatalogFile.reload();
    }

    public static List<InstalledPack> installed(GunPlatform platform) {
        Path directory = GunPackPaths.installDirectory(platform);
        if (!Files.isDirectory(directory)) return List.of();
        List<InstalledPack> result = new ArrayList<>();
        List<GunPackInstallRegistry.Entry> metadata = GunPackInstallRegistry.load();
        try (var stream = Files.list(directory)) {
            stream.filter(Files::isRegularFile)
                    .filter(path -> isPackFile(path.getFileName().toString()))
                    .forEach(path -> {
                        try {
                            String physical = path.getFileName().toString();
                            boolean enabled = !physical.toLowerCase(Locale.ROOT).endsWith(".disabled");
                            String logical = enabled ? physical : physical.substring(0, physical.length() - ".disabled".length());
                            Optional<GunPackInstallRegistry.Entry> meta = metadata.stream()
                                    .filter(entry -> entry.platform() == platform && (entry.fileName().equals(logical) || entry.fileName().equals(physical)))
                                    .findFirst();
                            result.add(new InstalledPack(platform, logical, path, Files.size(path), enabled,
                                    meta.map(GunPackInstallRegistry.Entry::id).orElse(""),
                                    meta.map(GunPackInstallRegistry.Entry::version).orElse("unknown")));
                        } catch (IOException ignored) { }
                    });
        } catch (IOException ignored) { }
        result.sort(Comparator.comparing(InstalledPack::fileName));
        return List.copyOf(result);
    }

    public static OperationResult setEnabled(InstalledPack pack, boolean enabled) {
        if (pack == null || pack.enabled() == enabled) return new OperationResult(true, "No change.");
        Path source = pack.path();
        String sourceName = source.getFileName().toString();
        Path target = enabled
                ? source.resolveSibling(sourceName.substring(0, sourceName.length() - ".disabled".length()))
                : source.resolveSibling(sourceName + ".disabled");
        try {
            if (Files.exists(target)) return new OperationResult(false, "Target file already exists.");
            Files.move(source, target, StandardCopyOption.ATOMIC_MOVE);
            GunPackInstallRegistry.setEnabled(pack.fileName(), pack.platform(), enabled);
            return new OperationResult(true, enabled ? "Enabled. Restart or reload may be required." : "Disabled. Restart or reload may be required.");
        } catch (IOException exception) {
            return new OperationResult(false, exception.getMessage());
        }
    }

    public static OperationResult uninstall(InstalledPack pack) {
        if (pack == null || !Files.isRegularFile(pack.path())) return new OperationResult(false, "Pack file was not found.");
        try {
            GunPackPaths.ensureDirectories();
            String stamp = Long.toString(Instant.now().toEpochMilli());
            Path target = GunPackPaths.recycleDirectory(pack.platform()).resolve(stamp + "-" + pack.path().getFileName());
            Files.move(pack.path(), target, StandardCopyOption.REPLACE_EXISTING);
            GunPackInstallRegistry.remove(pack.fileName(), pack.platform());
            return new OperationResult(true, "Moved to recycle. Restart or reload may be required.");
        } catch (IOException exception) {
            return new OperationResult(false, exception.getMessage());
        }
    }

    public static List<BackupPack> backups(GunPlatform platform) {
        Path directory = GunPackPaths.backupDirectory(platform);
        if (!Files.isDirectory(directory)) return List.of();
        List<BackupPack> result = new ArrayList<>();
        try (var stream = Files.list(directory)) {
            stream.filter(Files::isRegularFile)
                    .filter(path -> isPackFile(path.getFileName().toString()))
                    .forEach(path -> {
                        try { result.add(new BackupPack(platform, path.getFileName().toString(), path, Files.size(path))); }
                        catch (IOException ignored) { }
                    });
        } catch (IOException ignored) { }
        result.sort(Comparator.comparing(BackupPack::fileName).reversed());
        return List.copyOf(result);
    }

    public static OperationResult restoreBackup(BackupPack backup) {
        if (backup == null || !Files.isRegularFile(backup.path())) return new OperationResult(false, "Backup file was not found.");
        try {
            GunPackPaths.ensureDirectories();
            String raw = backup.fileName();
            int separator = raw.indexOf('-');
            String targetName = separator >= 0 && separator + 1 < raw.length() ? raw.substring(separator + 1) : raw;
            Path target = GunPackPaths.installDirectory(backup.platform()).resolve(targetName);
            if (Files.exists(target)) {
                String stamp = Long.toString(Instant.now().toEpochMilli());
                Files.move(target, GunPackPaths.backupDirectory(backup.platform()).resolve(stamp + "-" + target.getFileName()), StandardCopyOption.REPLACE_EXISTING);
            }
            Files.move(backup.path(), target, StandardCopyOption.REPLACE_EXISTING);
            return new OperationResult(true, "Backup restored. Restart or reload may be required.");
        } catch (IOException exception) {
            return new OperationResult(false, exception.getMessage());
        }
    }

    public static UpdateState updateState(GunPackDescriptor descriptor) {
        Optional<GunPackInstallRegistry.Entry> installed = GunPackInstallRegistry.find(descriptor.id(), descriptor.platform());
        if (installed.isEmpty()) return UpdateState.NOT_INSTALLED;
        return GunPackVersion.newerThan(descriptor.version(), installed.get().version()) ? UpdateState.UPDATE_AVAILABLE : UpdateState.CURRENT;
    }

    private static boolean isPackFile(String name) {
        String lower = name.toLowerCase(Locale.ROOT);
        return lower.endsWith(".zip") || lower.endsWith(".jar") || lower.endsWith(".zip.disabled") || lower.endsWith(".jar.disabled");
    }

    public enum UpdateState { NOT_INSTALLED, CURRENT, UPDATE_AVAILABLE }
    public record OperationResult(boolean success, String message) { }
    public record InstalledPack(GunPlatform platform, String fileName, Path path, long sizeBytes, boolean enabled, String id, String version) { }
    public record BackupPack(GunPlatform platform, String fileName, Path path, long sizeBytes) { }
    private GunPackManager() { }
}
