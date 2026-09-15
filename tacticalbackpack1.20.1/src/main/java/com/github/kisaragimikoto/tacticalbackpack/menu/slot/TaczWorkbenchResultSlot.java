package com.github.kisaragimikoto.tacticalbackpack.menu.slot;

import com.github.kisaragimikoto.tacticalbackpack.menu.BackpackWorkbenchMenu;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ResultContainer;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public class TaczWorkbenchResultSlot extends Slot {

    private final BackpackWorkbenchMenu menu;

    public TaczWorkbenchResultSlot(BackpackWorkbenchMenu menu, ResultContainer result, int index, int x, int y) {
        super(result, index, x, y);
        this.menu = menu;
    }

    @Override
    public boolean mayPlace(ItemStack stack) {
        return false;
    }

    @Override
    public void onTake(Player player, ItemStack stack) {
        super.onTake(player, stack);
        menu.onTaczResultTaken(player);
    }
}