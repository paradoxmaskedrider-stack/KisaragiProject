package com.github.kisaragimikoto.tacticalbackpack.compat;

import net.minecraftforge.fml.ModList;

public class ModCompatRegistry {

    public static boolean EPICFIGHT;
    public static boolean NIGHTFALL;
    public static boolean TCONSTRUCT;
    public static boolean TICEX;
    public static boolean RE_AVARITIA;
    public static boolean CCTWEAKED;
    public static boolean CREATE;
    public static boolean FARMERS_DELIGHT;
    public static boolean MEKANISM;

    public static void init() {

        EPICFIGHT = ModList.get().isLoaded("epicfight");
        NIGHTFALL = ModList.get().isLoaded("epicfight_nightfall")
                || ModList.get().isLoaded("epicfightnightfall")
                || ModList.get().isLoaded("nightfall")
                || ModList.get().isLoaded("efn");
        TCONSTRUCT = ModList.get().isLoaded("tconstruct");
        TICEX = ModList.get().isLoaded("ticex");
        RE_AVARITIA = ModList.get().isLoaded("reavaritia");
        CCTWEAKED = ModList.get().isLoaded("computercraft");
        CREATE = ModList.get().isLoaded("create");
        FARMERS_DELIGHT = ModList.get().isLoaded("farmersdelight");
        MEKANISM = ModList.get().isLoaded("mekanism");
    }
}