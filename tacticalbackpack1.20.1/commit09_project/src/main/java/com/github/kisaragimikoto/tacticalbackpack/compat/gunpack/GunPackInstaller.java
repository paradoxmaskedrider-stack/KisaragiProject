package com.github.kisaragimikoto.tacticalbackpack.compat.gunpack;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.Duration;
import java.time.Instant;
import java.util.Locale;

/** Downloads to a temporary file, validates it, backs up collisions, then atomically installs it. */
public final class GunPackInstaller {
    private static final HttpClient CLIENT = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(20))
            .followRedirects(HttpClient.Redirect.NORMAL)
            .build();

    public static GunPackInstallResult install(GunPackDescriptor descriptor) {
        if (descriptor == null || descriptor.downloadUri() == null) return GunPackInstallResult.failure("Missing pack metadata or download URL.");
        URI uri = descriptor.downloadUri();
        if (!"https".equalsIgnoreCase(uri.getScheme())) return GunPackInstallResult.failure("Only HTTPS downloads are allowed.");
        try {
            GunPackPaths.ensureDirectories();
            String safeName = sanitize(descriptor.name()) + "-" + sanitize(descriptor.version()) + extension(uri.getPath());
            Path partial = GunPackPaths.downloads().resolve(safeName + ".part");
            Path downloaded = GunPackPaths.downloads().resolve(safeName);
            Files.deleteIfExists(partial);

            HttpRequest request = HttpRequest.newBuilder(uri)
                    .timeout(Duration.ofMinutes(5))
                    .header("User-Agent", "TacticalBackpack-GunPackManager/1")
                    .GET().build();
            HttpResponse<Path> response = CLIENT.send(request, HttpResponse.BodyHandlers.ofFile(partial));
            if (response.statusCode() < 200 || response.statusCode() >= 300) {
                Files.deleteIfExists(partial);
                return GunPackInstallResult.failure("Download failed with HTTP " + response.statusCode() + ".");
            }
            Files.move(partial, downloaded, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
            var validation = GunPackFileValidator.validate(downloaded, descriptor.sha256());
            if (!validation.valid()) {
                Path quarantined = GunPackPaths.quarantine().resolve(downloaded.getFileName());
                Files.move(downloaded, quarantined, StandardCopyOption.REPLACE_EXISTING);
                return GunPackInstallResult.failure(validation.message() + " File moved to quarantine.");
            }

            Path deployDir = GunPackPaths.deploymentDirectory(descriptor.platform(), downloaded.getFileName().toString());
            Files.createDirectories(deployDir);
            Path target = deployDir.resolve(downloaded.getFileName());
            if (Files.exists(target)) {
                String stamp = Long.toString(Instant.now().toEpochMilli());
                Files.move(target, GunPackPaths.backupDirectory(descriptor.platform()).resolve(stamp + "-" + target.getFileName()), StandardCopyOption.REPLACE_EXISTING);
            }
            Files.move(downloaded, target, StandardCopyOption.REPLACE_EXISTING);
            GunPackInstallRegistry.record(descriptor, target);
            return new GunPackInstallResult(true, "Installed " + descriptor.name() + " for " + descriptor.platform().displayName() + ".", target, descriptor.requiresRestart());
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            return GunPackInstallResult.failure("Download was interrupted.");
        } catch (IOException ex) {
            return GunPackInstallResult.failure("Install failed: " + ex.getMessage());
        }
    }

    private static String extension(String path) {
        String lower = path == null ? "" : path.toLowerCase(Locale.ROOT);
        return lower.endsWith(".jar") ? ".jar" : ".zip";
    }

    private static String sanitize(String value) {
        String cleaned = value == null ? "pack" : value.replaceAll("[^a-zA-Z0-9._-]", "_");
        return cleaned.isBlank() ? "pack" : cleaned;
    }

    private GunPackInstaller() {}
}
