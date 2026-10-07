package com.github.kisaragimikoto.tacticalbackpack.menu;

import com.github.kisaragimikoto.tacticalbackpack.inventory.BackpackInventory;
import com.github.kisaragimikoto.tacticalbackpack.compat.ae2.AE2Integration;
import com.github.kisaragimikoto.tacticalbackpack.item.TacticalBackpackItem;
import com.github.kisaragimikoto.tacticalbackpack.registry.ModMenus;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.DataSlot;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.network.NetworkHooks;

/** Main backpack menu. Storage is exposed as 54-slot pages so very large backpacks remain usable. */
public class TacticalBackpackMenu extends AbstractContainerMenu {
    public static final int STORAGE_PAGE_SIZE = 54;

    public static final int BUTTON_PAGE_PREVIOUS = 100;
    public static final int BUTTON_PAGE_NEXT = 101;
    public static final int BUTTON_PAGE_FIRST = 102;
    public static final int BUTTON_PAGE_LAST = 103;
    public static final int BUTTON_PAGE_JUMP_BASE = 10_000;

    private final BackpackInventory container;
    private final PagedStorageContainer pagedStorage;
    private final ItemStack backpackStack;
    private final int storageSize;
    private final int visibleStorageSlots;
    private final int rows;
    private final int totalPages;
    private final Player ownerPlayer;
    private int currentPage;
    private int clientAe2Linked;
    private int clientAe2Online;

