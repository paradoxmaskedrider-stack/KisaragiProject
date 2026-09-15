package com.github.kisaragimikoto.tacticalbackpack.compat.gunpack;

import java.util.Locale;
import java.util.Optional;

/** Gun ecosystem handled by the unified pack manager. */
public enum GunPlatform {
    TACZ("tacz", "TaCZ / Timeless and Classics Zero"),
    POINT_BLANK("pointblank", "Point Blank"),
    ELITE_X_QUALITY_GUNS("elite_x_quality_guns", "Elite X Quality Guns"),
    GUNSMITHLIB("gunsmithlib", "GunsmithLib");

    private final String id;
    private final String displayName;

    GunPlatform(String id, String displayName) {
        this.id = id;
        this.displayName = displayName;
    }

    public String id() { return id; }
    public String displayName() { return displayName; }

    /** Primary host MODID used for dependency checks. */
    public String modId() {
        return switch (this) {
            case TACZ -> "tacz";
            case POINT_BLANK -> "pointblank";
            case ELITE_X_QUALITY_GUNS -> "elite_x_quality_guns";
            case GUNSMITHLIB -> "gunsmithlib";
        };
    }

    public static Optional<GunPlatform> parse(String value) {
        if (value == null) return Optional.empty();
        String normalized = value.toLowerCase(Locale.ROOT).replace('-', '_').replace(' ', '_');
        for (GunPlatform platform : values()) {
            if (platform.id.equals(normalized) || platform.name().toLowerCase(Locale.ROOT).equals(normalized)) {
                return Optional.of(platform);
            }
        }
        if (normalized.equals("timeless_and_classics_zero")) return Optional.of(TACZ);
        if (normalized.equals("elite_x") || normalized.equals("elitex")) return Optional.of(ELITE_X_QUALITY_GUNS);
        return Optional.empty();
    }
}
