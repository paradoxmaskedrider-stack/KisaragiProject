package com.github.kisaragimikoto.tacticalbackpack.compat.tconstruct;

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
public class TinkersToolHandler {

    private static final UUID TINKERS_DAMAGE_UUID =
            UUID.fromString("c3f0a14a-99a5-4a29-b4d1-70bb3a1b5001");

    private static final UUID TINKERS_ATTACK_SPEED_UUID =
            UUID.fromString("70a2c321-4f8e-4a2f-a09f-6c3b601b5002");

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {

        if (event.phase != TickEvent.Phase.END) {
            return;
        }

        Player player = event.player;

        if (player.level().isClientSide) {
            return;
        }

        if (!TinkersIntegration.isLoaded()) {
            removeModifiers(player);
            return;
        }

        if (!TacticalBackpackItem.isEquipped(player)) {
            removeModifiers(player);
            return;
        }

        // TODO: Backpack内のTinkersツールを取得する処理を後で追加
        boolean hasTinkersTool = false;

        if (!hasTinkersTool) {
            removeModifiers(player);
            return;
        }

        applyModifier(
                player.getAttribute(Attributes.ATTACK_DAMAGE),
                TINKERS_DAMAGE_UUID,
                "Tactical Backpack Tinkers Damage Bonus",
                8.0
        );

        applyModifier(
                player.getAttribute(Attributes.ATTACK_SPEED),
                TINKERS_ATTACK_SPEED_UUID,
                "Tactical Backpack Tinkers Attack Speed Bonus",
                3.0
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
        AttributeInstance damage = player.getAttribute(Attributes.ATTACK_DAMAGE);
        if (damage != null) {
            damage.removeModifier(TINKERS_DAMAGE_UUID);
        }

        AttributeInstance speed = player.getAttribute(Attributes.ATTACK_SPEED);
        if (speed != null) {
            speed.removeModifier(TINKERS_ATTACK_SPEED_UUID);
        }
    }
}