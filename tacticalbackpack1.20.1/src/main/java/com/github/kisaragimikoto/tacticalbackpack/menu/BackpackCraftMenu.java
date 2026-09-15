package com.github.kisaragimikoto.tacticalbackpack.menu;

import com.github.kisaragimikoto.tacticalbackpack.inventory.BackpackCraftingInventory;
import com.github.kisaragimikoto.tacticalbackpack.facility.BackpackFacility;
import com.github.kisaragimikoto.tacticalbackpack.facility.BackpackFacilityResolver;
import com.github.kisaragimikoto.tacticalbackpack.inventory.BackpackInventory;
import com.github.kisaragimikoto.tacticalbackpack.item.TacticalBackpackItem;
import com.github.kisaragimikoto.tacticalbackpack.menu.slot.BackpackResultSlot;
import com.github.kisaragimikoto.tacticalbackpack.registry.ModMenus;

import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ResultContainer;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;

import net.minecraftforge.network.NetworkHooks;

import java.util.Optional;

public class BackpackCraftMenu extends AbstractContainerMenu {

    private final ItemStack backpackStack;
    private final BackpackInventory backpackInventory;
    private final BackpackCraftingInventory craftMatrix;
    private final ResultContainer craftResult = new ResultContainer();
    private final Player owner;

    public BackpackCraftMenu(int id, Inventory playerInventory, ItemStack backpackStack) {
        super(ModMenus.BACKPACK_CRAFT.get(), id);

        this.backpackStack = backpackStack;
        this.backpackInventory = TacticalBackpackItem.getInventory(backpackStack);
        this.owner = playerInventory.player;
        this.craftMatrix = new BackpackCraftingInventory(this);

        // Result slot
        this.addSlot(new BackpackResultSlot(owner, craftMatrix, craftResult, 0, 122, 48));

        // 3x3 crafting grid
        final int craftStartX = 31;
        final int craftStartY = 48;
        final int craftGap = 16;

        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 3; col++) {
                this.addSlot(new Slot(
                        craftMatrix,
                        col + row * 3,
                        craftStartX + col * craftGap,
                        craftStartY + row * craftGap
                ));
            }
        }

        // Backpack storage
        final int storageStartX = 8;
        final int storageStartY = 84;
        final int storageGap = 16;

        int storageSize = BackpackInventory.getStorageSize(backpackStack);
        int maxStorageSlots = Math.min(storageSize, 54);

        for (int slotIndex = 0; slotIndex < maxStorageSlots; slotIndex++) {
            int row = slotIndex / 9;
            int col = slotIndex % 9;

            this.addSlot(new Slot(
                    backpackInventory,
                    slotIndex,
                    storageStartX + col * storageGap,
                    storageStartY + row * storageGap
            ));
        }

        // Player inventory
        final int invStartX = 8;
        final int invStartY = 160;
        final int invGap = 16;

        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                this.addSlot(new Slot(
                        playerInventory,
                        col + row * 9 + 9,
                        invStartX + col * invGap,
                        invStartY + row * invGap
                ));
            }
        }

        // Hotbar
        final int hotbarY = 214;

        for (int col = 0; col < 9; col++) {
            this.addSlot(new Slot(
                    playerInventory,
                    col,
                    invStartX + col * invGap,
                    hotbarY
            ));
        }

        slotsChanged(craftMatrix);
    }

    @Override
    public void slotsChanged(net.minecraft.world.Container container) {
        super.slotsChanged(container);
        updateCraftResult();
    }

    private void updateCraftResult() {
        Level level = owner.level();

        Optional<CraftingRecipe> recipe = level.getRecipeManager()
                .getRecipeFor(RecipeType.CRAFTING, craftMatrix, level);

        craftResult.setItem(0, recipe
                .map(craftingRecipe -> craftingRecipe.assemble(craftMatrix, level.registryAccess()).copy())
                .orElse(ItemStack.EMPTY));

        broadcastChanges();
    }

    @Override
    public boolean stillValid(Player player) {
        return !backpackStack.isEmpty()
                && backpackStack.getItem() instanceof TacticalBackpackItem
                && BackpackFacilityResolver.isAvailable(backpackStack, BackpackFacility.CRAFTING);
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        Slot slot = index >= 0 && index < slots.size() ? slots.get(index) : null;
        if (slot == null || !slot.hasItem()) return ItemStack.EMPTY;

        ItemStack source = slot.getItem();
        ItemStack original = source.copy();
        int resultStart = 0;
        int craftStart = 1;
        int craftEnd = 10;
        int backpackStart = craftEnd;
        int backpackEnd = backpackStart + Math.min(BackpackInventory.getStorageSize(backpackStack), 54);
        int playerStart = backpackEnd;
        int playerEnd = slots.size();

        if (index == resultStart) {
            if (!moveItemStackTo(source, playerStart, playerEnd, true)) return ItemStack.EMPTY;
            slot.onQuickCraft(source, original);
        } else if (index >= craftStart && index < backpackEnd) {
            if (!moveItemStackTo(source, playerStart, playerEnd, false)) return ItemStack.EMPTY;
        } else {
            if (TacticalBackpackItem.isSameBackpack(backpackStack, source)) return ItemStack.EMPTY;
            if (!moveItemStackTo(source, backpackStart, backpackEnd, false)
                    && !moveItemStackTo(source, craftStart, craftEnd, false)) {
                return ItemStack.EMPTY;
            }
        }

        if (source.isEmpty()) slot.set(ItemStack.EMPTY);
        else slot.setChanged();
        if (source.getCount() == original.getCount()) return ItemStack.EMPTY;
        slot.onTake(player, source);
        backpackInventory.setChanged();
        return original;
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