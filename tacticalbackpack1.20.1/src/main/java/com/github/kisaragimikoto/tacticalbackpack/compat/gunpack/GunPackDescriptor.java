package com.github.kisaragimikoto.tacticalbackpack.compat.gunpack;

import java.net.URI;

/** Immutable metadata used by browser, queue and installer layers. */
public record GunPackDescriptor(
        String id,
        String name,
        String version,
        GunPlatform platform,
        String minecraftVersion,
        URI downloadUri,
        String sha256,
        String sourceName,
        boolean requiresRestart) {
}
