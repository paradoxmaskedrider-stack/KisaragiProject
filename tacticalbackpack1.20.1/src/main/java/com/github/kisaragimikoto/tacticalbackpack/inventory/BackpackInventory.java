package com.github.kisaragimikoto.tacticalbackpack.inventory;

import com.github.kisaragimikoto.tacticalbackpack.config.BackpackConfig;
import com.github.kisaragimikoto.tacticalbackpack.registry.ModItems;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.item.ItemStack;

/** ItemStack-backed backpack inventory. The owning stack is updated whenever a slot changes. */
public class BackpackInventory extends SimpleContainer {
    public static final int EQUIPMENT_SLOT_COUNT = 6;
    public static final int UPGRADE_SLOT_COUNT = 3;
    public static final int SPECIAL_SLOT_COUNT = EQUIPMENT_SLOT_COUNT + UPGRADE_SLOT_COUNT;
    public static final String INVENTORY_TAG = "BackpackInventory";
    private static final String[] LEGACY_INVENTORY_TAGS = {"Inventory", "Items", "inventory"};

    private final ItemStack owner;
    private boolean loading;

    public BackpackInventory() {
        this(ItemStack.EMPTY, BackpackConfig.BACKPACK_SIZE.get() + SPECIAL_SLOT_COUNT);
    }

    public BackpackInventory(ItemStack stack) {
        this(stack, getSize(stack));
        loadFromNBT(stack.getTag());
    }

    private BackpackInventory(ItemStack owner, int size) {
        super(size);
        this.owner = owner;
    }

    private static int getSize(ItemStack stack) {
        return getStorageSize(stack) + SPECIAL_SLOT_COUNT;
    }

    public static int getStorageSize(ItemStack stack) {
        int base = BackpackConfig.BACKPACK_SIZE.get();
        CompoundTag tag = stack.getTag();
        if (tag != null) {
            base += Math.max(0, tag.getInt("ExtraSlots"));
            base += getUpgradeBonus(tag);
        }
        return Math.max(9, Math.min(base, 50000 - SPECIAL_SLOT_COUNT));
    }

    private static int getUpgradeBonus(CompoundTag tag) {
        int bonus = 0;
        ListTag list = tag.getList(INVENTORY_TAG, Tag.TAG_COMPOUND);
        for (int i = 0; i < list.size(); i++) {
            CompoundTag itemTag = list.getCompound(i);
            ItemStack item = ItemStack.of(itemTag);
            if (item.is(ModItems.SIZE_UPGRADE_1.get())) bonus += 27;
            else if (item.is(ModItems.SIZE_UPGRADE_2.get())) bonus += 54;
        }
        return bonus;
    }

    public static int getEquipmentSlotIndex(int storageSize, int offset) {
        return storageSize + offset;
    }

    public static int getUpgradeSlotIndex(int storageSize, int offset) {
        return storageSize + EQUIPMENT_SLOT_COUNT + offset;
    }

    public void saveToNBT(CompoundTag tag) {
        ListTag list = new ListTag();
        for (int i = 0; i < getContainerSize(); i++) {
            ItemStack item = getItem(i);
            if (!item.isEmpty()) {
                CompoundTag itemTag = new CompoundTag();
                itemTag.putInt("Slot", i);
                item.save(itemTag);
                list.add(itemTag);
            }
        }
        tag.put(INVENTORY_TAG, list);
    }

    public void loadFromNBT(CompoundTag tag) {
        loading = true;
        try {
            clearContent();
            if (tag == null) return;

            ListTag list = findInventoryList(tag);
            for (int i = 0; i < list.size(); i++) {
                CompoundTag itemTag = list.getCompound(i);
                int slot = itemTag.getInt("Slot");
                if (slot >= 0 && slot < getContainerSize()) {
                    ItemStack loaded = ItemStack.of(itemTag);
                    if (!loaded.isEmpty()) {
                        super.setItem(slot, loaded);
                    }
                }
            }
        } finally {
            loading = false;
        }
    }

    private static ListTag findInventoryList(CompoundTag tag) {
        if (tag.contains(INVENTORY_TAG, Tag.TAG_LIST)) {
            return tag.getList(INVENTORY_TAG, Tag.TAG_COMPOUND);
        }
        for (String legacyKey : LEGACY_INVENTORY_TAGS) {
            if (tag.contains(legacyKey, Tag.TAG_LIST)) {
                return tag.getList(legacyKey, Tag.TAG_COMPOUND);
            }
        }
        return new ListTag();
    }

    @Override
    public void setChanged() {
        super.setChanged();
        if (!loading && !owner.isEmpty()) {
            saveToNBT(owner.getOrCreateTag());
        }
    }
}
