package com.github.kisaragimikoto.tacticalbackpack.menu;

import com.github.kisaragimikoto.tacticalbackpack.inventory.BackpackInventory;
import com.github.kisaragimikoto.tacticalbackpack.item.TacticalBackpackItem;
import com.github.kisaragimikoto.tacticalbackpack.menu.slot.EnergySlot;
import com.github.kisaragimikoto.tacticalbackpack.menu.slot.SizeSlot;
import com.github.kisaragimikoto.tacticalbackpack.menu.slot.UpgradeSlot;
import com.github.kisaragimikoto.tacticalbackpack.registry.ModMenus;

import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.network.NetworkHooks;

public class BackpackUpgradesMenu extends AbstractContainerMenu {

    private final ItemStack backpackStack;
    private final BackpackInventory backpackInventory;

    public BackpackUpgradesMenu(int id, Inventory playerInventory, ItemStack backpackStack) {
        super(ModMenus.BACKPACK_UPGRADES.get(), id);

        this.backpackStack = backpackStack;
        this.backpackInventory = TacticalBackpackItem.getInventory(backpackStack);

        int storageSize = BackpackInventory.getStorageSize(backpackStack);

        this.addSlot(new EnergySlot(backpackInventory, BackpackInventory.getUpgradeSlotIndex(storageSize, 0), 40, 88));
        this.addSlot(new UpgradeSlot(backpackInventory, BackpackInventory.getUpgradeSlotIndex(storageSize, 1), 72, 87));
        this.addSlot(new SizeSlot(backpackInventory, BackpackInventory.getUpgradeSlotIndex(storageSize, 2), 103, 88));

        final int invStartX = 9;
        final int invStartY = 122;
        final int gap = 16;

        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                this.addSlot(new Slot(
                        playerInventory,
                        col + row * 9 + 9,
                        invStartX + col * gap,
                        invStartY + row * gap
                ));
            }
        }

        final int hotbarY = 173;

        for (int col = 0; col < 9; col++) {
            this.addSlot(new Slot(playerInventory, col, invStartX + col * gap, hotbarY));
        }
    }

    @Override
    public boolean stillValid(Player player) {
        return true;
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

    @Override
    public boolean clickMenuButton(Player player, int id) {
        if (!(player instanceof ServerPlayer serverPlayer)) return false;

        MenuProvider provider = switch (id) {
            case BackpackTabs.MAIN -> provider("Tactical Backpack", (windowId, inv, p) ->
                    new TacticalBackpackMenu(windowId, inv, backpackStack));

            case BackpackTabs.CRAFT -> provider("Backpack Crafting", (windowId, inv, p) ->
                    new BackpackCraftMenu(windowId, inv, backpackStack));

            case BackpackTabs.POCKET -> provider("Backpack Pocket", (windowId, inv, p) ->
                    new BackpackPocketMenu(windowId, inv, backpackStack));

            case BackpackTabs.UPGRADES -> provider("Backpack Upgrades", (windowId, inv, p) ->
                    new BackpackUpgradesMenu(windowId, inv, backpackStack));

            case BackpackTabs.EQUIPMENT -> provider("Backpack Equipment", (windowId, inv, p) ->
                    new BackpackEquipmentMenu(windowId, inv, backpackStack));

            default -> null;
        };

        if (provider == null) return false;

        NetworkHooks.openScreen(serverPlayer, provider, buf -> buf.writeItem(backpackStack));
        return true;
    }

    private static MenuProvider provider(String title, MenuFactory factory) {
        return new MenuProvider() {
            @Override
            public Component getDisplayName() {
                return Component.literal(title);
            }

            @Override
            public AbstractContainerMenu createMenu(int id, Inventory inv, Player player) {
                return factory.create(id, inv, player);
            }
        };
    }

    private interface MenuFactory {
        AbstractContainerMenu create(int id, Inventory inv, Player player);
    }
}