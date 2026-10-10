package com.github.kisaragimikoto.tacticalbackpack.menu.slot;

import com.github.kisaragimikoto.tacticalbackpack.inventory.BackpackAttachmentHelper;
import net.minecraft.world.Container;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

/** Dedicated slot for installed backpack attachments. */
public class AttachmentSlot extends Slot {
    public AttachmentSlot(Container container, int index, int x, int y) {
        super(container, index, x, y);
    }

    @Override
    public boolean mayPlace(ItemStack stack) {
        return BackpackAttachmentHelper.isAttachment(stack);
    }

    @Override
    public int getMaxStackSize() {
        return 1;
    }
}
