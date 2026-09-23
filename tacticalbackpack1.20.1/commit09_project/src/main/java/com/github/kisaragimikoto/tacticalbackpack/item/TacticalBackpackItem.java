package com.github.kisaragimikoto.tacticalbackpack.item;

import com.github.kisaragimikoto.tacticalbackpack.compat.curios.CuriosIntegration;
import com.github.kisaragimikoto.tacticalbackpack.energy.BackpackEnergyProvider;
import com.github.kisaragimikoto.tacticalbackpack.inventory.BackpackEnderStorage;
import com.github.kisaragimikoto.tacticalbackpack.inventory.BackpackInventory;
import com.github.kisaragimikoto.tacticalbackpack.inventory.BackpackContents;
import com.github.kisaragimikoto.tacticalbackpack.facility.BackpackFacility;
import com.github.kisaragimikoto.tacticalbackpack.facility.BackpackFacilityResolver;
import com.github.kisaragimikoto.tacticalbackpack.menu.TacticalBackpackMenu;
import com.github.kisaragimikoto.tacticalbackpack.registry.ModItems;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;

import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.network.NetworkHooks;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class TacticalBackpackItem extends Item {

    public TacticalBackpackItem(Properties properties) {
        super(properties.stacksTo(1));
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);

        if (level.isClientSide) {
            return InteractionResultHolder.success(stack);
        }

        if (!(player instanceof ServerPlayer serverPlayer)) {
            return InteractionResultHolder.pass(stack);
        }

        NetworkHooks.openScreen(
                serverPlayer,
                new MenuProvider() {
                    @Override
                    public Component getDisplayName() {
                        return Component.literal("Tactical Backpack");
                    }

                    @Override
                    public AbstractContainerMenu createMenu(int windowId, Inventory inv, Player p) {
                        return new TacticalBackpackMenu(windowId, inv, stack);
                    }
                },
                buf -> buf.writeItem(stack)
        );

        return InteractionResultHolder.consume(stack);
    }

    @Override
    public ICapabilityProvider initCapabilities(ItemStack stack, @Nullable CompoundTag nbt) {
        return new BackpackEnergyProvider();
    }

    public static final String BACKPACK_ID_TAG = "BackpackId";

    /** Returns a stable id used for nested-backpack cycle protection. */
    public static String getOrCreateBackpackId(ItemStack stack) {
        CompoundTag tag = stack.getOrCreateTag();
        if (!tag.contains(BACKPACK_ID_TAG, Tag.TAG_STRING) || tag.getString(BACKPACK_ID_TAG).isBlank()) {
            tag.putString(BACKPACK_ID_TAG, UUID.randomUUID().toString());
            stack.setTag(tag);
        }
        return tag.getString(BACKPACK_ID_TAG);
    }

    public static boolean isSameBackpack(ItemStack first, ItemStack second) {
        if (first.isEmpty() || second.isEmpty()
                || !(first.getItem() instanceof TacticalBackpackItem)
                || !(second.getItem() instanceof TacticalBackpackItem)) {
            return false;
        }
        return getOrCreateBackpackId(first).equals(getOrCreateBackpackId(second));
    }

    public static int getCombinedStorageSize(ItemStack stack) {
        return BackpackContents.countStorageSlots(stack);
    }

    public static BackpackInventory getInventory(ItemStack stack) {
        // BackpackInventory performs the NBT load itself. Loading twice could clear
        // the client-side mirror during menu creation and make stored items appear blank.
        return new BackpackInventory(stack);
    }

    public static void saveInventory(ItemStack stack, BackpackInventory inventory) {
        CompoundTag tag = stack.getOrCreateTag();
        inventory.saveToNBT(tag);
        stack.setTag(tag);
    }

    public static BackpackEnderStorage getEnder(ItemStack stack) {
        BackpackEnderStorage inv = new BackpackEnderStorage();

        if (stack.hasTag()) {
            CompoundTag tag = stack.getTag();
            if (tag != null) {
                inv.load(tag);
            }
        }

        return inv;
    }

    public static void saveEnder(ItemStack stack, BackpackEnderStorage inv) {
        CompoundTag tag = stack.getOrCreateTag();
        inv.save(tag);
        stack.setTag(tag);
    }

    public static boolean isMekanismLoaded() {
        return ModList.get().isLoaded("mekanism");
    }

    public static boolean isCuriosBackpack() {
        return CuriosIntegration.isLoaded();
    }

    public static boolean hasElytra(ItemStack stack) {
        return BackpackContents.anyMatch(stack, child -> child.is(Items.ELYTRA));
    }

    public static boolean canFly(Player player, ItemStack stack) {
        return false;
    }

    public static boolean isEquipped(Player player) {
        for (ItemStack stack : player.getInventory().items) {
            if (stack.getItem() instanceof TacticalBackpackItem) {
                return true;
            }
        }

        return false;
    }

    public static ItemStack getEquippedBackpack(Player player) {
        for (ItemStack stack : player.getInventory().items) {
            if (stack.getItem() instanceof TacticalBackpackItem) {
                return stack;
            }
        }

        return ItemStack.EMPTY;
    }

    public static boolean hasAutoPickupModule(ItemStack stack) {
        return BackpackContents.anyMatch(stack, child -> child.is(ModItems.AUTO_PICKUP_MODULE.get()));
    }

    public static boolean hasFilterModule(ItemStack stack) {
        return BackpackContents.anyMatch(stack, child -> child.is(ModItems.FILTER_MODULE.get()));
    }

    public static boolean hasSortModule(ItemStack stack) {
        return BackpackContents.anyMatch(stack, child -> child.is(ModItems.SORT_MODULE.get()));
    }

    public static boolean hasWorkbenchModule(ItemStack stack) {
        return BackpackFacilityResolver.isAvailable(stack, BackpackFacility.CRAFTING);
    }

    public static boolean hasGunWorkbenchModule(ItemStack stack) {
        return BackpackContents.anyMatch(stack, child -> child.is(ModItems.GUN_WORKBENCH_MODULE.get()));
    }

    public static boolean hasEnergyCell(ItemStack stack) {
        return BackpackContents.anyMatch(stack, child -> child.is(ModItems.ENERGY_CELL_SMALL.get())
                || child.is(ModItems.ENERGY_CELL_MEDIUM.get())
                || child.is(ModItems.ENERGY_CELL_LARGE.get()));
    }

    public static void setFilterItem(ItemStack backpack, ItemStack filterStack) {
        CompoundTag tag = backpack.getOrCreateTag();
        CompoundTag filterTag = new CompoundTag();
        filterStack.save(filterTag);
        tag.put("FilterItem", filterTag);
        backpack.setTag(tag);
    }

    public static ItemStack getFilterItem(ItemStack backpack) {
        CompoundTag tag = backpack.getTag();
        if (tag == null || !tag.contains("FilterItem")) {
            return ItemStack.EMPTY;
        }

        return ItemStack.of(tag.getCompound("FilterItem"));
    }

    public static void clearFilterItem(ItemStack backpack) {
        CompoundTag tag = backpack.getTag();
        if (tag != null && tag.contains("FilterItem")) {
            tag.remove("FilterItem");
            backpack.setTag(tag);
        }
    }

    public static void saveFavorites(ItemStack stack, List<String> favorites) {
        CompoundTag tag = stack.getOrCreateTag();

        ListTag list = new ListTag();
        for (String id : favorites) {
            list.add(StringTag.valueOf(id));
        }

        tag.put("Favorites", list);
        stack.setTag(tag);
    }

    public static List<String> loadFavorites(ItemStack stack) {
        List<String> result = new ArrayList<>();

        if (!stack.hasTag()) {
            return result;
        }

        CompoundTag tag = stack.getTag();
        if (tag == null || !tag.contains("Favorites", Tag.TAG_LIST)) {
            return result;
        }

        ListTag list = tag.getList("Favorites", Tag.TAG_STRING);

        for (int i = 0; i < list.size(); i++) {
            result.add(list.getString(i));
        }

        return result;
    }
}