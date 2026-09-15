package com.github.kisaragimikoto.tacticalbackpack.registry;

import com.github.kisaragimikoto.tacticalbackpack.TacticalBackpackMod;
import com.github.kisaragimikoto.tacticalbackpack.registry.ModItems;

import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;

import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;
import net.minecraft.core.registries.Registries; // ← ここが正解

public class ModCreativeTabs {

    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, TacticalBackpackMod.MODID);

    public static final RegistryObject<CreativeModeTab> TACTICAL_BACKPACK_TAB =
            CREATIVE_MODE_TABS.register("tactical_backpack_tab", () -> CreativeModeTab.builder()
                    .title(Component.literal("Tactical Backpack"))
                    .icon(() -> new ItemStack(ModItems.TACTICAL_BACKPACK.get()))
                    .displayItems((parameters, output) -> {

                        output.accept(ModItems.TACTICAL_BACKPACK.get());

                        output.accept(ModItems.ENERGY_CELL_SMALL.get());
                        output.accept(ModItems.ENERGY_CELL_MEDIUM.get());
                        output.accept(ModItems.ENERGY_CELL_LARGE.get());

                        output.accept(ModItems.AUTO_PICKUP_MODULE.get());
                        output.accept(ModItems.FILTER_MODULE.get());
                        output.accept(ModItems.SORT_MODULE.get());

                        output.accept(ModItems.SIZE_UPGRADE_1.get());
                        output.accept(ModItems.SIZE_UPGRADE_2.get());

                        output.accept(ModItems.POCKET_CORE.get());
                        output.accept(ModItems.STABILITY_MODULE.get());
                        output.accept(ModItems.REPAIR_KIT.get());
                        output.accept(ModItems.WORKBENCH_MODULE.get());
                        output.accept(ModItems.GUN_WORKBENCH_MODULE.get());
                        output.accept(ModItems.MEMORY_CARD.get());
                        output.accept(ModItems.TRADE_COIN.get());
                        output.accept(ModItems.BACKPACK_TRADE_TERMINAL.get());
                        output.accept(ModItems.BACKPACK_RETURN_DEVICE.get());
                        output.accept(ModItems.BACKPACK_ZONE_CONTROLLER.get());
                        output.accept(ModItems.BACKPACK_ZONE_REMOVER.get());
                    })
                    .build());
}