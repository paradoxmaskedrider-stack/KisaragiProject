package com.github.kisaragimikoto.tacticalbackpack.menu;

import com.github.kisaragimikoto.tacticalbackpack.compat.tacz.TaczRecipeEntry;
import com.github.kisaragimikoto.tacticalbackpack.compat.tacz.TaczRecipeRegistry;
import com.github.kisaragimikoto.tacticalbackpack.inventory.BackpackCraftingInventory;
import com.github.kisaragimikoto.tacticalbackpack.inventory.BackpackInventory;
import com.github.kisaragimikoto.tacticalbackpack.inventory.TaczRecipeListContainer;
import com.github.kisaragimikoto.tacticalbackpack.item.TacticalBackpackItem;
import com.github.kisaragimikoto.tacticalbackpack.menu.slot.TaczRecipeListSlot;
import com.github.kisaragimikoto.tacticalbackpack.menu.slot.TaczWorkbenchResultSlot;
import com.github.kisaragimikoto.tacticalbackpack.registry.ModMenus;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ResultContainer;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

public class BackpackWorkbenchMenu extends AbstractContainerMenu {

    private final ItemStack backpackStack;
    private final BackpackInventory backpackInventory;
    private final BackpackCraftingInventory craftMatrix;
    private final ResultContainer craftResult = new ResultContainer();
    private final Player owner;

    private final TaczRecipeListContainer recipeListContainer = new TaczRecipeListContainer(9);
    private final List<TaczRecipeEntry> visibleRecipes = new ArrayList<>();
    private final List<String> favoriteRecipeIds;

    private TaczRecipeEntry selectedTaczRecipe;
    private int selectedRecipeIndex = -1;

    private int recipeScrollOffset = 0;
    private static final int VISIBLE_RECIPE_COUNT = 9;

    private String searchText = "";
    private boolean favoritesOnly = false;

