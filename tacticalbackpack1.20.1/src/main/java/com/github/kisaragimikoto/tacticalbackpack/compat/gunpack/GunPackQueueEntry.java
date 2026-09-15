package com.github.kisaragimikoto.tacticalbackpack.compat.gunpack;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

/** Persistent queue item used by the future in-game Gun Pack Manager screen. */
public record GunPackQueueEntry(
        UUID queueId,
        GunPackDescriptor descriptor,
        GunPackQueueState state,
        String message,
        Instant createdAt,
        Instant updatedAt) {

    public GunPackQueueEntry {
        Objects.requireNonNull(queueId, "queueId");
        Objects.requireNonNull(descriptor, "descriptor");
        Objects.requireNonNull(state, "state");
        Objects.requireNonNull(createdAt, "createdAt");
        Objects.requireNonNull(updatedAt, "updatedAt");
        message = message == null ? "" : message;
    }

    public static GunPackQueueEntry pending(GunPackDescriptor descriptor) {
        Instant now = Instant.now();
        return new GunPackQueueEntry(UUID.randomUUID(), descriptor, GunPackQueueState.PENDING, "Queued", now, now);
    }

    public GunPackQueueEntry withState(GunPackQueueState newState, String newMessage) {
        return new GunPackQueueEntry(queueId, descriptor, newState, newMessage, createdAt, Instant.now());
    }
}
