package com.github.kisaragimikoto.tacticalbackpack.block;

import com.github.kisaragimikoto.tacticalbackpack.item.TacticalBackpackItem;
import com.github.kisaragimikoto.tacticalbackpack.menu.BackpackEquipmentMenu;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraftforge.network.NetworkHooks;

/** Physical station inside a backpack room used to edit installed attachments. */
public final class BackpackCustomizerBlock extends Block {
    public BackpackCustomizerBlock(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player,
                                 InteractionHand hand, BlockHitResult hit) {
        if (level.isClientSide) return InteractionResult.SUCCESS;
        if (!(player instanceof ServerPlayer serverPlayer)) return InteractionResult.PASS;

        String interiorId = player.getPersistentData().getString("TB_InteriorBackpackId");
        ItemStack resolved = TacticalBackpackItem.findBackpackById(player, interiorId);
        if (resolved.isEmpty()) {
            resolved = TacticalBackpackItem.getEquippedBackpack(player);
        }
        if (resolved.isEmpty()) {
            player.displayClientMessage(Component.translatable("message.tacticalbackpack.interior.no_backpack"), true);
            return InteractionResult.CONSUME;
        }
        final ItemStack backpack = resolved;

        NetworkHooks.openScreen(serverPlayer, new MenuProvider() {
            @Override
            public Component getDisplayName() {
                return Component.translatable("block.tacticalbackpack.backpack_customizer");
            }

            @Override
            public AbstractContainerMenu createMenu(int id, Inventory inventory, Player p) {
                return new BackpackEquipmentMenu(id, inventory, backpack);
            }
        }, buf -> buf.writeItem(backpack));

        return InteractionResult.CONSUME;
    }
}
