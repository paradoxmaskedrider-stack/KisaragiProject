package com.github.kisaragimikoto.tacticalbackpack.client;

import com.github.kisaragimikoto.tacticalbackpack.network.AE2ItemListPacket;

import java.util.List;

/** Client-only snapshot of the current ME browser page. */
public final class AE2ClientInventoryCache {
    private static List<AE2ItemListPacket.Entry> entries = List.of();
    private static String query = "";
    private static int page;
    private static int totalPages = 1;

    public static void accept(AE2ItemListPacket packet) {
        entries = List.copyOf(packet.entries());
        query = packet.query();
        page = packet.page();
        totalPages = packet.totalPages();
    }

    public static List<AE2ItemListPacket.Entry> entries() { return entries; }
    public static String query() { return query; }
    public static int page() { return page; }
    public static int totalPages() { return totalPages; }

    public static void clear() {
        entries = List.of();
        query = "";
        page = 0;
        totalPages = 1;
    }

    private AE2ClientInventoryCache() { }
}
