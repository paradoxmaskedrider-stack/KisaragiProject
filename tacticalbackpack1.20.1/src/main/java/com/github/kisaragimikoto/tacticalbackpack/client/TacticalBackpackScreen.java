package com.github.kisaragimikoto.tacticalbackpack.client;

import com.github.kisaragimikoto.tacticalbackpack.compat.ae2.AE2Integration;
import com.github.kisaragimikoto.tacticalbackpack.menu.BackpackTabs;
import com.github.kisaragimikoto.tacticalbackpack.menu.TacticalBackpackMenu;
import com.github.kisaragimikoto.tacticalbackpack.network.AE2BackpackActionPacket;
import com.github.kisaragimikoto.tacticalbackpack.network.AE2ItemListRequestPacket;
import com.github.kisaragimikoto.tacticalbackpack.network.AE2PullStackPacket;
import com.github.kisaragimikoto.tacticalbackpack.network.ModNetwork;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.phys.BlockHitResult;

/** Main Tactical Backpack screen with AE2 management and ME item browser. */
public final class TacticalBackpackScreen extends AbstractContainerScreen<TacticalBackpackMenu> {
    private static final int LEFT_PANEL_WIDTH = 176;
    private static final int AE2_PANEL_WIDTH = 168;
    private static final int PANEL_GAP = 6;
    private static final int ME_ROWS = AE2ItemListRequestPacket.PAGE_SIZE;

    private final Button[] meItemButtons = new Button[ME_ROWS];
    private EditBox searchBox;
    private EditBox amountBox;

    public TacticalBackpackScreen(TacticalBackpackMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        this.imageWidth = LEFT_PANEL_WIDTH + PANEL_GAP + AE2_PANEL_WIDTH;
        int normalHeight = 30 + menu.getRows() * 18 + 14 + 58 + 24;
        this.imageHeight = Math.max(normalHeight, 252);
        this.inventoryLabelY = 30 + menu.getRows() * 18 + 2;
    }

    @Override
    protected void init() {
        super.init();

        int tabY = topPos + 6;
        addTab(BackpackTabs.MAIN, leftPos + 6, tabY, "screen.tacticalbackpack.tab.main");
        addTab(BackpackTabs.CRAFT, leftPos + 39, tabY, "screen.tacticalbackpack.tab.craft");
        addTab(BackpackTabs.POCKET, leftPos + 72, tabY, "screen.tacticalbackpack.tab.pocket");
        addTab(BackpackTabs.UPGRADES, leftPos + 105, tabY, "screen.tacticalbackpack.tab.upgrades");
        addTab(BackpackTabs.EQUIPMENT, leftPos + 138, tabY, "screen.tacticalbackpack.tab.equipment");

        int panelX = leftPos + LEFT_PANEL_WIDTH + PANEL_GAP + 6;
        int width = AE2_PANEL_WIDTH - 12;
        int half = (width - 3) / 2;
        int y = topPos + 30;

        addRenderableWidget(Button.builder(
                Component.translatable("screen.tacticalbackpack.ae2.link_target"),
                button -> linkLookedAtBlock())
                .bounds(panelX, y, half, 18).build());
        addRenderableWidget(Button.builder(
                Component.translatable("screen.tacticalbackpack.ae2.unlink"),
                button -> {
                    AE2ClientInventoryCache.clear();
                    sendSimple(AE2BackpackActionPacket.Action.UNLINK);
                })
                .bounds(panelX + half + 3, y, width - half - 3, 18).build());
        y += 21;

        addRenderableWidget(Button.builder(
                Component.translatable("screen.tacticalbackpack.ae2.push_all"),
                button -> {
                    sendSimple(AE2BackpackActionPacket.Action.PUSH_ALL);
                    requestPage(AE2ClientInventoryCache.page());
                })
                .bounds(panelX, y, width, 18).build());
        y += 22;

        searchBox = new EditBox(font, panelX, y, width, 18,
                Component.translatable("screen.tacticalbackpack.ae2.search"));
        searchBox.setMaxLength(128);
        searchBox.setHint(Component.translatable("screen.tacticalbackpack.ae2.search_hint"));
        addRenderableWidget(searchBox);
        y += 21;

        addRenderableWidget(Button.builder(
                Component.translatable("screen.tacticalbackpack.ae2.search_button"),
                button -> requestPage(0))
                .bounds(panelX, y, half, 18).build());
        addRenderableWidget(Button.builder(
                Component.translatable("screen.tacticalbackpack.ae2.refresh"),
                button -> requestPage(AE2ClientInventoryCache.page()))
                .bounds(panelX + half + 3, y, width - half - 3, 18).build());
        y += 22;

        for (int i = 0; i < ME_ROWS; i++) {
            final int index = i;
            Button row = Button.builder(Component.empty(), button -> pullEntry(index))
                    .bounds(panelX, y + i * 20, width, 18).build();
            row.visible = false;
            meItemButtons[i] = addRenderableWidget(row);
        }
        y += ME_ROWS * 20 + 3;

        amountBox = new EditBox(font, panelX, y, 48, 18,
                Component.translatable("screen.tacticalbackpack.ae2.amount"));
        amountBox.setMaxLength(6);
        amountBox.setValue("64");
        amountBox.setHint(Component.literal("64"));
        addRenderableWidget(amountBox);

        addRenderableWidget(Button.builder(Component.literal("<"), button -> requestPage(AE2ClientInventoryCache.page() - 1))
                .bounds(panelX + 52, y, 32, 18).build());
        addRenderableWidget(Button.builder(Component.literal(">"), button -> requestPage(AE2ClientInventoryCache.page() + 1))
                .bounds(panelX + width - 32, y, 32, 18).build());
        y += 22;

        addRenderableWidget(Button.builder(
                Component.translatable("screen.tacticalbackpack.ae2.wireless_open"),
                button -> sendSimple(AE2BackpackActionPacket.Action.WIRELESS_OPEN))
                .bounds(panelX, y, half, 18).build());
        addRenderableWidget(Button.builder(
                Component.translatable("screen.tacticalbackpack.ae2.wireless_close"),
                button -> sendSimple(AE2BackpackActionPacket.Action.WIRELESS_CLOSE))
                .bounds(panelX + half + 3, y, width - half - 3, 18).build());

        requestPage(0);
    }

