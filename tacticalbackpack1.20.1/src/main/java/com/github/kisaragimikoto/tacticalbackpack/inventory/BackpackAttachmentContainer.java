package com.github.kisaragimikoto.tacticalbackpack.inventory;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.item.ItemStack;

/**
 * Fixed-size attachment inventory stored separately from the variable-size
 * main backpack inventory. This prevents attachment slots from moving when a
 * capacity attachment changes the storage size.
 */
public final class BackpackAttachmentContainer extends SimpleContainer {
    public static final String ATTACHMENTS_TAG = "BackpackAttachments";

    private final ItemStack owner;
    private boolean loading;

    public BackpackAttachmentContainer(ItemStack owner) {
        super(BackpackInventory.EQUIPMENT_SLOT_COUNT);
        this.owner = owner;
        load();
    }

    private void load() {
        loading = true;
        try {
            clearContent();
            CompoundTag root = owner.getTag();
            if (root == null || !root.contains(ATTACHMENTS_TAG, Tag.TAG_LIST)) return;

            ListTag list = root.getList(ATTACHMENTS_TAG, Tag.TAG_COMPOUND);
            for (int i = 0; i < list.size(); i++) {
                CompoundTag itemTag = list.getCompound(i);
                int slot = itemTag.getInt("Slot");
                if (slot < 0 || slot >= getContainerSize()) continue;
                ItemStack stack = ItemStack.of(itemTag);
                if (!stack.isEmpty()) super.setItem(slot, stack);
            }
        } finally {
            loading = false;
        }
    }

    public void save() {
        if (owner.isEmpty()) return;
        ListTag list = new ListTag();
        for (int i = 0; i < getContainerSize(); i++) {
            ItemStack stack = getItem(i);
            if (stack.isEmpty()) continue;
            CompoundTag itemTag = new CompoundTag();
            itemTag.putInt("Slot", i);
            stack.save(itemTag);
            list.add(itemTag);
        }
        owner.getOrCreateTag().put(ATTACHMENTS_TAG, list);
    }

    @Override
    public void setChanged() {
        super.setChanged();
        if (!loading) save();
    }
}
