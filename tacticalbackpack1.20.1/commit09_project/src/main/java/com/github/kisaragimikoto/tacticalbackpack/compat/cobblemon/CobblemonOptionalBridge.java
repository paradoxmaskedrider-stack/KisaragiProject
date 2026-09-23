package com.github.kisaragimikoto.tacticalbackpack.compat.cobblemon;

import net.minecraftforge.fml.ModList;
import java.lang.reflect.Method;
import java.util.*;

/** Reflection-only probe: Forge 1.20.1 remains bootable without Cobblemon. */
public final class CobblemonOptionalBridge {
    private static final String[] PROBES = {
        "com.cobblemon.mod.common.Cobblemon",
        "com.cobblemon.mod.common.api.pokemon.PokemonSpecies"
    };
    public static boolean isAvailable() { return ModList.get().isLoaded("cobblemon") && probe(); }
    public static boolean probe() {
        try { for (String n: PROBES) Class.forName(n,false,CobblemonOptionalBridge.class.getClassLoader()); return true; }
        catch (Throwable ignored) { return false; }
    }
    private CobblemonOptionalBridge() {}
}
