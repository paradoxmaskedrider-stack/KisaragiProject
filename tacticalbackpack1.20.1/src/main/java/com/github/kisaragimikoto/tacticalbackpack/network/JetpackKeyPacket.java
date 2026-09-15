package com.github.kisaragimikoto.tacticalbackpack.network;

import com.github.kisaragimikoto.tacticalbackpack.item.TacticalBackpackItem;
import com.github.kisaragimikoto.tacticalbackpack.registry.ModSounds;
import com.github.kisaragimikoto.tacticalbackpack.trade.FlightModeUtil;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;

import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class JetpackKeyPacket {

    private static final int MAX_HEAT = 100;
    private static final int OVERHEAT_COOLDOWN = 100;

    private final boolean pressed;

    public JetpackKeyPacket(boolean pressed) {
        this.pressed = pressed;
    }

    public static void encode(JetpackKeyPacket msg, FriendlyByteBuf buf) {
        buf.writeBoolean(msg.pressed);
    }

    public static JetpackKeyPacket decode(FriendlyByteBuf buf) {
        return new JetpackKeyPacket(buf.readBoolean());
    }

    public static void handle(JetpackKeyPacket msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayer player = ctx.get().getSender();
            if (player == null) return;

            ItemStack backpack = TacticalBackpackItem.getEquippedBackpack(player);
            if (backpack.isEmpty()) return;
            if (!FlightModeUtil.isJetpackEnabled(backpack)) return;

            CompoundTag tag = backpack.getOrCreateTag();

            if (!msg.pressed) {
                if (tag.getBoolean("JetpackActive") && player.level() instanceof ServerLevel level) {
                    level.playSound(null, player.getX(), player.getY(), player.getZ(),
                            ModSounds.JETPACK_STOP.get(), SoundSource.PLAYERS, 0.6F, 0.9F);
                }
                tag.putBoolean("JetpackActive", false);
                return;
            }

            if (player.isCreative() || player.isSpectator()) return;

            int cooldown = tag.getInt("JetpackOverheatCooldown");
            if (cooldown > 0) return;

            backpack.getCapability(ForgeCapabilities.ENERGY).ifPresent(energy -> {
                boolean hover = player.isShiftKeyDown();
                boolean boost = player.isSprinting();

                boolean jetpackMode = FlightModeUtil.isJetpack(backpack);
                boolean elytraMode = FlightModeUtil.isElytra(backpack);

                int cost = boost ? 60 : 20;
                int heatAdd = boost ? 4 : 1;

                if (energy.getEnergyStored() < cost) return;

                int heat = tag.getInt("JetpackHeat") + heatAdd;

                if (heat >= MAX_HEAT) {
                    tag.putInt("JetpackHeat", MAX_HEAT);
                    tag.putInt("JetpackOverheatCooldown", OVERHEAT_COOLDOWN);
                    tag.putBoolean("JetpackActive", false);

                    if (player.level() instanceof ServerLevel level) {
                        level.playSound(null, player.getX(), player.getY(), player.getZ(),
                                ModSounds.JETPACK_OVERHEAT.get(), SoundSource.PLAYERS, 1.0F, 0.8F);
                    }
                    return;
                }

                tag.putInt("JetpackHeat", heat);

                if (!(player.level() instanceof ServerLevel serverLevel)) return;

                if (!tag.getBoolean("JetpackActive")) {
                    serverLevel.playSound(null, player.getX(), player.getY(), player.getZ(),
                            ModSounds.JETPACK_START.get(), SoundSource.PLAYERS, 0.7F, 1.0F);
                    tag.putBoolean("JetpackActive", true);
                }

                if (player.tickCount % 10 == 0) {
                    serverLevel.playSound(null, player.getX(), player.getY(), player.getZ(),
                            boost ? ModSounds.JETPACK_BOOST.get() : ModSounds.JETPACK_LOOP.get(),
                            SoundSource.PLAYERS,
                            boost ? 0.9F : 0.45F,
                            boost ? 1.25F : 1.0F);
                }

                if (jetpackMode) {
                    if (player.onGround()) return;

                    if (hover) {
                        player.setDeltaMovement(
                                player.getDeltaMovement().x * 0.5,
                                -0.02,
                                player.getDeltaMovement().z * 0.5
                        );
                    } else if (boost) {
                        player.setDeltaMovement(
                                player.getDeltaMovement().x,
                                Math.min(player.getDeltaMovement().y + 0.16, 0.9),
                                player.getDeltaMovement().z
                        );
                    } else {
                        player.setDeltaMovement(
                                player.getDeltaMovement().x,
                                Math.min(player.getDeltaMovement().y + 0.08, 0.5),
                                player.getDeltaMovement().z
                        );
                    }

                    spawnJetpackParticles(serverLevel, player, boost);
                    player.fallDistance = 0;
                    energy.extractEnergy(cost, false);
                    return;
                }

                if (elytraMode) {
                    if (player.onGround()) return;

                    if (!player.isFallFlying()) {
                        player.startFallFlying();
                    }

                    if (boost) {
                        Vec3 look = player.getLookAngle();

                        player.setDeltaMovement(
                                player.getDeltaMovement().add(
                                        look.x * 0.15,
                                        look.y * 0.08,
                                        look.z * 0.15
                                )
                        );

                        spawnJetpackParticles(serverLevel, player, true);
                        player.fallDistance = 0;
                        energy.extractEnergy(cost, false);
                    } else {
                        spawnSmokeParticles(serverLevel, player);
                        energy.extractEnergy(5, false);
                    }
                }
            });
        });

        ctx.get().setPacketHandled(true);
    }

    private static void spawnJetpackParticles(ServerLevel level, ServerPlayer player, boolean boost) {
        double baseX = player.getX();
        double baseY = player.getY() + 0.4;
        double baseZ = player.getZ();

        double offset = 0.25;

        double leftX = baseX - offset;
        double rightX = baseX + offset;

        int smokeCount = boost ? 16 : 8;
        int flameCount = boost ? 12 : 6;

        level.sendParticles(ParticleTypes.SMOKE, leftX, baseY, baseZ,
                smokeCount, 0.15, 0.2, 0.15, 0.03);
        level.sendParticles(ParticleTypes.FLAME, leftX, baseY, baseZ,
                flameCount, 0.1, 0.1, 0.1, boost ? 0.08 : 0.04);

        level.sendParticles(ParticleTypes.SMOKE, rightX, baseY, baseZ,
                smokeCount, 0.15, 0.2, 0.15, 0.03);
        level.sendParticles(ParticleTypes.FLAME, rightX, baseY, baseZ,
                flameCount, 0.1, 0.1, 0.1, boost ? 0.08 : 0.04);
    }

    private static void spawnSmokeParticles(ServerLevel level, ServerPlayer player) {
        level.sendParticles(
                ParticleTypes.SMOKE,
                player.getX(),
                player.getY() + 0.2,
                player.getZ(),
                4,
                0.2,
                0.1,
                0.2,
                0.01
        );
    }
}