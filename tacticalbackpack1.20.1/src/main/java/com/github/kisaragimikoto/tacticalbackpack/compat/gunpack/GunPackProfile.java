package com.github.kisaragimikoto.tacticalbackpack.compat.gunpack;

import java.util.List;

/** Named cross-platform selection of Gun Packs. */
public record GunPackProfile(String name, List<Entry> entries) {
    public GunPackProfile {
        name = name == null ? "" : name.trim();
        entries = entries == null ? List.of() : List.copyOf(entries);
    }

    public record Entry(GunPlatform platform, String fileName) {
        public Entry {
            if (platform == null) throw new IllegalArgumentException("platform");
            fileName = fileName == null ? "" : fileName.trim();
        }
    }
}
