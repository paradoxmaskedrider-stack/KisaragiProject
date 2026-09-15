package com.github.kisaragimikoto.tacticalbackpack.compat.gunpack;

import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.loading.FMLLoader;

import java.util.ArrayList;
import java.util.List;

/** Loader and host-mod compatibility checks for catalog entries. */
public final class GunPackDependencyService {
    public static CheckResult check(GunPackDescriptor descriptor) {
        List<String> warnings = new ArrayList<>();
        String currentMinecraft = FMLLoader.versionInfo().mcVersion();
        if (!descriptor.minecraftVersion().isBlank()
                && !"any".equalsIgnoreCase(descriptor.minecraftVersion())
                && !currentMinecraft.equals(descriptor.minecraftVersion())) {
            warnings.add("Minecraft " + descriptor.minecraftVersion() + " required; current=" + currentMinecraft);
        }

        String hostMod = descriptor.platform().modId();
        if (!ModList.get().isLoaded(hostMod)) {
            warnings.add("Missing host mod: " + hostMod);
        }
        return new CheckResult(warnings.isEmpty(), List.copyOf(warnings));
    }

    public record CheckResult(boolean compatible, List<String> warnings) { }
    private GunPackDependencyService() { }
}
