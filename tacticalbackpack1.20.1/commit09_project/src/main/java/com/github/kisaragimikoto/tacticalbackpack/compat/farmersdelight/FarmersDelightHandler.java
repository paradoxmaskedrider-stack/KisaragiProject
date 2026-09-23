package com.github.kisaragimikoto.tacticalbackpack.compat.farmersdelight;

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
public class FarmersDelightHandler {

    private static final UUID FARMERS_HEALTH_UUID =
            UUID.fromString("f1a2b3c4-1111-2222-3333-444455556001");

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {

        if (event.phase != TickEvent.Phase.END) {
            return;
        }

        Player player = event.player;

        if (player.level().isClientSide) {
            return;
        }

        if (!FarmersDelightIntegration.isLoaded() || !TacticalBackpackItem.isEquipped(player)) {
            removeModifier(player);
            return;
        }

        applyModifier(
                player.getAttribute(Attributes.MAX_HEALTH),
                FARMERS_HEALTH_UUID,
                "Tactical Backpack Farmers Delight Bonus",
                4.0
        );

        if (player.getHealth() > player.getMaxHealth()) {
            player.setHealth(player.getMaxHealth());
        }
    }

    private static void applyModifier(AttributeInstance attribute, UUID uuid, String name, double value) {
        if (attribute == null) return;

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
        AttributeInstance attr = player.getAttribute(Attributes.MAX_HEALTH);
        if (attr != null) {
            attr.removeModifier(FARMERS_HEALTH_UUID);
        }

        if (player.getHealth() > player.getMaxHealth()) {
            player.setHealth(player.getMaxHealth());
        }
    }
}