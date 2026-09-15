package com.github.kisaragimikoto.tacticalbackpack.compat.mekanism;

import com.github.kisaragimikoto.tacticalbackpack.item.TacticalBackpackItem;

import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;

import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.UUID;

@Mod.EventBusSubscriber
public class MekanismEnergyHandler {

    private static final UUID MEKANISM_MOVEMENT_SPEED_UUID =
            UUID.fromString("d9e5d4b0-9c6a-4d0f-a61d-3c87b2220001");

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {

        if (event.phase != TickEvent.Phase.END) {
            return;
        }

        Player player = event.player;

        if (player.level().isClientSide) {
            return;
        }

        if (!MekanismIntegration.isLoaded() || !TacticalBackpackItem.isEquipped(player)) {
            removeModifier(player);
            return;
        }

        applyModifier(
                player.getAttribute(Attributes.MOVEMENT_SPEED),
                MEKANISM_MOVEMENT_SPEED_UUID,
                "Tactical Backpack Mekanism Speed Bonus",
                0.05
        );
    }

    private static void applyModifier(AttributeInstance attribute, UUID uuid, String name, double value) {
        if (attribute == null) {
            return;
        }

        attribute.removeModifier(uuid);

        if (value > 0) {
            attribute.addTransientModifier(new AttributeModifier(
                    uuid,
                    name,
                    value,
                    AttributeModifier.Operation.ADDITION
            ));
        }
    }

    private static void removeModifier(Player player) {
        AttributeInstance attr = player.getAttribute(Attributes.MOVEMENT_SPEED);
        if (attr != null) {
            attr.removeModifier(MEKANISM_MOVEMENT_SPEED_UUID);
        }
    }
}