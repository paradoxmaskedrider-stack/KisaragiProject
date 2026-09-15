package com.github.kisaragimikoto.tacticalbackpack.menu;

import com.github.kisaragimikoto.tacticalbackpack.inventory.BackpackInventory;
import com.github.kisaragimikoto.tacticalbackpack.item.TacticalBackpackItem;
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

public class BackpackEquipmentMenu extends AbstractContainerMenu {

    private final ItemStack backpackStack;
    private final BackpackInventory backpackInventory;

    public BackpackEquipmentMenu(int id, Inventory playerInventory, ItemStack backpackStack) {
        super(ModMenus.BACKPACK_EQUIPMENT.get(), id);

        this.backpackStack = backpackStack;
        this.backpackInventory = TacticalBackpackItem.getInventory(backpackStack);

        int storageSize = BackpackInventory.getStorageSize(backpackStack);

        final int equipmentStartX = 45;
        final int equipmentStartY = 42;
        final int equipmentGapX = 24;
        final int equipmentGapY = 18;

        for (int row = 0; row < 6; row++) {
            for (int col = 0; col < 2; col++) {
                int slotIndex = row * 2 + col;

                this.addSlot(new Slot(
                        backpackInventory,
                        BackpackInventory.getEquipmentSlotIndex(storageSize, slotIndex),
                        equipmentStartX + col * equipmentGapX,
                        equipmentStartY + row * equipmentGapY
                ));
            }
        }

        final int invStartX = 9;
        final int invStartY = 147;
        final int hotbarY = 207;

        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                this.addSlot(new Slot(
                        playerInventory,
                        col + row * 9 + 9,
                        invStartX + col * 18,
                        invStartY + row * 18
                ));
            }
        }

        for (int col = 0; col < 9; col++) {
            this.addSlot(new Slot(playerInventory, col, invStartX + col * 18, hotbarY));
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