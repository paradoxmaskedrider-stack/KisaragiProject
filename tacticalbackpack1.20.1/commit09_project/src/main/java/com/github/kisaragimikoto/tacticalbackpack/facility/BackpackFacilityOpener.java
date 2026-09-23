package com.github.kisaragimikoto.tacticalbackpack.facility;

import com.github.kisaragimikoto.tacticalbackpack.item.TacticalBackpackItem;
import com.github.kisaragimikoto.tacticalbackpack.menu.BackpackCraftMenu;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.inventory.AnvilMenu;
import net.minecraft.world.inventory.ChestMenu;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.EnchantmentMenu;
import net.minecraft.world.inventory.GrindstoneMenu;
import net.minecraft.world.inventory.SmithingMenu;
import net.minecraft.world.inventory.StonecutterMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.network.NetworkHooks;

/** Opens portable facilities only after the nested-backpack resolver confirms availability. */
public final class BackpackFacilityOpener {
    private BackpackFacilityOpener() {}

    public static boolean open(ServerPlayer player, ItemStack backpack, BackpackFacility facility) {
        if (backpack.isEmpty() || !(backpack.getItem() instanceof TacticalBackpackItem)) {
            player.displayClientMessage(Component.literal("Tactical Backpack not found."), true);
            return false;
        }
        if (!BackpackFacilityResolver.isAvailable(backpack, facility)) {
            player.displayClientMessage(Component.literal("Facility unavailable: " + facility.key()), true);
            return false;
        }

        ContainerLevelAccess access = ContainerLevelAccess.create(player.level(), player.blockPosition());
        switch (facility) {
            case CRAFTING -> NetworkHooks.openScreen(player,
                    new SimpleMenuProvider((id, inv, p) -> new BackpackCraftMenu(id, inv, backpack),
                            Component.translatable("container.crafting")),
                    buf -> buf.writeItem(backpack));
            case ENDER_CHEST -> player.openMenu(new SimpleMenuProvider(
                    (id, inv, p) -> ChestMenu.threeRows(id, inv, player.getEnderChestInventory()),
                    Component.translatable("container.enderchest")));
            case ENCHANTING -> player.openMenu(new SimpleMenuProvider(
                    (id, inv, p) -> new EnchantmentMenu(id, inv, access),
                    Component.translatable("container.enchant")));
            case ANVIL -> player.openMenu(new SimpleMenuProvider(
                    (id, inv, p) -> new AnvilMenu(id, inv, access),
                    Component.translatable("container.repair")));
            case GRINDSTONE -> player.openMenu(new SimpleMenuProvider(
                    (id, inv, p) -> new GrindstoneMenu(id, inv, access),
                    Component.translatable("container.grindstone_title")));
            case STONECUTTER -> player.openMenu(new SimpleMenuProvider(
                    (id, inv, p) -> new StonecutterMenu(id, inv, access),
                    Component.translatable("container.stonecutter")));
            case SMITHING -> player.openMenu(new SimpleMenuProvider(
                    (id, inv, p) -> new SmithingMenu(id, inv, access),
                    Component.translatable("container.upgrade")));
            case FURNACE, BLAST_FURNACE, SMOKER, BREWING -> {
                player.displayClientMessage(Component.literal(
                        "Persistent portable processing for " + facility.key() + " is being implemented next."), true);
                return false;
            }
        }
        return true;
    }
}
