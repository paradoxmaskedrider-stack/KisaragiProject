package com.github.kisaragimikoto.tacticalbackpack.facility;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.util.function.Predicate;

/** Vanilla facilities that can be supplied by an item stored in any nested backpack. */
public enum BackpackFacility {
    CRAFTING("crafting", stack -> stack.is(Items.CRAFTING_TABLE)),
    FURNACE("furnace", stack -> stack.is(Items.FURNACE)),
    BLAST_FURNACE("blast_furnace", stack -> stack.is(Items.BLAST_FURNACE)),
    SMOKER("smoker", stack -> stack.is(Items.SMOKER)),
    ENDER_CHEST("ender_chest", stack -> stack.is(Items.ENDER_CHEST)),
    BREWING("brewing", stack -> stack.is(Items.BREWING_STAND)),
    ENCHANTING("enchanting", stack -> stack.is(Items.ENCHANTING_TABLE)),
    ANVIL("anvil", stack -> stack.is(Items.ANVIL) || stack.is(Items.CHIPPED_ANVIL) || stack.is(Items.DAMAGED_ANVIL)),
    GRINDSTONE("grindstone", stack -> stack.is(Items.GRINDSTONE)),
    STONECUTTER("stonecutter", stack -> stack.is(Items.STONECUTTER)),
    SMITHING("smithing", stack -> stack.is(Items.SMITHING_TABLE));

    private final String key;
    private final Predicate<ItemStack> itemMatcher;

    BackpackFacility(String key, Predicate<ItemStack> itemMatcher) {
        this.key = key;
        this.itemMatcher = itemMatcher;
    }

    public String key() {
        return key;
    }

    public boolean matches(ItemStack stack) {
        return !stack.isEmpty() && itemMatcher.test(stack);
    }
}