    public TacticalBackpackMenu(int id, Inventory playerInventory, ItemStack stack) {
        super(ModMenus.BACKPACK.get(), id);
        this.backpackStack = stack;
        this.ownerPlayer = playerInventory.player;
        this.container = TacticalBackpackItem.getInventory(stack);
        this.storageSize = BackpackInventory.getStorageSize(stack);
        this.visibleStorageSlots = Math.min(storageSize, STORAGE_PAGE_SIZE);
        this.rows = Math.max(1, (visibleStorageSlots + 8) / 9);
        this.totalPages = Math.max(1, (storageSize + STORAGE_PAGE_SIZE - 1) / STORAGE_PAGE_SIZE);
        this.currentPage = 0;
        this.pagedStorage = new PagedStorageContainer(container, storageSize);
        this.pagedStorage.setPage(0);
        container.startOpen(playerInventory.player);

        final int startX = 8;
        final int storageY = 30;
        for (int slot = 0; slot < visibleStorageSlots; slot++) {
            final int localSlot = slot;
            addSlot(new Slot(pagedStorage, localSlot, startX + (localSlot % 9) * 18, storageY + (localSlot / 9) * 18) {
                @Override
                public boolean mayPlace(ItemStack candidate) {
                    if (!pagedStorage.isValidLocalSlot(localSlot)) return false;
                    // Other backpacks are allowed. Only the exact backpack currently
                    // being edited is rejected to prevent a self-referencing NBT loop.
                    return !TacticalBackpackItem.isSameBackpack(backpackStack, candidate);
                }

                @Override
                public boolean mayPickup(Player player) {
                    return pagedStorage.isValidLocalSlot(localSlot) && super.mayPickup(player);
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

        // Keep the selected storage page synchronized to the client.
        addDataSlot(new DataSlot() {
            @Override
            public int get() {
                return currentPage;
            }

            @Override
            public void set(int value) {
                setCurrentPage(value, false);
            }
        });

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
    public int getStorageSize() { return storageSize; }
    public int getCurrentPage() { return currentPage; }
    public int getTotalPages() { return totalPages; }
    public int getPageStartIndex() { return currentPage * STORAGE_PAGE_SIZE; }
    public int getCurrentPageSlotCount() {
        return Math.max(0, Math.min(STORAGE_PAGE_SIZE, storageSize - getPageStartIndex()));
    }
    public boolean hasPreviousPage() { return currentPage > 0; }
    public boolean hasNextPage() { return currentPage + 1 < totalPages; }
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

    private void setCurrentPage(int requestedPage, boolean syncSlots) {
        int clamped = Math.max(0, Math.min(requestedPage, totalPages - 1));
        if (currentPage == clamped && pagedStorage.getPage() == clamped) return;
        currentPage = clamped;
        pagedStorage.setPage(clamped);
        if (syncSlots) {
            broadcastChanges();
        }
    }

    @Override
    public boolean stillValid(Player player) {
        return !backpackStack.isEmpty() && backpackStack.getItem() instanceof TacticalBackpackItem;
    }

    @Override
    public void slotsChanged(Container changedContainer) {
        super.slotsChanged(changedContainer);
        if (changedContainer == container || changedContainer == pagedStorage) {
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
        // Page controls stay in the same open menu; no screen recreation required.
        if (id == BUTTON_PAGE_PREVIOUS) {
            setCurrentPage(currentPage - 1, true);
            return true;
        }
        if (id == BUTTON_PAGE_NEXT) {
            setCurrentPage(currentPage + 1, true);
            return true;
        }
        if (id == BUTTON_PAGE_FIRST) {
            setCurrentPage(0, true);
            return true;
        }
        if (id == BUTTON_PAGE_LAST) {
            setCurrentPage(totalPages - 1, true);
            return true;
        }
        if (id >= BUTTON_PAGE_JUMP_BASE) {
            setCurrentPage(id - BUTTON_PAGE_JUMP_BASE, true);
            return true;
        }

        if (!(player instanceof ServerPlayer serverPlayer)) return false;
        TacticalBackpackItem.saveInventory(backpackStack, container);
        MenuProvider provider = switch (id) {
            case BackpackTabs.MAIN -> provider("Tactical Backpack", (windowId, inv, p) -> new TacticalBackpackMenu(windowId, inv, backpackStack));
            case BackpackTabs.CRAFT -> provider("Backpack Crafting", (windowId, inv, p) -> new BackpackCraftMenu(windowId, inv, backpackStack));
            case BackpackTabs.POCKET -> provider("Backpack Pocket", (windowId, inv, p) -> new BackpackPocketMenu(windowId, inv, backpackStack));
            case BackpackTabs.UPGRADES -> provider("Backpack Upgrades", (windowId, inv, p) -> new BackpackUpgradesMenu(windowId, inv, backpackStack));
            case BackpackTabs.EQUIPMENT -> provider("Backpack Attachments", (windowId, inv, p) -> new BackpackEquipmentMenu(windowId, inv, backpackStack));
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

    /**
     * Fixed 54-slot window into the real backpack inventory.
     * The Slot objects never need to be recreated; changing page only changes this window offset.
     */
    private static final class PagedStorageContainer implements Container {
        private final BackpackInventory backing;
        private final int storageSize;
        private int page;

        private PagedStorageContainer(BackpackInventory backing, int storageSize) {
            this.backing = backing;
            this.storageSize = storageSize;
        }

        int getPage() {
            return page;
        }

        void setPage(int page) {
            this.page = Math.max(0, page);
        }

        boolean isValidLocalSlot(int localSlot) {
            int actual = actualIndex(localSlot);
            return localSlot >= 0 && localSlot < STORAGE_PAGE_SIZE && actual >= 0 && actual < storageSize;
        }

        private int actualIndex(int localSlot) {
            return page * STORAGE_PAGE_SIZE + localSlot;
        }

        @Override
        public int getContainerSize() {
            return STORAGE_PAGE_SIZE;
        }

        @Override
        public boolean isEmpty() {
            for (int i = 0; i < STORAGE_PAGE_SIZE; i++) {
                if (isValidLocalSlot(i) && !backing.getItem(actualIndex(i)).isEmpty()) return false;
            }
            return true;
        }

        @Override
        public ItemStack getItem(int slot) {
            return isValidLocalSlot(slot) ? backing.getItem(actualIndex(slot)) : ItemStack.EMPTY;
        }

        @Override
        public ItemStack removeItem(int slot, int amount) {
            return isValidLocalSlot(slot) ? backing.removeItem(actualIndex(slot), amount) : ItemStack.EMPTY;
        }

        @Override
        public ItemStack removeItemNoUpdate(int slot) {
            return isValidLocalSlot(slot) ? backing.removeItemNoUpdate(actualIndex(slot)) : ItemStack.EMPTY;
        }

        @Override
        public void setItem(int slot, ItemStack stack) {
            if (isValidLocalSlot(slot)) backing.setItem(actualIndex(slot), stack);
        }

        @Override
        public void setChanged() {
            backing.setChanged();
        }

        @Override
        public boolean stillValid(Player player) {
            return backing.stillValid(player);
        }

        @Override
        public void clearContent() {
            for (int i = 0; i < STORAGE_PAGE_SIZE; i++) {
                if (isValidLocalSlot(i)) backing.setItem(actualIndex(i), ItemStack.EMPTY);
            }
            backing.setChanged();
        }
    }
}
