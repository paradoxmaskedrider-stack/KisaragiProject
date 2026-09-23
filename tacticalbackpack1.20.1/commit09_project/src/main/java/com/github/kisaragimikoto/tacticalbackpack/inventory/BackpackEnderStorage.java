package com.github.kisaragimikoto.tacticalbackpack.inventory;

import net.minecraft.world.SimpleContainer;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.item.ItemStack;

public class BackpackEnderStorage extends SimpleContainer {

    public BackpackEnderStorage() {
        super(54);
    }

    public void save(CompoundTag tag) {

        ListTag list = new ListTag();

        for (int i = 0; i < getContainerSize(); i++) {

            ItemStack stack = getItem(i);

            if (!stack.isEmpty()) {
                CompoundTag item = new CompoundTag();
                item.putInt("Slot", i);
                stack.save(item);
                list.add(item);
            }
        }

        tag.put("EnderStorage", list);
    }

    public void load(CompoundTag tag) {

        ListTag list = tag.getList("EnderStorage", Tag.TAG_COMPOUND);

        for (int i = 0; i < list.size(); i++) {

            CompoundTag item = list.getCompound(i);
            int slot = item.getInt("Slot");

            setItem(slot, ItemStack.of(item));
        }
    }
}