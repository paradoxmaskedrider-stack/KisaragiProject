package com.github.kisaragimikoto.tacticalbackpack.compat.ae2;

import net.minecraft.server.level.ServerPlayer;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Short-lived server-side session marker for future AE2 wireless-terminal style UI access.
 * No credentials or AE2 objects are persisted here.
 */
public final class AE2WirelessSessionManager {
    private static final Map<UUID, Session> SESSIONS = new ConcurrentHashMap<>();

    public record Session(String linkId, long expiresAtGameTime) { }

    public static void open(ServerPlayer player, AE2LinkData link, long durationTicks) {
        long ttl = Math.max(20L, durationTicks);
        SESSIONS.put(player.getUUID(), new Session(link.linkId(), player.serverLevel().getGameTime() + ttl));
    }

    public static boolean isActive(ServerPlayer player, AE2LinkData link) {
        Session session = SESSIONS.get(player.getUUID());
        if (session == null || !session.linkId().equals(link.linkId())) return false;
        if (player.serverLevel().getGameTime() > session.expiresAtGameTime()) {
            SESSIONS.remove(player.getUUID());
            return false;
        }
        return true;
    }

    public static void close(ServerPlayer player) {
        if (player != null) SESSIONS.remove(player.getUUID());
    }

    private AE2WirelessSessionManager() { }
}
