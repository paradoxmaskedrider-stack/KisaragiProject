package com.github.kisaragimikoto.tacticalbackpack.client;

import com.github.kisaragimikoto.tacticalbackpack.menu.BackpackEquipmentMenu;
import com.github.kisaragimikoto.tacticalbackpack.menu.BackpackTabs;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

public final class BackpackEquipmentScreen extends BackpackSimpleScreen<BackpackEquipmentMenu> {
    public BackpackEquipmentScreen(BackpackEquipmentMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title,
                BackpackTabs.EQUIPMENT,
                Component.translatable("screen.tacticalbackpack.section.attachments"));
    }
}
