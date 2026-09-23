package com.github.kisaragimikoto.tacticalbackpack.config;

import net.minecraftforge.common.ForgeConfigSpec;

public class BackpackConfig {

    public static final ForgeConfigSpec SPEC;

    public static final ForgeConfigSpec.IntValue BACKPACK_SIZE;
    public static final ForgeConfigSpec.BooleanValue ENABLE_ELYTRA;
    public static final ForgeConfigSpec.BooleanValue ENABLE_EPICFIGHT;
    public static final ForgeConfigSpec.BooleanValue ENABLE_TCONSTRUCT;
    public static final ForgeConfigSpec.BooleanValue ENABLE_MEKANISM;
    public static final ForgeConfigSpec.BooleanValue ENABLE_AE2;
    public static final ForgeConfigSpec.IntValue AE2_TRANSFER_LIMIT;
    public static final ForgeConfigSpec.IntValue AE2_WIRELESS_SESSION_TICKS;
    public static final ForgeConfigSpec.IntValue COBBLEMON_INITIAL_BOXES;
    public static final ForgeConfigSpec.IntValue COBBLEMON_SLOTS_PER_BOX;
    public static final ForgeConfigSpec.LongValue COBBLEMON_MAX_POKEMON;
    public static final ForgeConfigSpec.BooleanValue ENABLE_GUN_PACK_MANAGER;
    public static final ForgeConfigSpec.BooleanValue ALLOW_GUN_PACK_DOWNLOADS;

    static {
        ForgeConfigSpec.Builder builder = new ForgeConfigSpec.Builder();

        builder.push("Tactical Backpack");

        BACKPACK_SIZE = builder
                .comment("Backpack size")
                .defineInRange("size", 54, 9, 50000);

        ENABLE_ELYTRA = builder
                .define("enableElytra", true);

        ENABLE_EPICFIGHT = builder
                .define("enableEpicFight", true);

        ENABLE_TCONSTRUCT = builder
                .define("enableTConstruct", true);

        ENABLE_MEKANISM = builder
                .define("enableMekanism", true);

        ENABLE_AE2 = builder
                .comment("Enable Applied Energistics 2 integration when AE2 is installed")
                .define("enableAE2", true);

        AE2_TRANSFER_LIMIT = builder
                .comment("Maximum item count moved by one backpack-to-ME bulk transfer. 0 means unlimited.")
                .defineInRange("ae2TransferLimit", 4096, 0, 1_000_000);

        AE2_WIRELESS_SESSION_TICKS = builder
                .comment("Server-side lifetime of an AE2 wireless backpack session in ticks.")
                .defineInRange("ae2WirelessSessionTicks", 1200, 20, 72_000);

        builder.pop();

        builder.push("Cobblemon Backpack PC");
        COBBLEMON_INITIAL_BOXES = builder
                .comment("Number of boxes created for a new backpack PC")
                .defineInRange("initialBoxes", 30, 1, 1_000_000);
        COBBLEMON_SLOTS_PER_BOX = builder
                .comment("Pokemon slots per virtual box")
                .defineInRange("slotsPerBox", 30, 1, 10_000);
        COBBLEMON_MAX_POKEMON = builder
                .comment("Maximum stored Pokemon per backpack. Set to 0 or a negative value for no configured limit. Data is still paged and loaded lazily.")
                .defineInRange("maxPokemon", 0L, Long.MIN_VALUE, Long.MAX_VALUE);
        builder.pop();

        builder.push("Gun Pack Manager");
        ENABLE_GUN_PACK_MANAGER = builder.define("enabled", true);
        ALLOW_GUN_PACK_DOWNLOADS = builder
                .comment("Allows explicit HTTPS downloads requested from the in-game manager. Disabled means local import/listing only.")
                .define("allowDownloads", false);
        builder.pop();
        SPEC = builder.build();
    }
}