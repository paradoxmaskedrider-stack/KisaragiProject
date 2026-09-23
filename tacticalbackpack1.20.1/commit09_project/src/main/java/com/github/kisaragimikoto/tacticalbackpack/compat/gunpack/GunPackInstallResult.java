package com.github.kisaragimikoto.tacticalbackpack.compat.gunpack;

import java.nio.file.Path;

public record GunPackInstallResult(boolean success, String message, Path installedPath, boolean restartRequired) {
    public static GunPackInstallResult failure(String message) {
        return new GunPackInstallResult(false, message, null, false);
    }
}
