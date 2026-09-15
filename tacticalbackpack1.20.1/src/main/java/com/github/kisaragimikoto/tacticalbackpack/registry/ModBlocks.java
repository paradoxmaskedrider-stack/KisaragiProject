package com.github.kisaragimikoto.tacticalbackpack.registry;

import com.github.kisaragimikoto.tacticalbackpack.TacticalBackpackMod;
import com.github.kisaragimikoto.tacticalbackpack.block.BackpackTradeTerminalBlock;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;

import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModBlocks {

    public static final DeferredRegister<Block> BLOCKS =
            DeferredRegister.create(
                    ForgeRegistries.BLOCKS,
                    TacticalBackpackMod.MODID
            );

    public static final RegistryObject<Block> BACKPACK_TRADE_TERMINAL =
            BLOCKS.register(
                    "backpack_trade_terminal",
                    () -> new BackpackTradeTerminalBlock(
                            BlockBehaviour.Properties.of()
                                    .mapColor(MapColor.METAL)
                                    .strength(4.0F)
                                    .sound(SoundType.METAL)
                                    .requiresCorrectToolForDrops()
                    )
            );
}