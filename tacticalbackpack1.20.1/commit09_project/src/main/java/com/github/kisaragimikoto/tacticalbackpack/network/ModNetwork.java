package com.github.kisaragimikoto.tacticalbackpack.network;

import com.github.kisaragimikoto.tacticalbackpack.TacticalBackpack;

import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;

public class ModNetwork {

    private static final String PROTOCOL_VERSION = "1";
    private static int packetId = 0;

    public static final SimpleChannel CHANNEL = NetworkRegistry.newSimpleChannel(
            new ResourceLocation(
                    TacticalBackpack.MODID,
                    "main"
            ),
            () -> PROTOCOL_VERSION,
            PROTOCOL_VERSION::equals,
            PROTOCOL_VERSION::equals
    );

    public static void register() {

        CHANNEL.registerMessage(
                packetId++,
                UpdateWorkbenchSearchPacket.class,
                UpdateWorkbenchSearchPacket::encode,
                UpdateWorkbenchSearchPacket::decode,
                UpdateWorkbenchSearchPacket::handle
        );

        CHANNEL.registerMessage(
                packetId++,
                JetpackKeyPacket.class,
                JetpackKeyPacket::encode,
                JetpackKeyPacket::decode,
                JetpackKeyPacket::handle
        );

        CHANNEL.registerMessage(
                packetId++,
                FlightModeTogglePacket.class,
                FlightModeTogglePacket::encode,
                FlightModeTogglePacket::decode,
                FlightModeTogglePacket::handle
        );

        CHANNEL.registerMessage(
                packetId++,
                JetpackTogglePacket.class,
                JetpackTogglePacket::encode,
                JetpackTogglePacket::decode,
                JetpackTogglePacket::handle
        );

        CHANNEL.registerMessage(
                packetId++,
                GunWorkbenchActionPacket.class,
                GunWorkbenchActionPacket::encode,
                GunWorkbenchActionPacket::decode,
                GunWorkbenchActionPacket::handle
        );

        CHANNEL.registerMessage(
                packetId++,
                EnterBackpackWorldPacket.class,
                EnterBackpackWorldPacket::encode,
                EnterBackpackWorldPacket::decode,
                EnterBackpackWorldPacket::handle
        );

        CHANNEL.registerMessage(
                packetId++,
                ReturnFromBackpackWorldPacket.class,
                ReturnFromBackpackWorldPacket::encode,
                ReturnFromBackpackWorldPacket::decode,
                ReturnFromBackpackWorldPacket::handle
        );

        CHANNEL.registerMessage(
                packetId++,
                BackpackTradeBuyPacket.class,
                BackpackTradeBuyPacket::encode,
                BackpackTradeBuyPacket::decode,
                BackpackTradeBuyPacket::handle
        );

        CHANNEL.registerMessage(
                packetId++,
                BackpackZoneSizePacket.class,
                BackpackZoneSizePacket::encode,
                BackpackZoneSizePacket::decode,
                BackpackZoneSizePacket::handle
        );
        
        CHANNEL.registerMessage(
                packetId++,
                BackpackTradeSellPacket.class,
                BackpackTradeSellPacket::encode,
                BackpackTradeSellPacket::decode,
                BackpackTradeSellPacket::handle
        );

        CHANNEL.registerMessage(
                packetId++,
                BackpackTradeSearchPacket.class,
                BackpackTradeSearchPacket::encode,
                BackpackTradeSearchPacket::decode,
                BackpackTradeSearchPacket::handle
        );
        
    }
}