    private void addTab(int id, int x, int y, String translationKey) {
        addRenderableWidget(Button.builder(Component.translatable(translationKey), button -> {
            Minecraft minecraft = Minecraft.getInstance();
            if (minecraft.gameMode != null) {
                minecraft.gameMode.handleInventoryButtonClick(menu.containerId, id);
            }
        }).bounds(x, y, 31, 20).build());
    }

    private void linkLookedAtBlock() {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.hitResult instanceof BlockHitResult blockHit) {
            ModNetwork.CHANNEL.sendToServer(AE2BackpackActionPacket.link(blockHit.getBlockPos()));
            requestPage(0);
            return;
        }
        LocalPlayer player = minecraft.player;
        if (player != null) {
            player.displayClientMessage(Component.translatable("message.tacticalbackpack.ae2.look_at_block"), false);
        }
    }

    private void requestPage(int page) {
        if (searchBox == null) return;
        ModNetwork.CHANNEL.sendToServer(new AE2ItemListRequestPacket(searchBox.getValue(), Math.max(0, page)));
    }

    private void pullEntry(int index) {
        var entries = AE2ClientInventoryCache.entries();
        if (index < 0 || index >= entries.size()) return;
        int amount = parseAmount();
        var entry = entries.get(index);
        ModNetwork.CHANNEL.sendToServer(new AE2PullStackPacket(
                entry.stack(), amount, searchBox.getValue(), AE2ClientInventoryCache.page()));
    }

    private int parseAmount() {
        try {
            return Math.max(1, Math.min(Integer.parseInt(amountBox.getValue().trim()), 64 * 256));
        } catch (NumberFormatException ignored) {
            amountBox.setValue("64");
            return 64;
        }
    }

    private void sendSimple(AE2BackpackActionPacket.Action action) {
        ModNetwork.CHANNEL.sendToServer(AE2BackpackActionPacket.simple(action));
    }

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float partialTick, int mouseX, int mouseY) {
        int left = leftPos;
        int top = topPos;
        guiGraphics.fill(left, top, left + LEFT_PANEL_WIDTH, top + imageHeight, 0xE0101010);
        guiGraphics.fill(left + LEFT_PANEL_WIDTH + PANEL_GAP, top,
                left + imageWidth, top + imageHeight, 0xE0181818);

        int storageY = top + 30;
        int visible = menu.getVisibleStorageSlots();
        for (int slot = 0; slot < visible; slot++) {
            int x = left + 8 + (slot % 9) * 18;
            int y = storageY + (slot / 9) * 18;
            drawSlotBox(guiGraphics, x, y);
        }

        int playerInventoryY = storageY + menu.getRows() * 18 + 14;
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                drawSlotBox(guiGraphics, left + 8 + col * 18, playerInventoryY + row * 18);
            }
        }
        int hotbarY = playerInventoryY + 58;
        for (int col = 0; col < 9; col++) {
            drawSlotBox(guiGraphics, left + 8 + col * 18, hotbarY);
        }
    }

    private static void drawSlotBox(GuiGraphics guiGraphics, int x, int y) {
        guiGraphics.fill(x - 1, y - 1, x + 17, y + 17, 0xFF707070);
        guiGraphics.fill(x, y, x + 16, y + 16, 0xFF242424);
    }

    @Override
    protected void renderLabels(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        guiGraphics.drawString(font, title, 8, 20, 0xFFFFFF, false);
        guiGraphics.drawString(font, playerInventoryTitle, 8, inventoryLabelY, 0xD0D0D0, false);

        int panelX = LEFT_PANEL_WIDTH + PANEL_GAP + 8;
        guiGraphics.drawString(font, Component.translatable("screen.tacticalbackpack.ae2.title"),
                panelX, 8, 0xFFFFFF, false);

        Component state;
        if (!AE2Integration.isLoaded()) {
            state = Component.translatable("screen.tacticalbackpack.ae2.not_installed");
        } else if (!menu.isAe2Linked()) {
            state = Component.translatable("screen.tacticalbackpack.ae2.not_linked");
        } else if (menu.isAe2Online()) {
            state = Component.translatable("screen.tacticalbackpack.ae2.online");
        } else {
            state = Component.translatable("screen.tacticalbackpack.ae2.offline");
        }
        guiGraphics.drawString(font, state, panelX, 19, menu.isAe2Online() ? 0x55FF55 : 0xFFAA55, false);

        String pageText = (AE2ClientInventoryCache.page() + 1) + "/" + AE2ClientInventoryCache.totalPages();
        int pageY = 30 + 21 + 22 + 21 + 22 + ME_ROWS * 20 + 7;
        guiGraphics.drawCenteredString(font, pageText, LEFT_PANEL_WIDTH + PANEL_GAP + AE2_PANEL_WIDTH / 2,
                pageY, 0xD0D0D0);
    }

    private void updateMEButtons() {
        var entries = AE2ClientInventoryCache.entries();
        for (int i = 0; i < meItemButtons.length; i++) {
            Button button = meItemButtons[i];
            if (button == null) continue;
            if (i >= entries.size()) {
                button.visible = false;
                continue;
            }
            var entry = entries.get(i);
            String name = entry.stack().getHoverName().getString();
            String amount = formatAmount(entry.amount());
            String label = name + "  x" + amount;
            if (label.length() > 24) label = label.substring(0, 21) + "...";
            button.setMessage(Component.literal(label));
            button.visible = true;
            button.active = menu.isAe2Online();
        }
    }

    private static String formatAmount(long amount) {
        if (amount >= 1_000_000_000L) return String.format("%.1fB", amount / 1_000_000_000.0D);
        if (amount >= 1_000_000L) return String.format("%.1fM", amount / 1_000_000.0D);
        if (amount >= 1_000L) return String.format("%.1fK", amount / 1_000.0D);
        return Long.toString(amount);
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        updateMEButtons();
        renderBackground(guiGraphics);
        super.render(guiGraphics, mouseX, mouseY, partialTick);
        renderTooltip(guiGraphics, mouseX, mouseY);
    }

    @Override
    public void removed() {
        AE2ClientInventoryCache.clear();
        super.removed();
    }
}
