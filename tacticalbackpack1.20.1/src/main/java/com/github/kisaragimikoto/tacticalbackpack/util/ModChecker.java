package com.github.kisaragimikoto.tacticalbackpack.util;

import net.minecraftforge.fml.ModList;

/**
 * Central optional-mod detection helper.
 * Keeps compatibility classes from duplicating Forge ModList checks.
 */
public final class ModChecker {
    public static boolean isLoaded(String modId) {
        return modId != null && !modId.isBlank() && ModList.get().isLoaded(modId);
    }

    public static boolean isCuriosLoaded() {
        return isLoaded("curios");
    }

    public static boolean isTaczLoaded() {
        return isLoaded("tacz");
    }

    public static boolean isPointBlankLoaded() {
        return isLoaded("pointblank");
    }

    public static boolean isGunsmithLibLoaded() {
        return isLoaded("gunsmithlib");
    }

    public static boolean isEliteXQualityGunsLoaded() {
        return isLoaded("elite_x_quality_guns");
    }

    private ModChecker() { }
}
