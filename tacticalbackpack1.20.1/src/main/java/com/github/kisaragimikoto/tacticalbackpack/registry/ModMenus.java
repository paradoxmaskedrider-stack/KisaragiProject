package com.github.kisaragimikoto.tacticalbackpack.registry;

import com.github.kisaragimikoto.tacticalbackpack.TacticalBackpack;
import com.github.kisaragimikoto.tacticalbackpack.menu.TacticalBackpackMenu;
import com.github.kisaragimikoto.tacticalbackpack.menu.BackpackCraftMenu;
import com.github.kisaragimikoto.tacticalbackpack.menu.BackpackPocketMenu;
import com.github.kisaragimikoto.tacticalbackpack.menu.BackpackUpgradesMenu;
import com.github.kisaragimikoto.tacticalbackpack.menu.BackpackWorkbenchMenu;
import com.github.kisaragimikoto.tacticalbackpack.menu.BackpackEquipmentMenu;
import com.github.kisaragimikoto.tacticalbackpack.menu.BackpackTradeMenu;

import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.ItemStack;

import net.minecraftforge.common.extensions.IForgeMenuType;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import com.github.kisaragimikoto.tacticalbackpack.menu.BackpackGunWorkbenchMenu;

public class ModMenus {

    public static final DeferredRegister<MenuType<?>> MENUS =
            DeferredRegister.create(ForgeRegistries.MENU_TYPES, TacticalBackpack.MODID);

    public static final RegistryObject<MenuType<TacticalBackpackMenu>> BACKPACK =
            MENUS.register("backpack",
                    () -> IForgeMenuType.create((windowId, inv, data) -> {
                        ItemStack stack = data.readItem();
                        return new TacticalBackpackMenu(windowId, inv, stack);
                    }));

    public static final RegistryObject<MenuType<BackpackCraftMenu>> BACKPACK_CRAFT =
            MENUS.register("backpack_craft",
                    () -> IForgeMenuType.create((windowId, inv, data) -> {
                        ItemStack stack = data.readItem();
                        return new BackpackCraftMenu(windowId, inv, stack);
                    }));

    public static final RegistryObject<MenuType<BackpackPocketMenu>> BACKPACK_POCKET =
            MENUS.register("backpack_pocket",
                    () -> IForgeMenuType.create((windowId, inv, data) -> {
                        ItemStack stack = data.readItem();
                        return new BackpackPocketMenu(windowId, inv, stack);
                    }));

    public static final RegistryObject<MenuType<BackpackUpgradesMenu>> BACKPACK_UPGRADES =
            MENUS.register("backpack_upgrades",
                    () -> IForgeMenuType.create((windowId, inv, data) -> {
                        ItemStack stack = data.readItem();
                        return new BackpackUpgradesMenu(windowId, inv, stack);
                    }));

    public static final RegistryObject<MenuType<BackpackWorkbenchMenu>> BACKPACK_WORKBENCH =
            MENUS.register("backpack_workbench",
                    () -> IForgeMenuType.create((windowId, inv, data) -> {
                        ItemStack stack = data.readItem();
                        return new BackpackWorkbenchMenu(windowId, inv, stack);
                    }));

    public static final RegistryObject<MenuType<BackpackEquipmentMenu>> BACKPACK_EQUIPMENT =
            MENUS.register("backpack_equipment",
                    () -> IForgeMenuType.create((windowId, inv, data) -> {
                        ItemStack stack = data.readItem();
                        return new BackpackEquipmentMenu(windowId, inv, stack);
                    }));
    public static final RegistryObject<MenuType<BackpackGunWorkbenchMenu>> BACKPACK_GUN_WORKBENCH =
            MENUS.register("backpack_gun_workbench",
                    () -> IForgeMenuType.create((windowId, inv, data) -> {
                        ItemStack stack = data.readItem();
                        return new BackpackGunWorkbenchMenu(windowId, inv, stack);
                    }));

    public static final RegistryObject<MenuType<BackpackTradeMenu>> BACKPACK_TRADE =
            MENUS.register("backpack_trade",
                    () -> IForgeMenuType.create((windowId, inv, data) ->
                            new BackpackTradeMenu(windowId, inv)
                    ));
}