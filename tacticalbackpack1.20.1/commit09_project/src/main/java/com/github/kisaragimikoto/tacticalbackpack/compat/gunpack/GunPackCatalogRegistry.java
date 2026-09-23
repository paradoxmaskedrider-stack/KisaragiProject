package com.github.kisaragimikoto.tacticalbackpack.compat.gunpack;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Provider-neutral catalog. CurseForge support plugs into this registry through
 * an approved API-backed provider rather than HTML scraping.
 */
public final class GunPackCatalogRegistry {
    private static final CopyOnWriteArrayList<GunPackDescriptor> ENTRIES = new CopyOnWriteArrayList<>();

    public static void replaceAll(List<GunPackDescriptor> descriptors) {
        ENTRIES.clear();
        if (descriptors != null) ENTRIES.addAll(descriptors);
    }

    public static void register(GunPackDescriptor descriptor) {
        if (descriptor == null) return;
        ENTRIES.removeIf(existing -> existing.id().equalsIgnoreCase(descriptor.id())
                && existing.platform() == descriptor.platform());
        ENTRIES.add(descriptor);
    }

    public static List<GunPackDescriptor> browse(Optional<GunPlatform> platform, String search) {
        String needle = search == null ? "" : search.toLowerCase(Locale.ROOT).trim();
        List<GunPackDescriptor> result = new ArrayList<>();
        for (GunPackDescriptor descriptor : ENTRIES) {
            if (platform.isPresent() && descriptor.platform() != platform.get()) continue;
            if (!needle.isEmpty()) {
                String haystack = (descriptor.name() + " " + descriptor.id() + " " + descriptor.sourceName()).toLowerCase(Locale.ROOT);
                if (!haystack.contains(needle)) continue;
            }
            result.add(descriptor);
        }
        result.sort(Comparator.comparing(GunPackDescriptor::name, String.CASE_INSENSITIVE_ORDER));
        return List.copyOf(result);
    }

    public static Optional<GunPackDescriptor> find(String id, GunPlatform platform) {
        return ENTRIES.stream()
                .filter(entry -> entry.platform() == platform && entry.id().equalsIgnoreCase(id))
                .findFirst();
    }

    private GunPackCatalogRegistry() {}
}
