package com.github.kisaragimikoto.tacticalbackpack.client;

import com.github.kisaragimikoto.tacticalbackpack.menu.BackpackCraftMenu;
import com.github.kisaragimikoto.tacticalbackpack.menu.BackpackTabs;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

public final class BackpackCraftScreen extends BackpackSimpleScreen<BackpackCraftMenu> {
    public BackpackCraftScreen(BackpackCraftMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title,
                BackpackTabs.CRAFT,
                Component.translatable("screen.tacticalbackpack.section.craft"));

        // Crafting needs more vertical room than the other secondary tabs:
        // 3x3 grid + 54 backpack slots + player inventory + hotbar.
        this.imageHeight = 304;
        this.inventoryLabelY = 206;
    }
}
