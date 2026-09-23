package com.github.kisaragimikoto.tacticalbackpack.compat.nightfall;

import net.minecraftforge.fml.ModList;

import java.util.List;

/**
 * Optional EpicFight-Nightfall detection without linking against Nightfall classes.
 * Several ids are accepted because addon distributions have used different ids.
 */
public final class NightfallIntegration {
    private static final List<String> CANDIDATE_MOD_IDS = List.of(
            "epicfight_nightfall",
            "epicfight-nightfall",
            "epicfightnightfall",
            "nightfall",
            "efn"
    );

    private static String detectedModId = "";

    private NightfallIntegration() {}

    public static void init() {
        detectedModId = CANDIDATE_MOD_IDS.stream()
                .filter(id -> ModList.get().isLoaded(id))
                .findFirst()
                .orElse("");
    }

    public static boolean isLoaded() {
        return !detectedModId.isEmpty();
    }

    public static String detectedModId() {
        return detectedModId;
    }
}
