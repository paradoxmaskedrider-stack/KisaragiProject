package com.github.kisaragimikoto.tacticalbackpack.client;

import com.github.kisaragimikoto.tacticalbackpack.menu.BackpackPocketMenu;
import com.github.kisaragimikoto.tacticalbackpack.menu.BackpackTabs;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

public final class BackpackPocketScreen extends BackpackSimpleScreen<BackpackPocketMenu> {
    public BackpackPocketScreen(BackpackPocketMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title,
                BackpackTabs.POCKET,
                Component.translatable("screen.tacticalbackpack.section.pocket"));
    }
}
