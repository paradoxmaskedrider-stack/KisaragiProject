package com.github.kisaragimikoto.tacticalbackpack.compat.ae2;

/**
 * AE2 API entry point kept in a dedicated class so core classes can remain
 * loadable when AE2 is absent.
 */
public final class AE2ApiBridgeInstaller {
    public static void install() {
        AE2BridgeRegistry.install(new AE2ApiNetworkResolver());
    }

    private AE2ApiBridgeInstaller() { }
}
