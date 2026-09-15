package com.github.kisaragimikoto.tacticalbackpack.menu;

import com.github.kisaragimikoto.tacticalbackpack.registry.ModMenus;
import com.github.kisaragimikoto.tacticalbackpack.trade.BackpackTradeRegistry;

import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class BackpackTradeMenu extends AbstractContainerMenu {

    private final List<BackpackTradeRegistry.TradeEntry> allTrades;
    private final List<BackpackTradeRegistry.TradeEntry> visibleTrades = new ArrayList<>();

    private int scrollIndex = 0;
    private int selectedIndex = -1;
    private String searchText = "";

    public BackpackTradeMenu(int id, Inventory playerInventory) {
        super(ModMenus.BACKPACK_TRADE.get(), id);
        this.allTrades = BackpackTradeRegistry.getAllTrades();
        updateVisibleTrades();
    }

    public BackpackTradeRegistry.TradeEntry getVisibleTrade(int index) {
        int realIndex = scrollIndex + index;
        return realIndex >= 0 && realIndex < visibleTrades.size() ? visibleTrades.get(realIndex) : null;
    }

    public int getScrollIndex() {
        return scrollIndex;
    }

    public int getSelectedIndex() {
        return selectedIndex;
    }

    public String getSearchText() {
        return searchText;
    }

    public void setSearchText(String text) {
        this.searchText = text == null ? "" : text.toLowerCase();
        this.scrollIndex = 0;
        this.selectedIndex = -1;
        updateVisibleTrades();
    }

    public BackpackTradeRegistry.TradeEntry getSelectedTrade() {
        return selectedIndex >= 0 && selectedIndex < visibleTrades.size() ? visibleTrades.get(selectedIndex) : null;
    }

    private void updateVisibleTrades() {
        visibleTrades.clear();

        for (BackpackTradeRegistry.TradeEntry entry : allTrades) {
            if (searchText.isEmpty() || entry.itemId().toLowerCase().contains(searchText)) {
                visibleTrades.add(entry);
            }
        }
    }

    private void scrollUp() {
        if (scrollIndex > 0) {
            scrollIndex--;
        }
    }

    private void scrollDown() {
        if (scrollIndex < Math.max(0, visibleTrades.size() - 9)) {
            scrollIndex++;
        }
    }

    private void selectVisible(int visibleIndex) {
        int realIndex = scrollIndex + visibleIndex;
        if (realIndex >= 0 && realIndex < visibleTrades.size()) {
            selectedIndex = realIndex;
        }
    }

    @Override
    public boolean clickMenuButton(Player player, int id) {
        if (id == 90) {
            scrollUp();
            return true;
        }

        if (id == 91) {
            scrollDown();
            return true;
        }

        if (id >= 100 && id < 109) {
            selectVisible(id - 100);
            return true;
        }

        return false;
    }

    @Override
    public boolean stillValid(Player player) {
        return true;
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        return ItemStack.EMPTY;
    }
}