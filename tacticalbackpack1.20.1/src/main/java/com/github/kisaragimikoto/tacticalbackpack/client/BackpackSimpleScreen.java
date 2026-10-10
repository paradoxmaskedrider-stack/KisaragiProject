package com.github.kisaragimikoto.tacticalbackpack.client;

import com.github.kisaragimikoto.tacticalbackpack.menu.BackpackTabs;
import com.github.kisaragimikoto.tacticalbackpack.network.EnterBackpackWorldPacket;
import com.github.kisaragimikoto.tacticalbackpack.network.ModNetwork;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;

/** Shared tabbed screen used by the secondary backpack menus. */
public class BackpackSimpleScreen<M extends AbstractContainerMenu> extends AbstractContainerScreen<M> {
    private final int activeTab;
    private final Component sectionTitle;

    public BackpackSimpleScreen(M menu, Inventory inventory, Component title, int activeTab, Component sectionTitle) {
        super(menu, inventory, title);
        this.activeTab = activeTab;
        this.sectionTitle = sectionTitle;
        this.imageWidth = 176;
        this.imageHeight = 240;
        this.inventoryLabelY = 132;
        this.titleLabelY = -1000; // built-in container title is intentionally hidden
    }

    @Override
    protected void init() {
        super.init();
        addBackpackTabs();
    }

    private void addBackpackTabs() {
        addTab(BackpackTabs.MAIN, 6, "screen.tacticalbackpack.tab.main");
        addTab(BackpackTabs.CRAFT, 48, "screen.tacticalbackpack.tab.craft");
        addTab(BackpackTabs.UPGRADES, 90, "screen.tacticalbackpack.tab.upgrades");
        addTab(BackpackTabs.EQUIPMENT, 132, "screen.tacticalbackpack.tab.attachments");

        addRenderableWidget(Button.builder(
                Component.translatable("screen.tacticalbackpack.enter_interior"),
                b -> {
                    ModNetwork.CHANNEL.sendToServer(new EnterBackpackWorldPacket());
                    onClose();
                })
                .bounds(leftPos + 8, topPos - 22, 160, 18).build());
    }

    private void addTab(int id, int relativeX, String key) {
        Button button = Button.builder(Component.translatable(key), b -> {
            Minecraft minecraft = Minecraft.getInstance();
            if (minecraft.gameMode != null && id != activeTab) {
                minecraft.gameMode.handleInventoryButtonClick(menu.containerId, id);
            }
        }).bounds(leftPos + relativeX, topPos + 6, 31, 20).build();
        button.active = id != activeTab;
        addRenderableWidget(button);
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        graphics.fill(leftPos, topPos, leftPos + imageWidth, topPos + imageHeight, 0xE0141414);
        graphics.fill(leftPos + 4, topPos + 31, leftPos + imageWidth - 4, topPos + imageHeight - 4, 0xC0101010);

        for (Slot slot : menu.slots) {
            drawSlotBox(graphics, leftPos + slot.x, topPos + slot.y);
        }
    }

    private static void drawSlotBox(GuiGraphics graphics, int x, int y) {
        graphics.fill(x - 1, y - 1, x + 17, y + 17, 0xFF707070);
        graphics.fill(x, y, x + 16, y + 16, 0xFF242424);
    }

    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
        // Do not render the generic "TacticalBackpack" container title.
        if (sectionTitle != null && !sectionTitle.getString().isBlank()) {
            graphics.drawString(font, sectionTitle, 8, 34, 0xFFFFFF, false);
        }
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        renderBackground(graphics);
        super.render(graphics, mouseX, mouseY, partialTick);
        renderTooltip(graphics, mouseX, mouseY);
    }
}
