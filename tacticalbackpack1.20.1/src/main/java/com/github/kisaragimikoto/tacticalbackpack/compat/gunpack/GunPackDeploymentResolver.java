package com.github.kisaragimikoto.tacticalbackpack.compat.gunpack;

import net.minecraftforge.fml.loading.FMLPaths;

import java.nio.file.Path;
import java.util.Locale;

/** Resolves the real game directory used by each supported gun ecosystem. */
public final class GunPackDeploymentResolver {
    public static Path deploymentDirectory(GunPlatform platform, String fileName) {
        Path gameDir = FMLPaths.GAMEDIR.get();
        String lower = fileName == null ? "" : fileName.toLowerCase(Locale.ROOT);
        if (lower.endsWith(".disabled")) {
            lower = lower.substring(0, lower.length() - ".disabled".length());
        }

        return switch (platform) {
            case TACZ -> gameDir.resolve("tacz");
            case POINT_BLANK -> gameDir.resolve("pointblank");
            case ELITE_X_QUALITY_GUNS -> lower.endsWith(".jar")
                    ? gameDir.resolve("mods")
                    : gameDir.resolve("tacz");
            case GUNSMITHLIB -> lower.endsWith(".jar")
                    ? gameDir.resolve("mods")
                    : gameDir.resolve("tacz");
        };
    }

    private GunPackDeploymentResolver() { }
}
