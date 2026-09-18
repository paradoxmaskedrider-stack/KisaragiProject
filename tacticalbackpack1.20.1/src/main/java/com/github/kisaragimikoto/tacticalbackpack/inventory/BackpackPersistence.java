package com.github.kisaragimikoto.tacticalbackpack.inventory;

import com.github.kisaragimikoto.tacticalbackpack.util.BackpackTags;
import com.github.kisaragimikoto.tacticalbackpack.util.BackpackVersion;
import net.minecraft.nbt.CompoundTag;

public final class BackpackPersistence {

    private BackpackPersistence(){}

    public static void initialize(CompoundTag tag){

        if(!tag.contains(BackpackTags.VERSION)){
            tag.putInt(
                    BackpackTags.VERSION,
                    BackpackVersion.CURRENT
            );
        }

        if(!tag.contains(BackpackTags.INVENTORY)){
            tag.put(
                    BackpackTags.INVENTORY,
                    new net.minecraft.nbt.ListTag()
            );
        }

    }

    public static void save(
            BackpackInventory inventory,
            CompoundTag tag){

        inventory.saveToNBT(tag);

    }

    public static void load(
            BackpackInventory inventory,
            CompoundTag tag){

        inventory.loadFromNBT(tag);

    }

}