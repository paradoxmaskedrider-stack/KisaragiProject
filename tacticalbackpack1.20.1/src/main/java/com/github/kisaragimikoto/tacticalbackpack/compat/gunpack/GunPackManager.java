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
import java.util.Optional;

/** Shared backend for the in-game Gun Pack Manager screen. */
public final class GunPackManager {
    public static void init() {
        try {
            GunPackPaths.ensureDirectories();
            migrateLegacyInstalls();
        } catch (IOException ignored) { }
        GunPackCatalogFile.reload();
    }

    /**
     * Lists only files registered as managed by TacticalBackpack.
     * This prevents unrelated files in mods/tacz/pointblank from appearing in this manager.
     */
    public static List<InstalledPack> installed(GunPlatform platform) {
        List<InstalledPack> result = new ArrayList<>();
        for (GunPackInstallRegistry.Entry entry : GunPackInstallRegistry.load()) {
            if (entry.platform() != platform) continue;
            String logicalName = entry.fileName();
            Path directory = GunPackPaths.deploymentDirectory(platform, logicalName);
            Path enabledPath = directory.resolve(logicalName);
            Path disabledPath = directory.resolve(logicalName + ".disabled");
            Path path = Files.isRegularFile(enabledPath) ? enabledPath : Files.isRegularFile(disabledPath) ? disabledPath : null;
            if (path == null) continue;
            try {
                boolean enabled = !path.getFileName().toString().toLowerCase(Locale.ROOT).endsWith(".disabled");
                result.add(new InstalledPack(platform, logicalName, path, Files.size(path), enabled, entry.id(), entry.version()));
            } catch (IOException ignored) { }
        }
        result.sort(Comparator.comparing(InstalledPack::fileName));
        return List.copyOf(result);
    }

    public static OperationResult setEnabled(InstalledPack pack, boolean enabled) {
        if (pack == null || pack.enabled() == enabled) return new OperationResult(true, "No change.");
        Path source = pack.path();
        String sourceName = source.getFileName().toString();
        Path target;
        if (enabled) {
            if (!sourceName.endsWith(".disabled")) return new OperationResult(false, "Disabled suffix was not found.");
            target = source.resolveSibling(sourceName.substring(0, sourceName.length() - ".disabled".length()));
        } else {
            target = source.resolveSibling(sourceName + ".disabled");
        }
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
            Path deployDir = GunPackPaths.deploymentDirectory(backup.platform(), targetName);
            Files.createDirectories(deployDir);
            Path target = deployDir.resolve(targetName);
            if (Files.exists(target)) {
                String stamp = Long.toString(Instant.now().toEpochMilli());
                Files.move(target, GunPackPaths.backupDirectory(backup.platform()).resolve(stamp + "-" + target.getFileName()), StandardCopyOption.REPLACE_EXISTING);
            }
            Files.move(backup.path(), target, StandardCopyOption.REPLACE_EXISTING);
            GunPackInstallRegistry.recordImported(backup.platform(), target);
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

    /** Moves files installed by commit06/07 from the manager-private folder into the actual host-mod folder. */
    private static void migrateLegacyInstalls() throws IOException {
        for (GunPlatform platform : GunPlatform.values()) {
            Path legacy = GunPackPaths.legacyInstallDirectory(platform);
            if (!Files.isDirectory(legacy)) continue;
            try (var stream = Files.list(legacy)) {
                for (Path source : stream.filter(Files::isRegularFile).filter(path -> isPackFile(path.getFileName().toString())).toList()) {
                    String fileName = source.getFileName().toString();
                    String logicalName = fileName.endsWith(".disabled")
                            ? fileName.substring(0, fileName.length() - ".disabled".length()) : fileName;
                    Path deployDir = GunPackPaths.deploymentDirectory(platform, logicalName);
                    Files.createDirectories(deployDir);
                    Path target = deployDir.resolve(fileName);
                    if (Files.exists(target)) {
                        Path backup = GunPackPaths.backupDirectory(platform)
                                .resolve(Instant.now().toEpochMilli() + "-" + target.getFileName());
                        Files.move(target, backup, StandardCopyOption.REPLACE_EXISTING);
                    }
                    Files.move(source, target, StandardCopyOption.REPLACE_EXISTING);
                }
            }
        }
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
