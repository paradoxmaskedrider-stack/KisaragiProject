package com.github.kisaragimikoto.tacticalbackpack.compat.gunpack;

import net.minecraftforge.fml.loading.FMLPaths;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/** Keeps downloaded content isolated and exposes per-platform deployment folders. */
public final class GunPackPaths {
    private static final Path ROOT = FMLPaths.GAMEDIR.get().resolve("tacticalbackpack").resolve("gunpacks");

    public static Path root() { return ROOT; }
    public static Path downloads() { return ROOT.resolve("downloads"); }
    public static Path backups() { return ROOT.resolve("backups"); }
    public static Path quarantine() { return ROOT.resolve("quarantine"); }
    public static Path recycle() { return ROOT.resolve("recycle"); }
    public static Path profiles() { return ROOT.resolve("profiles"); }
    public static Path imports() { return ROOT.resolve("imports"); }
    public static Path autoImportDirectory() { return imports().resolve("auto"); }

    public static Path importDirectory(GunPlatform platform) {
        return imports().resolve(platform.id());
    }

    public static Path backupDirectory(GunPlatform platform) {
        return backups().resolve(platform.id());
    }

    public static Path recycleDirectory(GunPlatform platform) {
        return recycle().resolve(platform.id());
    }

    public static Path installDirectory(GunPlatform platform) {
        // Adapter-owned folders prevent one ecosystem's pack from being copied into another.
        return ROOT.resolve("installed").resolve(platform.id());
    }

    public static void ensureDirectories() throws IOException {
        Files.createDirectories(downloads());
        Files.createDirectories(backups());
        Files.createDirectories(quarantine());
        Files.createDirectories(recycle());
        Files.createDirectories(profiles());
        Files.createDirectories(imports());
        Files.createDirectories(autoImportDirectory());
        for (GunPlatform platform : GunPlatform.values()) {
            Files.createDirectories(installDirectory(platform));
            Files.createDirectories(backupDirectory(platform));
            Files.createDirectories(recycleDirectory(platform));
            Files.createDirectories(importDirectory(platform));
        }
    }

    private GunPackPaths() {}
}
