package com.github.kisaragimikoto.tacticalbackpack.trade;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import net.minecraftforge.registries.ForgeRegistries;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class BackpackTradeRegistry {

    public static List<TradeEntry> getAllTrades() {
        List<TradeEntry> trades = new ArrayList<>();

        for (Item item : ForgeRegistries.ITEMS.getValues()) {
            ItemStack stack = new ItemStack(item);

            if (stack.isEmpty()) continue;
            if (!isTradeAllowed(item, stack)) continue;

            ResourceLocation id = ForgeRegistries.ITEMS.getKey(item);
            if (id == null) continue;

            int price = BackpackTradePriceUtil.getPrice(item);

            trades.add(new TradeEntry(id.toString(), price));
        }

        trades.sort(Comparator.comparing(TradeEntry::itemId));

        return trades;
    }

    private static boolean isTradeAllowed(Item item, ItemStack stack) {
        ResourceLocation id = ForgeRegistries.ITEMS.getKey(item);
        if (id == null) return false;

        String namespace = id.getNamespace();
        String path = id.getPath();

        if (namespace.equals("tacticalbackpack")) {
            if (path.equals("trade_coin")) return false;
            if (path.equals("backpack_return_device")) return false;
            if (path.equals("backpack_zone_controller")) return false;
            if (path.equals("backpack_zone_remover")) return false;
            if (path.equals("backpack_trade_terminal")) return false;
        }

        if (path.contains("command_block")) return false;
        if (path.contains("structure_block")) return false;
        if (path.contains("jigsaw")) return false;
        if (path.contains("barrier")) return false;
        if (path.contains("debug_stick")) return false;
        if (path.contains("spawn_egg")) return false;

        return true;
    }

    public record TradeEntry(String itemId, int price) {
    }
}