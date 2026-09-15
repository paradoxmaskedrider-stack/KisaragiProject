package com.github.kisaragimikoto.tacticalbackpack.compat.gunpack;

import java.util.ArrayList;
import java.util.List;

/** Small dependency-free comparator for catalog version labels. */
public final class GunPackVersion {
    public static boolean newerThan(String candidate, String installed) {
        return compare(candidate, installed) > 0;
    }

    public static int compare(String left, String right) {
        List<String> a = tokens(left);
        List<String> b = tokens(right);
        int size = Math.max(a.size(), b.size());
        for (int i = 0; i < size; i++) {
            String x = i < a.size() ? a.get(i) : "0";
            String y = i < b.size() ? b.get(i) : "0";
            int result;
            if (digits(x) && digits(y)) {
                result = Integer.compare(parse(x), parse(y));
            } else result = x.compareToIgnoreCase(y);
            if (result != 0) return result;
        }
        return 0;
    }

    private static List<String> tokens(String value) {
        String normalized = value == null ? "" : value.trim().replaceFirst("^[vV]", "");
        String[] split = normalized.split("[^A-Za-z0-9]+");
        List<String> result = new ArrayList<>();
        for (String part : split) if (!part.isBlank()) result.add(part);
        return result;
    }
    private static boolean digits(String value) { return value.chars().allMatch(Character::isDigit); }
    private static int parse(String value) { try { return Integer.parseInt(value); } catch (NumberFormatException ignored) { return Integer.MAX_VALUE; } }
    private GunPackVersion() { }
}
