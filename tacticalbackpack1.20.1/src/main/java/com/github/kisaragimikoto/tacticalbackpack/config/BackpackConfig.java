package com.github.kisaragimikoto.tacticalbackpack.config;

import net.minecraftforge.common.ForgeConfigSpec;

public class BackpackConfig {

    public static final ForgeConfigSpec SPEC;

    public static final ForgeConfigSpec.IntValue BACKPACK_SIZE;
    public static final ForgeConfigSpec.IntValue MAX_STORAGE_SLOTS;
    public static final ForgeConfigSpec.IntValue SIZE_UPGRADE_1_BONUS;
    public static final ForgeConfigSpec.IntValue SIZE_UPGRADE_2_BONUS;
    public static final ForgeConfigSpec.BooleanValue ENABLE_AUTO_OVERFLOW;
    public static final ForgeConfigSpec.IntValue OVERFLOW_KEEP_FREE_SLOTS;
    public static final ForgeConfigSpec.BooleanValue OVERFLOW_TO_AE2;
    public static final ForgeConfigSpec.BooleanValue OVERFLOW_TO_ITEM_HANDLER;
    public static final ForgeConfigSpec.IntValue OVERFLOW_CHECK_INTERVAL_TICKS;
    public static final ForgeConfigSpec.BooleanValue OVERFLOW_SKIP_NESTED_BACKPACKS;
    public static final ForgeConfigSpec.BooleanValue ENABLE_PICKUP_OVERFLOW;
    public static final ForgeConfigSpec.BooleanValue PICKUP_OVERFLOW_REQUIRES_AUTO_PICKUP_MODULE;
    public static final ForgeConfigSpec.BooleanValue PICKUP_OVERFLOW_TO_AE2;
    public static final ForgeConfigSpec.BooleanValue PICKUP_OVERFLOW_TO_EXTERNAL_STORAGE;
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
                .comment("Base internal backpack storage slots before capacity upgrades.")
                .defineInRange("size", 54, 9, 50000);

        MAX_STORAGE_SLOTS = builder
                .comment("Maximum internal storage slots after all capacity upgrades. The UI will page large inventories.")
                .defineInRange("maxStorageSlots", 50000, 9, 50000);

        SIZE_UPGRADE_1_BONUS = builder
                .comment("Storage slots added by one Size Upgrade I attachment.")
                .defineInRange("sizeUpgrade1Bonus", 27, 0, 50000);

        SIZE_UPGRADE_2_BONUS = builder
                .comment("Storage slots added by one Size Upgrade II attachment.")
                .defineInRange("sizeUpgrade2Bonus", 54, 0, 50000);

        ENABLE_AUTO_OVERFLOW = builder
                .comment("Master switch for automatic overflow routing. Provider-specific routing is applied only when supported and linked.")
                .define("enableAutoOverflow", true);

        OVERFLOW_KEEP_FREE_SLOTS = builder
                .comment("Try to keep at least this many internal slots free by routing excess items to linked storage providers.")
                .defineInRange("overflowKeepFreeSlots", 9, 0, 50000);

        OVERFLOW_TO_AE2 = builder
                .comment("Allow a linked AE2 network to be used as an automatic overflow destination.")
                .define("overflowToAE2", true);

        OVERFLOW_TO_ITEM_HANDLER = builder
                .comment("Allow a linked Forge IItemHandler block (including many Create/Mekanism storage blocks) as an overflow destination.")
                .define("overflowToItemHandler", true);

        OVERFLOW_CHECK_INTERVAL_TICKS = builder
                .comment("How often, in server ticks, automatic overflow checks run for carried backpacks. 20 ticks = 1 second.")
                .defineInRange("overflowCheckIntervalTicks", 20, 1, 1200);

        OVERFLOW_SKIP_NESTED_BACKPACKS = builder
                .comment("Do not automatically send TacticalBackpack items stored inside another backpack to external storage.")
                .define("overflowSkipNestedBackpacks", true);

        ENABLE_PICKUP_OVERFLOW = builder
                .comment("When the normal player inventory is full, route picked-up items into carried TacticalBackpacks instead of leaving them on the ground.")
                .define("enablePickupOverflow", true);

        PICKUP_OVERFLOW_REQUIRES_AUTO_PICKUP_MODULE = builder
                .comment("Require an Auto Pickup Module in a backpack before that backpack can receive pickup overflow.")
                .define("pickupOverflowRequiresAutoPickupModule", true);

        PICKUP_OVERFLOW_TO_AE2 = builder
                .comment("Legacy compatibility option. AE2 participation is still controlled by overflowToAE2.")
                .define("pickupOverflowToAE2", true);

        PICKUP_OVERFLOW_TO_EXTERNAL_STORAGE = builder
                .comment("When a pickup-overflow backpack is full, route the remainder through all linked external storage providers.")
                .define("pickupOverflowToExternalStorage", true);

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