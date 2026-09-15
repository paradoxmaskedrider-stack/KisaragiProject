package com.github.kisaragimikoto.tacticalbackpack.menu.slot;

import com.github.kisaragimikoto.tacticalbackpack.inventory.BackpackCraftingInventory;

import net.minecraft.core.NonNullList;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ResultContainer;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;

import java.util.Optional;

public class BackpackResultSlot extends Slot {

    private final BackpackCraftingInventory craftMatrix;
    private final Player owner;

    public BackpackResultSlot(Player owner, BackpackCraftingInventory craftMatrix, ResultContainer result, int index, int x, int y) {
        super(result, index, x, y);
        this.owner = owner;
        this.craftMatrix = craftMatrix;
    }

    @Override
    public boolean mayPlace(ItemStack stack) {
        return false;
    }

    @Override
    public void onTake(Player player, ItemStack craftedStack) {
        super.onTake(player, craftedStack);

        Level level = owner.level();

        Optional<CraftingRecipe> recipeOptional = level.getRecipeManager()
                .getRecipeFor(RecipeType.CRAFTING, craftMatrix, level);

        if (recipeOptional.isEmpty()) {
            return;
        }

        CraftingRecipe recipe = recipeOptional.get();
        NonNullList<ItemStack> remainingItems = recipe.getRemainingItems(craftMatrix);

        for (int i = 0; i < craftMatrix.getContainerSize(); i++) {
            ItemStack ingredient = craftMatrix.getItem(i);
            ItemStack remaining = remainingItems.get(i);

            if (!ingredient.isEmpty()) {
                ingredient.shrink(1);
                if (ingredient.isEmpty()) {
                    craftMatrix.setItem(i, ItemStack.EMPTY);
                }
            }

            if (!remaining.isEmpty()) {
                ItemStack slotStack = craftMatrix.getItem(i);

                if (slotStack.isEmpty()) {
                    craftMatrix.setItem(i, remaining.copy());
                } else if (ItemStack.isSameItemSameTags(slotStack, remaining)) {
                    slotStack.grow(remaining.getCount());
                } else {
                    player.getInventory().placeItemBackInInventory(remaining.copy());
                }
            }
        }
    }
}