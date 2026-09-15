package com.github.kisaragimikoto.tacticalbackpack.registry;

import com.github.kisaragimikoto.tacticalbackpack.TacticalBackpackMod;
import com.github.kisaragimikoto.tacticalbackpack.item.TacticalBackpackItem;
import com.github.kisaragimikoto.tacticalbackpack.item.BackpackReturnDeviceItem;
import com.github.kisaragimikoto.tacticalbackpack.item.BackpackZoneControllerItem;
import com.github.kisaragimikoto.tacticalbackpack.item.BackpackZoneRemoverItem;

import net.minecraft.world.item.BlockItem;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModItems {

    public static final DeferredRegister<Item> ITEMS =
            DeferredRegister.create(ForgeRegistries.ITEMS, TacticalBackpackMod.MODID);

    public static final RegistryObject<Item> TACTICAL_BACKPACK =
            ITEMS.register("tactical_backpack",
                    () -> new TacticalBackpackItem(new Item.Properties().stacksTo(1).rarity(Rarity.UNCOMMON)));

    public static final RegistryObject<Item> ENERGY_CELL_SMALL =
            ITEMS.register("energy_cell_small",
                    () -> new Item(new Item.Properties().stacksTo(16)));

    public static final RegistryObject<Item> ENERGY_CELL_MEDIUM =
            ITEMS.register("energy_cell_medium",
                    () -> new Item(new Item.Properties().stacksTo(16).rarity(Rarity.UNCOMMON)));

    public static final RegistryObject<Item> ENERGY_CELL_LARGE =
            ITEMS.register("energy_cell_large",
                    () -> new Item(new Item.Properties().stacksTo(16).rarity(Rarity.RARE)));

    public static final RegistryObject<Item> AUTO_PICKUP_MODULE =
            ITEMS.register("auto_pickup_module",
                    () -> new Item(new Item.Properties().stacksTo(1).rarity(Rarity.UNCOMMON)));

    public static final RegistryObject<Item> FILTER_MODULE =
            ITEMS.register("filter_module",
                    () -> new Item(new Item.Properties().stacksTo(1)));

    public static final RegistryObject<Item> SORT_MODULE =
            ITEMS.register("sort_module",
                    () -> new Item(new Item.Properties().stacksTo(1)));

    public static final RegistryObject<Item> SIZE_UPGRADE_1 =
            ITEMS.register("size_upgrade_1",
                    () -> new Item(new Item.Properties().stacksTo(1).rarity(Rarity.UNCOMMON)));

    public static final RegistryObject<Item> SIZE_UPGRADE_2 =
            ITEMS.register("size_upgrade_2",
                    () -> new Item(new Item.Properties().stacksTo(1).rarity(Rarity.RARE)));

    public static final RegistryObject<Item> POCKET_CORE =
            ITEMS.register("pocket_core",
                    () -> new Item(new Item.Properties().stacksTo(1).rarity(Rarity.RARE)));

    public static final RegistryObject<Item> STABILITY_MODULE =
            ITEMS.register("stability_module",
                    () -> new Item(new Item.Properties().stacksTo(1).rarity(Rarity.UNCOMMON)));

    public static final RegistryObject<Item> REPAIR_KIT =
            ITEMS.register("repair_kit",
                    () -> new Item(new Item.Properties().stacksTo(16)));

    public static final RegistryObject<Item> WORKBENCH_MODULE =
            ITEMS.register("workbench_module",
                    () -> new Item(new Item.Properties().stacksTo(1).rarity(Rarity.UNCOMMON)));

    // 🔥 追加（これがエラー原因だったやつ）
    public static final RegistryObject<Item> GUN_WORKBENCH_MODULE =
            ITEMS.register("gun_workbench_module",
                    () -> new Item(new Item.Properties().stacksTo(1).rarity(Rarity.RARE)));

    public static final RegistryObject<Item> MEMORY_CARD =
            ITEMS.register("memory_card",
                    () -> new Item(new Item.Properties().stacksTo(1)));

    public static final RegistryObject<Item> TRADE_COIN =
            ITEMS.register("trade_coin",
                    () -> new Item(new Item.Properties().stacksTo(64).rarity(Rarity.UNCOMMON)));

    public static final RegistryObject<Item> BACKPACK_TRADE_TERMINAL =
            ITEMS.register("backpack_trade_terminal",
                    () -> new BlockItem(
                            ModBlocks.BACKPACK_TRADE_TERMINAL.get(),
                            new Item.Properties().stacksTo(64).rarity(Rarity.RARE)
                    ));

    public static final RegistryObject<Item> BACKPACK_RETURN_DEVICE =
            ITEMS.register("backpack_return_device",
                    () -> new BackpackReturnDeviceItem(new Item.Properties().stacksTo(1).rarity(Rarity.RARE)));

    public static final RegistryObject<Item> BACKPACK_ZONE_CONTROLLER =
            ITEMS.register("backpack_zone_controller",
                    () -> new BackpackZoneControllerItem(
                            new Item.Properties()
                                    .stacksTo(1)
                                    .rarity(Rarity.RARE)
                    ));

    public static final RegistryObject<Item> BACKPACK_ZONE_REMOVER =
            ITEMS.register("backpack_zone_remover",
                    () -> new BackpackZoneRemoverItem(
                            new Item.Properties()
                                    .stacksTo(1)
                                    .rarity(Rarity.RARE)
                    ));
}