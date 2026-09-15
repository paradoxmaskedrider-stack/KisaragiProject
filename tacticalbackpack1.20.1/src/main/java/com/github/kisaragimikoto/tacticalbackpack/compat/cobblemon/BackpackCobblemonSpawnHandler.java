package com.github.kisaragimikoto.tacticalbackpack.compat.cobblemon;

import com.github.kisaragimikoto.tacticalbackpack.world.BackpackBiomeZoneType;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;

import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.List;

@Mod.EventBusSubscriber
public class BackpackCobblemonSpawnHandler {

    @SubscribeEvent
    public static void onLevelTick(TickEvent.LevelTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        if (!(event.level instanceof ServerLevel level)) return;
        if (!CobblemonIntegration.isLoaded()) return;
        if (!BackpackCobblemonZoneUtil.isInBackpackWorld(level)) return;

        // 5秒に1回
        if (level.getGameTime() % 100 != 0) return;

        for (ServerPlayer player : level.players()) {
            trySpawnNearPlayer(level, player);
        }
    }

    private static void trySpawnNearPlayer(ServerLevel level, ServerPlayer player) {
        BlockPos playerPos = player.blockPosition();
        BackpackBiomeZoneType zoneType = BackpackCobblemonZoneUtil.getZoneType(playerPos);

        List<String> candidates = BackpackCobblemonSpawnTable.getPokemonForZone(zoneType);
        if (candidates.isEmpty()) return;

        String pokemonName = candidates.get(level.random.nextInt(candidates.size()));

        // ここは次でCobblemon API接続に差し替える
        player.displayClientMessage(
                net.minecraft.network.chat.Component.literal(
                        "[Backpack Zone] Spawn candidate: " + pokemonName + " / " + zoneType.getDisplayName()
                ),
                true
        );
    }
}