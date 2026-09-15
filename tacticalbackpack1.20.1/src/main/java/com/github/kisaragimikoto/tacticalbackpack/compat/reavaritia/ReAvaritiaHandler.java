package com.github.kisaragimikoto.tacticalbackpack.compat.reavaritia;

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
public class ReAvaritiaHandler {

    private static final UUID REAVARITIA_HEALTH_UUID =
            UUID.fromString("d5f3b44a-4d0d-4120-9d39-4a7c1d2c6001");

    private static final UUID REAVARITIA_ARMOR_UUID =
            UUID.fromString("d5f3b44a-4d0d-4120-9d39-4a7c1d2c6002");

    private static final UUID REAVARITIA_TOUGHNESS_UUID =
            UUID.fromString("d5f3b44a-4d0d-4120-9d39-4a7c1d2c6003");

    private static final UUID REAVARITIA_DAMAGE_UUID =
            UUID.fromString("d5f3b44a-4d0d-4120-9d39-4a7c1d2c6004");

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {

        if (event.phase != TickEvent.Phase.END) {
            return;
        }

        Player player = event.player;

        if (player.level().isClientSide) {
            return;
        }

        if (!ReAvaritiaIntegration.isLoaded() || !TacticalBackpackItem.isEquipped(player)) {
            removeModifiers(player);
            return;
        }

        applyModifier(
                player.getAttribute(Attributes.MAX_HEALTH),
                REAVARITIA_HEALTH_UUID,
                "Tactical Backpack ReAvaritia Health Bonus",
                20.0
        );

        applyModifier(
                player.getAttribute(Attributes.ARMOR),
                REAVARITIA_ARMOR_UUID,
                "Tactical Backpack ReAvaritia Armor Bonus",
                20.0
        );

        applyModifier(
                player.getAttribute(Attributes.ARMOR_TOUGHNESS),
                REAVARITIA_TOUGHNESS_UUID,
                "Tactical Backpack ReAvaritia Toughness Bonus",
                20.0
        );

        applyModifier(
                player.getAttribute(Attributes.ATTACK_DAMAGE),
                REAVARITIA_DAMAGE_UUID,
                "Tactical Backpack ReAvaritia Damage Bonus",
                20.0
        );

        if (player.getHealth() > player.getMaxHealth()) {
            player.setHealth(player.getMaxHealth());
        }
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

    private static void removeModifiers(Player player) {
        remove(player.getAttribute(Attributes.MAX_HEALTH), REAVARITIA_HEALTH_UUID);
        remove(player.getAttribute(Attributes.ARMOR), REAVARITIA_ARMOR_UUID);
        remove(player.getAttribute(Attributes.ARMOR_TOUGHNESS), REAVARITIA_TOUGHNESS_UUID);
        remove(player.getAttribute(Attributes.ATTACK_DAMAGE), REAVARITIA_DAMAGE_UUID);

        if (player.getHealth() > player.getMaxHealth()) {
            player.setHealth(player.getMaxHealth());
        }
    }

    private static void remove(AttributeInstance attribute, UUID uuid) {
        if (attribute != null) {
            attribute.removeModifier(uuid);
        }
    }
}