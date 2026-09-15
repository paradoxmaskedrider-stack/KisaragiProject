package com.github.kisaragimikoto.tacticalbackpack.inventory;

import com.github.kisaragimikoto.tacticalbackpack.item.TacticalBackpackItem;

import net.minecraftforge.items.ItemStackHandler;
import net.minecraft.world.item.ItemStack;

public class BackpackItemHandler extends ItemStackHandler {

    private final ItemStack backpack;

    public BackpackItemHandler(ItemStack stack) {
        super(54);
        this.backpack = stack;

        BackpackInventory inv = TacticalBackpackItem.getInventory(stack);

        for (int i = 0; i < getSlots(); i++) {
            setStackInSlot(i, inv.getItem(i));
        }
    }

    @Override
    protected void onContentsChanged(int slot) {
        BackpackInventory inv = TacticalBackpackItem.getInventory(backpack);

        for (int i = 0; i < getSlots(); i++) {
            inv.setItem(i, getStackInSlot(i));
        }

        TacticalBackpackItem.saveInventory(backpack, inv);
    }
}