package com.github.kisaragimikoto.tacticalbackpack.compat.gunpack;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.EnumMap;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/** Heuristic detector used only to suggest an import target. It never executes pack content. */
public final class GunPackPlatformDetector {
    private static final int MAX_ENTRIES_TO_SCAN = 20_000;

    public static Detection detect(Path archive) {
        EnumMap<GunPlatform, Integer> scores = new EnumMap<>(GunPlatform.class);
        for (GunPlatform platform : GunPlatform.values()) scores.put(platform, 0);
        scoreText(archive.getFileName().toString(), scores, 8);

        try (InputStream input = Files.newInputStream(archive); ZipInputStream zip = new ZipInputStream(input)) {
            ZipEntry entry;
            int count = 0;
            while ((entry = zip.getNextEntry()) != null && count++ < MAX_ENTRIES_TO_SCAN) {
                scoreText(entry.getName(), scores, 2);
            }
        } catch (IOException exception) {
            return new Detection(Optional.empty(), 0, "Could not inspect archive: " + exception.getMessage(), Map.copyOf(scores));
        }

        GunPlatform best = null;
        int bestScore = 0;
        int second = 0;
        for (var item : scores.entrySet()) {
            int value = item.getValue();
            if (value > bestScore) {
                second = bestScore;
                bestScore = value;
                best = item.getKey();
            } else if (value > second) {
                second = value;
            }
        }
        if (best == null || bestScore < 4) {
            return new Detection(Optional.empty(), bestScore, "No reliable target detected.", Map.copyOf(scores));
        }
        if (bestScore - second < 3) {
            return new Detection(Optional.empty(), bestScore, "Detection was ambiguous; choose the target manually.", Map.copyOf(scores));
        }
        return new Detection(Optional.of(best), bestScore, "Detected " + best.displayName() + ".", Map.copyOf(scores));
    }

    private static void scoreText(String value, EnumMap<GunPlatform, Integer> scores, int weight) {
        String text = value.toLowerCase(Locale.ROOT).replace('\\', '/');
        add(scores, GunPlatform.TACZ, weight, text, "tacz", "timeless", "gunpack", "tacz_default_gun");
        add(scores, GunPlatform.POINT_BLANK, weight, text, "pointblank", "point_blank", "pbpack");
        add(scores, GunPlatform.ELITE_X_QUALITY_GUNS, weight, text, "elite_x", "elitex", "qualityguns", "quality_guns");
        add(scores, GunPlatform.GUNSMITHLIB, weight, text, "gunsmithlib", "gunsmith_lib", "gunsmith/");
    }

    private static void add(EnumMap<GunPlatform, Integer> scores, GunPlatform platform, int weight, String text, String... markers) {
        for (String marker : markers) {
            if (text.contains(marker)) scores.merge(platform, weight, Integer::sum);
        }
    }

    public record Detection(Optional<GunPlatform> platform, int confidence, String message, Map<GunPlatform, Integer> scores) { }
    private GunPackPlatformDetector() { }
}
