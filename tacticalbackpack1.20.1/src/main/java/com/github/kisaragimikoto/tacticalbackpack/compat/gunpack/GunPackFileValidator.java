package com.github.kisaragimikoto.tacticalbackpack.compat.gunpack;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import java.util.Locale;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/** Basic safety checks. This intentionally does not execute downloaded content. */
public final class GunPackFileValidator {
    private static final long MAX_UNCOMPRESSED_BYTES = 2L * 1024L * 1024L * 1024L;
    private static final int MAX_ENTRIES = 100_000;
    private static final Set<String> DENIED_EXTENSIONS = Set.of(".exe", ".dll", ".bat", ".cmd", ".ps1", ".sh", ".com", ".scr");

    public static ValidationResult validate(Path archive, String expectedSha256) {
        try {
            if (!Files.isRegularFile(archive)) return ValidationResult.fail("Downloaded file is missing.");
            String name = archive.getFileName().toString().toLowerCase(Locale.ROOT);
            if (!(name.endsWith(".zip") || name.endsWith(".jar"))) return ValidationResult.fail("Only ZIP/JAR gun packs are accepted.");
            if (expectedSha256 != null && !expectedSha256.isBlank()) {
                String actual = sha256(archive);
                if (!actual.equalsIgnoreCase(expectedSha256.trim())) return ValidationResult.fail("SHA-256 mismatch.");
            }
            long expanded = 0L;
            int entries = 0;
            try (InputStream input = Files.newInputStream(archive); ZipInputStream zip = new ZipInputStream(input)) {
                ZipEntry entry;
                while ((entry = zip.getNextEntry()) != null) {
                    entries++;
                    if (entries > MAX_ENTRIES) return ValidationResult.fail("Archive contains too many entries.");
                    String entryName = entry.getName().replace('\\', '/');
                    if (entryName.startsWith("/") || entryName.contains("../")) return ValidationResult.fail("Archive contains an unsafe path.");
                    String lower = entryName.toLowerCase(Locale.ROOT);
                    for (String ext : DENIED_EXTENSIONS) if (lower.endsWith(ext)) return ValidationResult.fail("Archive contains a denied executable file.");
                    long size = entry.getSize();
                    if (size > 0) {
                        expanded += size;
                        if (expanded > MAX_UNCOMPRESSED_BYTES) return ValidationResult.fail("Archive expands beyond the safety limit.");
                    }
                }
            }
            return ValidationResult.ok();
        } catch (IOException | NoSuchAlgorithmException ex) {
            return ValidationResult.fail("Validation failed: " + ex.getMessage());
        }
    }

    private static String sha256(Path path) throws IOException, NoSuchAlgorithmException {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        try (InputStream input = Files.newInputStream(path)) {
            byte[] buffer = new byte[8192];
            int read;
            while ((read = input.read(buffer)) >= 0) if (read > 0) digest.update(buffer, 0, read);
        }
        return HexFormat.of().formatHex(digest.digest());
    }

    public record ValidationResult(boolean valid, String message) {
        static ValidationResult ok() { return new ValidationResult(true, "OK"); }
        static ValidationResult fail(String message) { return new ValidationResult(false, message); }
    }

    private GunPackFileValidator() {}
}
