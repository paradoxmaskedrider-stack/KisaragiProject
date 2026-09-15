package com.github.kisaragimikoto.tacticalbackpack.compat.cobblemon;

import com.github.kisaragimikoto.tacticalbackpack.world.BackpackBiomeZoneType;

import java.util.List;

public class BackpackCobblemonSpawnTable {

    public static List<String> getPokemonForZone(BackpackBiomeZoneType zoneType) {
        return switch (zoneType) {
            case PLAINS -> List.of("pidgey", "rattata", "eevee");
            case FOREST -> List.of("caterpie", "weedle", "pikachu", "bulbasaur");
            case DESERT -> List.of("sandshrew", "trapinch", "cacnea");
            case SNOW -> List.of("swinub", "snorunt", "sneasel");
            case OCEAN -> List.of("magikarp", "tentacool", "squirtle");
            case CAVE -> List.of("zubat", "geodude", "onix");
            case NETHER -> List.of("houndour", "magmar", "slugma");
            case END -> List.of("abra", "gastly", "beldum");
        };
    }
}