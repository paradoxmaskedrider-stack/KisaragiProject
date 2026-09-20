package com.github.kisaragimikoto.tacticalbackpack.facility;

import com.github.kisaragimikoto.tacticalbackpack.inventory.BackpackContents;
import com.github.kisaragimikoto.tacticalbackpack.item.TacticalBackpackItem;
import com.github.kisaragimikoto.tacticalbackpack.registry.ModItems;
import net.minecraft.world.item.ItemStack;

import java.util.EnumSet;
import java.util.Set;

/** Resolves usable facilities from the root backpack and every nested backpack. */
public final class BackpackFacilityResolver {
    private BackpackFacilityResolver() {}

    public static boolean isAvailable(ItemStack rootBackpack, BackpackFacility facility) {
        if (!BackpackFacilitySettings.isEnabled(rootBackpack, facility)) {
            return false;
        }

        if (facility == BackpackFacility.CRAFTING
                && BackpackContents.anyMatch(rootBackpack, stack -> stack.is(ModItems.WORKBENCH_MODULE.get()))) {
            return true;
        }

        return BackpackContents.anyMatch(rootBackpack, facility::matches);
    }

    public static Set<BackpackFacility> resolveAvailable(ItemStack rootBackpack) {
        EnumSet<BackpackFacility> facilities = EnumSet.noneOf(BackpackFacility.class);
        if (rootBackpack.isEmpty() || !(rootBackpack.getItem() instanceof TacticalBackpackItem)) {
            return facilities;
        }
        for (BackpackFacility facility : BackpackFacility.values()) {
            if (isAvailable(rootBackpack, facility)) {
                facilities.add(facility);
            }
        }
        return facilities;
    }
}