    public BackpackWorkbenchMenu(int id, Inventory playerInventory, ItemStack backpackStack) {
        super(ModMenus.BACKPACK_WORKBENCH.get(), id);

        this.backpackStack = backpackStack;
        this.backpackInventory = TacticalBackpackItem.getInventory(backpackStack);
        this.owner = playerInventory.player;
        this.craftMatrix = new BackpackCraftingInventory(this);
        this.favoriteRecipeIds = new ArrayList<>(TacticalBackpackItem.loadFavorites(backpackStack));

        this.addSlot(new TaczWorkbenchResultSlot(this, craftResult, 0, 124, 35));

        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 3; col++) {
                int slotIndex = col + row * 3;
                this.addSlot(new Slot(craftMatrix, slotIndex, 30 + col * 18, 17 + row * 18));
            }
        }

        for (int i = 0; i < 9; i++) {
            int x = 176 + 8;
            int y = 30 + i * 18;
            this.addSlot(new TaczRecipeListSlot(recipeListContainer, i, x, y));
        }

        int storageSize = BackpackInventory.getStorageSize(backpackStack);

        for (int row = 0; row < 6; row++) {
            for (int col = 0; col < 9; col++) {
                int slotIndex = col + row * 9;
                if (slotIndex >= storageSize) {
                    break;
                }
                this.addSlot(new Slot(backpackInventory, slotIndex, 8 + col * 18, 84 + row * 18));
            }
        }

        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                this.addSlot(new Slot(playerInventory, col + row * 9 + 9, 8 + col * 18, 198 + row * 18));
            }
        }

        for (int col = 0; col < 9; col++) {
            this.addSlot(new Slot(playerInventory, col, 8 + col * 18, 256));
        }

        reloadTaczRecipeList();
        slotsChanged(craftMatrix);
    }

    public void reloadTaczRecipeList() {
        visibleRecipes.clear();

        List<TaczRecipeEntry> filteredRecipes = new ArrayList<>();
        String search = searchText.toLowerCase(Locale.ROOT).trim();

        for (TaczRecipeEntry entry : TaczRecipeRegistry.getAll()) {
            if (favoritesOnly && !isFavorite(entry)) {
                continue;
            }

            if (search.isEmpty() || matchesSearch(entry, search)) {
                filteredRecipes.add(entry);
            }
        }

        filteredRecipes.sort((a, b) -> {
            boolean af = isFavorite(a);
            boolean bf = isFavorite(b);

            if (af == bf) {
                return a.getId().toString().compareTo(b.getId().toString());
            }

            return af ? -1 : 1;
        });

        if (recipeScrollOffset < 0) {
            recipeScrollOffset = 0;
        }

        int maxOffset = Math.max(0, filteredRecipes.size() - VISIBLE_RECIPE_COUNT);
        if (recipeScrollOffset > maxOffset) {
            recipeScrollOffset = maxOffset;
        }

        int end = Math.min(recipeScrollOffset + VISIBLE_RECIPE_COUNT, filteredRecipes.size());

        for (int i = recipeScrollOffset; i < end; i++) {
            visibleRecipes.add(filteredRecipes.get(i));
        }

        for (int i = 0; i < recipeListContainer.getContainerSize(); i++) {
            if (i < visibleRecipes.size()) {
                recipeListContainer.setItem(i, createResultStack(visibleRecipes.get(i)));
            } else {
                recipeListContainer.setItem(i, ItemStack.EMPTY);
            }
        }
    }

    private boolean matchesSearch(TaczRecipeEntry entry, String search) {
        if (entry.getResultItem().toLowerCase(Locale.ROOT).contains(search)) {
            return true;
        }

        for (String ingredient : entry.getIngredients()) {
            if (ingredient.toLowerCase(Locale.ROOT).contains(search)) {
                return true;
            }
        }

        ItemStack resultStack = createResultStack(entry);
        if (!resultStack.isEmpty()) {
            String resultName = resultStack.getHoverName().getString().toLowerCase(Locale.ROOT);
            if (resultName.contains(search)) {
                return true;
            }
        }

        return false;
    }

    public boolean isFavorite(TaczRecipeEntry entry) {
        if (entry == null) {
            return false;
        }
        return favoriteRecipeIds.contains(entry.getId().toString());
    }

    public boolean isFavoritesOnly() {
        return favoritesOnly;
    }

    public void toggleFavoritesOnly() {
        favoritesOnly = !favoritesOnly;
        recipeScrollOffset = 0;
        selectedRecipeIndex = -1;
        selectedTaczRecipe = null;
        craftResult.setItem(0, ItemStack.EMPTY);
        reloadTaczRecipeList();
        broadcastChanges();
    }

    public void toggleFavorite(int index) {
        TaczRecipeEntry entry = getVisibleRecipe(index);
        if (entry == null) {
            return;
        }

        String id = entry.getId().toString();

        if (favoriteRecipeIds.contains(id)) {
            favoriteRecipeIds.remove(id);
        } else {
            favoriteRecipeIds.add(id);
        }

        TacticalBackpackItem.saveFavorites(backpackStack, favoriteRecipeIds);

        reloadTaczRecipeList();
        broadcastChanges();
    }

    private ItemStack createResultStack(TaczRecipeEntry entry) {
        ResourceLocation id = ResourceLocation.tryParse(entry.getResultItem());
        if (id == null) {
            return ItemStack.EMPTY;
        }

        Item item = ForgeRegistries.ITEMS.getValue(id);
        if (item == null) {
            return ItemStack.EMPTY;
        }

        return new ItemStack(item, entry.getResultCount());
    }

    private boolean hasRequiredIngredients(TaczRecipeEntry entry) {
        List<String> ingredients = entry.getIngredients();
        int storageSize = BackpackInventory.getStorageSize(backpackStack);

        List<ItemStack> available = new ArrayList<>();
        for (int i = 0; i < storageSize; i++) {
            ItemStack stack = backpackInventory.getItem(i);
            if (!stack.isEmpty()) {
                available.add(stack.copy());
            }
        }

        for (String ingredientId : ingredients) {
            ResourceLocation id = ResourceLocation.tryParse(ingredientId);
            if (id == null) {
                return false;
            }

            Item item = ForgeRegistries.ITEMS.getValue(id);
            if (item == null) {
                return false;
            }

            boolean found = false;

            for (ItemStack stack : available) {
                if (stack.is(item) && stack.getCount() > 0) {
                    stack.shrink(1);
                    found = true;
                    break;
                }
            }

            if (!found) {
                return false;
            }
        }

        return true;
    }

    public boolean hasIngredient(String ingredientId) {
        ResourceLocation id = ResourceLocation.tryParse(ingredientId);
        if (id == null) {
            return false;
        }

        Item item = ForgeRegistries.ITEMS.getValue(id);
        if (item == null) {
            return false;
        }

        int storageSize = BackpackInventory.getStorageSize(backpackStack);

        for (int i = 0; i < storageSize; i++) {
            ItemStack stack = backpackInventory.getItem(i);
            if (stack.is(item) && stack.getCount() > 0) {
                return true;
            }
        }

        return false;
    }

    public int getIngredientCount(String ingredientId) {
        ResourceLocation id = ResourceLocation.tryParse(ingredientId);
        if (id == null) {
            return 0;
        }

        Item item = ForgeRegistries.ITEMS.getValue(id);
        if (item == null) {
            return 0;
        }

        int storageSize = BackpackInventory.getStorageSize(backpackStack);
        int count = 0;

        for (int i = 0; i < storageSize; i++) {
            ItemStack stack = backpackInventory.getItem(i);
            if (stack.is(item)) {
                count += stack.getCount();
            }
        }

        return count;
    }

    public int getMaxCraftCount(TaczRecipeEntry entry) {
        if (entry == null) {
            return 0;
        }

        java.util.Map<String, Integer> requiredCounts = new java.util.LinkedHashMap<>();
        for (String ingredient : entry.getIngredients()) {
            requiredCounts.put(ingredient, requiredCounts.getOrDefault(ingredient, 0) + 1);
        }

        int max = Integer.MAX_VALUE;

        for (java.util.Map.Entry<String, Integer> req : requiredCounts.entrySet()) {
            int owned = getIngredientCount(req.getKey());
            int needed = req.getValue();

            if (needed <= 0) {
                continue;
            }

            max = Math.min(max, owned / needed);
        }

        return max == Integer.MAX_VALUE ? 0 : max;
    }

    private void consumeRequiredIngredients(TaczRecipeEntry entry) {
        List<String> ingredients = entry.getIngredients();
        int storageSize = BackpackInventory.getStorageSize(backpackStack);

        for (String ingredientId : ingredients) {
            ResourceLocation id = ResourceLocation.tryParse(ingredientId);
            if (id == null) {
                continue;
            }

            Item item = ForgeRegistries.ITEMS.getValue(id);
            if (item == null) {
                continue;
            }

            for (int i = 0; i < storageSize; i++) {
                ItemStack stack = backpackInventory.getItem(i);
                if (stack.is(item) && stack.getCount() > 0) {
                    stack.shrink(1);
                    if (stack.isEmpty()) {
                        backpackInventory.setItem(i, ItemStack.EMPTY);
                    }
                    break;
                }
            }
        }
    }

    private boolean canInsertIntoPlayerInventory(ItemStack stack) {
        ItemStack remaining = stack.copy();

        for (int i = 0; i < owner.getInventory().getContainerSize(); i++) {
            ItemStack slot = owner.getInventory().getItem(i);

            if (!slot.isEmpty()
                    && ItemStack.isSameItemSameTags(slot, remaining)
                    && slot.getCount() < slot.getMaxStackSize()) {

                int move = Math.min(
                        remaining.getCount(),
                        slot.getMaxStackSize() - slot.getCount()
                );

                remaining.shrink(move);

                if (remaining.isEmpty()) {
                    return true;
                }
            }
        }

        for (int i = 0; i < owner.getInventory().getContainerSize(); i++) {
            if (owner.getInventory().getItem(i).isEmpty()) {
                return true;
            }
        }

        return false;
    }

    private ItemStack insertIntoPlayerInventory(ItemStack stack) {
        ItemStack remaining = stack.copy();

        for (int i = 0; i < owner.getInventory().getContainerSize(); i++) {
            ItemStack slot = owner.getInventory().getItem(i);

            if (!slot.isEmpty()
                    && ItemStack.isSameItemSameTags(slot, remaining)
                    && slot.getCount() < slot.getMaxStackSize()) {

                int move = Math.min(
                        remaining.getCount(),
                        slot.getMaxStackSize() - slot.getCount()
                );

                slot.grow(move);
                remaining.shrink(move);

                if (remaining.isEmpty()) {
                    return ItemStack.EMPTY;
                }
            }
        }

        for (int i = 0; i < owner.getInventory().getContainerSize(); i++) {
            ItemStack slot = owner.getInventory().getItem(i);

            if (slot.isEmpty()) {
                owner.getInventory().setItem(i, remaining.copy());
                return ItemStack.EMPTY;
            }
        }

        return remaining;
    }

    public void craftRecipeMultiple(int recipeIndex, int count) {
        TaczRecipeEntry entry = getVisibleRecipe(recipeIndex);
        if (entry == null || count <= 0) {
            return;
        }

        int max = getMaxCraftCount(entry);
        int actual = Math.min(count, max);

        if (actual <= 0) {
            return;
        }

        ItemStack result = createResultStack(entry);
        if (result.isEmpty()) {
            return;
        }

        ItemStack output = result.copy();
        output.setCount(result.getCount() * actual);

        if (!canInsertIntoPlayerInventory(output)) {
            craftResult.setItem(0, output);
            selectedTaczRecipe = entry;
            selectedRecipeIndex = recipeIndex;
            broadcastChanges();
            return;
        }

        for (int i = 0; i < actual; i++) {
            consumeRequiredIngredients(entry);
        }

        ItemStack remaining = insertIntoPlayerInventory(output);
        craftResult.setItem(0, remaining);

        selectedTaczRecipe = entry;
        selectedRecipeIndex = recipeIndex;

        TacticalBackpackItem.saveInventory(backpackStack, backpackInventory);
        broadcastChanges();
    }

    public void applyTaczRecipeToCraftMatrix(int recipeIndex) {
        TaczRecipeEntry entry = getVisibleRecipe(recipeIndex);
        if (entry == null) {
            return;
        }

        if (!hasRequiredIngredients(entry)) {
            selectedTaczRecipe = null;
            selectedRecipeIndex = -1;
            craftResult.setItem(0, ItemStack.EMPTY);
            return;
        }

        selectedTaczRecipe = entry;
        selectedRecipeIndex = recipeIndex;

        for (int i = 0; i < craftMatrix.getContainerSize(); i++) {
            craftMatrix.setItem(i, ItemStack.EMPTY);
        }

        List<String> ingredients = entry.getIngredients();
        int max = Math.min(ingredients.size(), craftMatrix.getContainerSize());

        for (int i = 0; i < max; i++) {
            ResourceLocation id = ResourceLocation.tryParse(ingredients.get(i));
            if (id == null) {
                continue;
            }

            Item item = ForgeRegistries.ITEMS.getValue(id);
            if (item == null) {
                continue;
            }

            craftMatrix.setItem(i, new ItemStack(item, 1));
        }

        craftResult.setItem(0, createResultStack(entry));
        broadcastChanges();
    }

    @Override
    public void slotsChanged(net.minecraft.world.Container container) {
        super.slotsChanged(container);
        updateCraftResult();
    }

    private void updateCraftResult() {
        if (selectedTaczRecipe != null) {
            if (hasRequiredIngredients(selectedTaczRecipe)) {
                craftResult.setItem(0, createResultStack(selectedTaczRecipe));
            } else {
                craftResult.setItem(0, ItemStack.EMPTY);
            }

            broadcastChanges();
            return;
        }

        Level level = owner.level();

        Optional<CraftingRecipe> recipe = level.getRecipeManager()
                .getRecipeFor(RecipeType.CRAFTING, craftMatrix, level);

        if (recipe.isPresent()) {
            ItemStack result = recipe.get().assemble(craftMatrix, level.registryAccess());
            craftResult.setItem(0, result.copy());
        } else {
            craftResult.setItem(0, ItemStack.EMPTY);
        }

        broadcastChanges();
    }

    public TaczRecipeEntry getVisibleRecipe(int index) {
        if (index < 0 || index >= visibleRecipes.size()) {
            return null;
        }
        return visibleRecipes.get(index);
    }

    public void onTaczResultTaken(Player player) {
        if (selectedTaczRecipe != null) {
            consumeRequiredIngredients(selectedTaczRecipe);
            TacticalBackpackItem.saveInventory(backpackStack, backpackInventory);

            if (hasRequiredIngredients(selectedTaczRecipe)) {
                craftResult.setItem(0, createResultStack(selectedTaczRecipe));
            } else {
                craftResult.setItem(0, ItemStack.EMPTY);
                selectedTaczRecipe = null;
                selectedRecipeIndex = -1;
            }

            broadcastChanges();
        }
    }

    public void scrollRecipes(int direction) {
        recipeScrollOffset += direction;
        reloadTaczRecipeList();
        broadcastChanges();
    }

    public int getRecipeScrollOffset() {
        return recipeScrollOffset;
    }

    public int getSelectedRecipeIndex() {
        return selectedRecipeIndex;
    }

    public boolean canCraftVisibleRecipe(int index) {
        TaczRecipeEntry entry = getVisibleRecipe(index);
        if (entry == null) {
            return false;
        }

        return hasRequiredIngredients(entry);
    }

    public String getSearchText() {
        return searchText;
    }

    public void setSearchText(String searchText) {
        this.searchText = searchText == null ? "" : searchText;
        this.recipeScrollOffset = 0;
        this.selectedRecipeIndex = -1;
        this.selectedTaczRecipe = null;
        reloadTaczRecipeList();
        craftResult.setItem(0, ItemStack.EMPTY);
        broadcastChanges();
    }

    @Override
    public boolean clickMenuButton(Player player, int id) {

        if (id == 90) {
            scrollRecipes(-1);
            return true;
        }

        if (id == 91) {
            scrollRecipes(1);
            return true;
        }

        if (id == 92) {
            toggleFavoritesOnly();
            return true;
        }

        if (id >= 120 && id < 129) {
            int recipeIndex = id - 120;
            toggleFavorite(recipeIndex);
            return true;
        }

        if (id >= 140 && id < 149) {
            int recipeIndex = id - 140;
            TaczRecipeEntry entry = getVisibleRecipe(recipeIndex);

            if (entry == null) {
                return false;
            }

            int max = getMaxCraftCount(entry);
            if (max <= 0) {
                return false;
            }

            craftRecipeMultiple(recipeIndex, max);
            return true;
        }

        if (id >= 100 && id < 109) {
            int recipeIndex = id - 100;
            TaczRecipeEntry entry = getVisibleRecipe(recipeIndex);

            if (entry == null) {
                return false;
            }

            if (!hasRequiredIngredients(entry)) {
                return false;
            }

            applyTaczRecipeToCraftMatrix(recipeIndex);
            return true;
        }

        return false;
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
        TacticalBackpackItem.saveFavorites(backpackStack, favoriteRecipeIds);
    }
}