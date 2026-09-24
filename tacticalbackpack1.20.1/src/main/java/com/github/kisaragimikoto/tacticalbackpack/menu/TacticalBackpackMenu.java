package com.github.kisaragimikoto.tacticalbackpack.menu;

import com.github.kisaragimikoto.tacticalbackpack.inventory.BackpackInventory;
import com.github.kisaragimikoto.tacticalbackpack.compat.ae2.AE2Integration;
import com.github.kisaragimikoto.tacticalbackpack.item.TacticalBackpackItem;
import com.github.kisaragimikoto.tacticalbackpack.registry.ModMenus;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.inventory.DataSlot;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.network.NetworkHooks;

public class TacticalBackpackMenu extends AbstractContainerMenu {
    private static final int MAX_VISIBLE_STORAGE = 54;
    private final BackpackInventory container;
    private final ItemStack backpackStack;
    private final int storageSize;
    private final int visibleStorageSlots;
    private final int rows;
    private final Player ownerPlayer;
    private int clientAe2Linked;
    private int clientAe2Online;

    public TacticalBackpackMenu(int id, Inventory playerInventory, ItemStack stack) {
        super(ModMenus.BACKPACK.get(), id);
        this.backpackStack = stack;
        this.ownerPlayer = playerInventory.player;
        this.container = TacticalBackpackItem.getInventory(stack);
        this.storageSize = BackpackInventory.getStorageSize(stack);
        this.visibleStorageSlots = Math.min(storageSize, MAX_VISIBLE_STORAGE);
        this.rows = Math.max(1, (visibleStorageSlots + 8) / 9);
        container.startOpen(playerInventory.player);

        final int startX = 8;
        final int storageY = 30;
        for (int slot = 0; slot < visibleStorageSlots; slot++) {
            int col = slot % 9;
            int row = slot / 9;
            addSlot(new Slot(container, slot, startX + col * 18, storageY + row * 18) {
                @Override
                public boolean mayPlace(ItemStack candidate) {
                    // Other backpacks are allowed. Only the exact backpack currently
                    // being edited is rejected to prevent a self-referencing NBT loop.
                    return !TacticalBackpackItem.isSameBackpack(backpackStack, candidate);
                }
            });
        }

        int playerInventoryY = storageY + rows * 18 + 14;
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                addSlot(new Slot(playerInventory, col + row * 9 + 9,
                        startX + col * 18, playerInventoryY + row * 18));
            }
        }
        int hotbarY = playerInventoryY + 58;
        for (int col = 0; col < 9; col++) {
            addSlot(new Slot(playerInventory, col, startX + col * 18, hotbarY));
        }

        addDataSlot(new DataSlot() {
            @Override
            public int get() {
                if (ownerPlayer instanceof ServerPlayer serverPlayer) {
                    return AE2Integration.isEnabled() && AE2Integration.isLinked(backpackStack) ? 1 : 0;
                }
                return clientAe2Linked;
            }

            @Override
            public void set(int value) {
                clientAe2Linked = value;
            }
        });

        addDataSlot(new DataSlot() {
            @Override
            public int get() {
                if (ownerPlayer instanceof ServerPlayer serverPlayer) {
                    return AE2Integration.isEnabled()
                            && AE2Integration.resolveStorage(serverPlayer, backpackStack).isOnline() ? 1 : 0;
                }
                return clientAe2Online;
            }

            @Override
            public void set(int value) {
                clientAe2Online = value;
            }
        });
    }

    public int getRows() { return rows; }
    public int getVisibleStorageSlots() { return visibleStorageSlots; }
    public ItemStack getBackpackStack() { return backpackStack; }
    public boolean isAe2Linked() { return clientAe2Linked != 0; }
    public boolean isAe2Online() { return clientAe2Online != 0; }

    public void saveBackpackInventory() {
        TacticalBackpackItem.saveInventory(backpackStack, container);
    }

    public void reloadBackpackInventory() {
        container.loadFromNBT(backpackStack.getTag());
        broadcastChanges();
    }

    @Override
    public boolean stillValid(Player player) {
        return !backpackStack.isEmpty() && backpackStack.getItem() instanceof TacticalBackpackItem;
    }

    @Override
    public void slotsChanged(Container changedContainer) {
        super.slotsChanged(changedContainer);
        if (changedContainer == container) {
            TacticalBackpackItem.saveInventory(backpackStack, container);
            broadcastChanges();
        }
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        Slot slot = index >= 0 && index < slots.size() ? slots.get(index) : null;
        if (slot == null || !slot.hasItem()) return ItemStack.EMPTY;

        ItemStack source = slot.getItem();
        ItemStack original = source.copy();
        int backpackEnd = visibleStorageSlots;
        int playerEnd = slots.size();

        if (index < backpackEnd) {
            if (!moveItemStackTo(source, backpackEnd, playerEnd, true)) return ItemStack.EMPTY;
        } else {
            if (TacticalBackpackItem.isSameBackpack(backpackStack, source)) return ItemStack.EMPTY;
            if (!moveItemStackTo(source, 0, backpackEnd, false)) return ItemStack.EMPTY;
        }

        if (source.isEmpty()) slot.set(ItemStack.EMPTY);
        else slot.setChanged();
        if (source.getCount() == original.getCount()) return ItemStack.EMPTY;
        slot.onTake(player, source);
        container.setChanged();
        return original;
    }

    @Override
    public void removed(Player player) {
        super.removed(player);
        container.stopOpen(player);
        TacticalBackpackItem.saveInventory(backpackStack, container);
    }

    @Override
    public boolean clickMenuButton(Player player, int id) {
        if (!(player instanceof ServerPlayer serverPlayer)) return false;
        TacticalBackpackItem.saveInventory(backpackStack, container);
        MenuProvider provider = switch (id) {
            case BackpackTabs.MAIN -> provider("Tactical Backpack", (windowId, inv, p) -> new TacticalBackpackMenu(windowId, inv, backpackStack));
            case BackpackTabs.CRAFT -> provider("Backpack Crafting", (windowId, inv, p) -> new BackpackCraftMenu(windowId, inv, backpackStack));
            case BackpackTabs.POCKET -> provider("Backpack Pocket", (windowId, inv, p) -> new BackpackPocketMenu(windowId, inv, backpackStack));
            case BackpackTabs.UPGRADES -> provider("Backpack Upgrades", (windowId, inv, p) -> new BackpackUpgradesMenu(windowId, inv, backpackStack));
            case BackpackTabs.EQUIPMENT -> provider("Backpack Equipment", (windowId, inv, p) -> new BackpackEquipmentMenu(windowId, inv, backpackStack));
            default -> null;
        };
        if (provider == null) return false;
        NetworkHooks.openScreen(serverPlayer, provider, buf -> buf.writeItem(backpackStack));
        return true;
    }

    private static MenuProvider provider(String title, MenuFactory factory) {
        return new MenuProvider() {
            public Component getDisplayName() { return Component.literal(title); }
            public AbstractContainerMenu createMenu(int id, Inventory inv, Player player) { return factory.create(id, inv, player); }
        };
    }

    private interface MenuFactory { AbstractContainerMenu create(int id, Inventory inv, Player player); }
}
