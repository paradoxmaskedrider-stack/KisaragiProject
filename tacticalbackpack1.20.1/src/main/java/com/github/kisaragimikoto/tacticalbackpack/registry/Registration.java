package com.github.kisaragimikoto.tacticalbackpack.registry;

import net.minecraftforge.eventbus.api.IEventBus;

public final class Registration {

    private Registration() {}

    public static void register(IEventBus bus) {

        ModItems.ITEMS.register(bus);

        ModBlocks.BLOCKS.register(bus);

        ModMenus.MENUS.register(bus);

        ModCreativeTabs.CREATIVE_MODE_TABS.register(bus);

        ModSounds.SOUNDS.register(bus);

        // 将来追加
        // ModEntities.ENTITIES.register(bus);
        // ModBlockEntities.BLOCK_ENTITIES.register(bus);
    }
}