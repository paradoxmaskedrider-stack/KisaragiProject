package com.github.kisaragimikoto.tacticalbackpack.menu;

import com.github.kisaragimikoto.tacticalbackpack.inventory.BackpackInventory;
import com.github.kisaragimikoto.tacticalbackpack.item.TacticalBackpackItem;
import com.github.kisaragimikoto.tacticalbackpack.registry.ModMenus;

import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public class BackpackGunWorkbenchMenu extends AbstractContainerMenu {

    private final ItemStack backpackStack;
    private final BackpackInventory backpackInventory;

    public BackpackGunWorkbenchMenu(int id, Inventory playerInventory, ItemStack backpackStack) {
        super(ModMenus.BACKPACK_GUN_WORKBENCH.get(), id);

        this.backpackStack = backpackStack;
        this.backpackInventory = TacticalBackpackItem.getInventory(backpackStack);

        // Blueprint slot
        this.addSlot(new Slot(backpackInventory, 0, 48, 54));

        // Required parts slots
        for (int row = 0; row < 5; row++) {
            this.addSlot(new Slot(backpackInventory, 1 + row, 126, 36 + row * 20));
        }

        // Parts storage 9x2
        int partsStartIndex = 6;
        for (int row = 0; row < 2; row++) {
            for (int col = 0; col < 9; col++) {
                this.addSlot(new Slot(
                        backpackInventory,
                        partsStartIndex + col + row * 9,
                        8 + col * 18,
                        104 + row * 18
                ));
            }
        }

        // Player inventory
        int invStartY = 160;
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                this.addSlot(new Slot(
                        playerInventory,
                        col + row * 9 + 9,
                        8 + col * 18,
                        invStartY + row * 18
                ));
            }
        }

        // Hotbar
        int hotbarY = 214;
        for (int col = 0; col < 9; col++) {
            this.addSlot(new Slot(
                    playerInventory,
                    col,
                    8 + col * 18,
                    hotbarY
            ));
        }
    }

    @Override
    public boolean stillValid(Player player) {
        return !backpackStack.isEmpty();
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        return ItemStack.EMPTY;
    }

    @Override
    public void removed(Player player) {
        super.removed(player);
        TacticalBackpackItem.saveInventory(backpackStack, backpackInventory);
    }
}