package com.github.kisaragimikoto.tacticalbackpack.client;

import com.github.kisaragimikoto.tacticalbackpack.menu.BackpackTabs;
import com.github.kisaragimikoto.tacticalbackpack.menu.BackpackUpgradesMenu;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

public final class BackpackUpgradesScreen extends BackpackSimpleScreen<BackpackUpgradesMenu> {
    public BackpackUpgradesScreen(BackpackUpgradesMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title,
                BackpackTabs.UPGRADES,
                Component.translatable("screen.tacticalbackpack.section.upgrades"));
    }
}
