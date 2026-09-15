package com.github.kisaragimikoto.tacticalbackpack.compat.epicfight;

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
public class EpicFightWeaponHandler {

    private static final UUID EPICFIGHT_ATTACK_SPEED_UUID =
            UUID.fromString("e1f7c921-0f8a-4a11-9e5a-9de1a1117001");

    private static final UUID EPICFIGHT_ATTACK_DAMAGE_UUID =
            UUID.fromString("e1f7c921-0f8a-4a11-9e5a-9de1a1117002");

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {

        if (event.phase != TickEvent.Phase.END) {
            return;
        }

        Player player = event.player;

        if (player.level().isClientSide) {
            return;
        }

        if (!EpicFightIntegration.isLoaded() || !TacticalBackpackItem.isEquipped(player)) {
            removeModifiers(player);
            return;
        }

        // TODO: Backpack内のEpicFight対応武器を取得する処理を後で追加
        boolean hasEpicFightWeapon = false;

        if (!hasEpicFightWeapon) {
            removeModifiers(player);
            return;
        }

        applyModifier(
                player.getAttribute(Attributes.ATTACK_SPEED),
                EPICFIGHT_ATTACK_SPEED_UUID,
                "Tactical Backpack EpicFight Attack Speed Bonus",
                4.0
        );

        applyModifier(
                player.getAttribute(Attributes.ATTACK_DAMAGE),
                EPICFIGHT_ATTACK_DAMAGE_UUID,
                "Tactical Backpack EpicFight Damage Bonus",
                10.0
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

    private static void removeModifiers(Player player) {
        remove(player.getAttribute(Attributes.ATTACK_SPEED), EPICFIGHT_ATTACK_SPEED_UUID);
        remove(player.getAttribute(Attributes.ATTACK_DAMAGE), EPICFIGHT_ATTACK_DAMAGE_UUID);
    }

    private static void remove(AttributeInstance attribute, UUID uuid) {
        if (attribute != null) {
            attribute.removeModifier(uuid);
        }
    }
}