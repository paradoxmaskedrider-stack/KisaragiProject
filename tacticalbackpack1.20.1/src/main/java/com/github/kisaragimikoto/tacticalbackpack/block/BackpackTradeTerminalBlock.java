package com.github.kisaragimikoto.tacticalbackpack.block;

import com.github.kisaragimikoto.tacticalbackpack.menu.BackpackTradeMenu;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

import net.minecraftforge.network.NetworkHooks;

public class BackpackTradeTerminalBlock extends Block {

    public BackpackTradeTerminalBlock(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult use(
            BlockState state,
            Level level,
            BlockPos pos,
            Player player,
            InteractionHand hand,
            BlockHitResult hit
    ) {

        if (level.isClientSide) {
            return InteractionResult.SUCCESS;
        }

        if (!(player instanceof ServerPlayer serverPlayer)) {
            return InteractionResult.PASS;
        }

        MenuProvider provider = new SimpleMenuProvider(
                new MenuProvider() {

                    @Override
                    public Component getDisplayName() {
                        return Component.literal("Backpack Market");
                    }

                    @Override
                    public AbstractContainerMenu createMenu(
                            int windowId,
                            net.minecraft.world.entity.player.Inventory inventory,
                            Player p
                    ) {
                        return new BackpackTradeMenu(windowId, inventory);
                    }
                },
                Component.literal("Backpack Market")
        );

        NetworkHooks.openScreen(serverPlayer, provider);

        return InteractionResult.CONSUME;
    }
}