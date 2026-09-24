package com.github.kisaragimikoto.tacticalbackpack.client.gunpack;

import com.github.kisaragimikoto.tacticalbackpack.compat.gunpack.GunPackCatalogFile;
import com.github.kisaragimikoto.tacticalbackpack.compat.gunpack.GunPackCatalogRegistry;
import com.github.kisaragimikoto.tacticalbackpack.compat.gunpack.GunPackDependencyService;
import com.github.kisaragimikoto.tacticalbackpack.compat.gunpack.GunPackDescriptor;
import com.github.kisaragimikoto.tacticalbackpack.compat.gunpack.GunPackDownloadQueue;
import com.github.kisaragimikoto.tacticalbackpack.compat.gunpack.GunPackManager;
import com.github.kisaragimikoto.tacticalbackpack.compat.gunpack.GunPackPaths;
import com.github.kisaragimikoto.tacticalbackpack.compat.gunpack.GunPackQueueEntry;
import com.github.kisaragimikoto.tacticalbackpack.compat.gunpack.GunPackQueueState;
import com.github.kisaragimikoto.tacticalbackpack.compat.gunpack.GunPlatform;
import net.minecraft.Util;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import java.util.List;
import java.util.Optional;

/**
 * Unified client-side manager for the four supported gun ecosystems.
 * The screen only mutates local client files and never sends download URLs or
 * provider credentials through the Minecraft network channel.
 */
public final class GunPackManagerScreen extends Screen {
    private enum View { CATALOG, INSTALLED, QUEUE }

    private GunPlatform platform = GunPlatform.TACZ;
    private View view = View.CATALOG;
    private EditBox search;
    private String status = "";
    private int selectedIndex = -1;
    private int scroll = 0;

    public GunPackManagerScreen() {
        super(Component.translatable("screen.tacticalbackpack.gunpacks.title"));
    }

    @Override
    protected void init() {
        rebuildWidgets();
    }

    @Override
    protected void rebuildWidgets() {
        clearWidgets();
        int center = width / 2;
        int top = 24;

        addRenderableWidget(Button.builder(Component.literal("TaCZ"), b -> switchPlatform(GunPlatform.TACZ))
                .bounds(center - 200, top, 96, 20).build());
        addRenderableWidget(Button.builder(Component.literal("Point Blank"), b -> switchPlatform(GunPlatform.POINT_BLANK))
                .bounds(center - 100, top, 96, 20).build());
        addRenderableWidget(Button.builder(Component.literal("Elite X"), b -> switchPlatform(GunPlatform.ELITE_X_QUALITY_GUNS))
                .bounds(center, top, 96, 20).build());
        addRenderableWidget(Button.builder(Component.literal("GunsmithLib"), b -> switchPlatform(GunPlatform.GUNSMITHLIB))
                .bounds(center + 100, top, 96, 20).build());

        int viewY = top + 24;
        addRenderableWidget(Button.builder(Component.translatable("screen.tacticalbackpack.gunpacks.catalog"), b -> switchView(View.CATALOG))
                .bounds(center - 150, viewY, 96, 20).build());
        addRenderableWidget(Button.builder(Component.translatable("screen.tacticalbackpack.gunpacks.installed"), b -> switchView(View.INSTALLED))
                .bounds(center - 48, viewY, 96, 20).build());
        addRenderableWidget(Button.builder(Component.translatable("screen.tacticalbackpack.gunpacks.queue"), b -> switchView(View.QUEUE))
                .bounds(center + 54, viewY, 96, 20).build());

        if (view == View.CATALOG) {
            search = new EditBox(font, center - 200, viewY + 26, 300, 20, Component.translatable("screen.tacticalbackpack.gunpacks.search"));
            search.setMaxLength(96);
            search.setResponder(value -> { selectedIndex = -1; scroll = 0; });
            addRenderableWidget(search);
            addRenderableWidget(Button.builder(Component.translatable("screen.tacticalbackpack.gunpacks.reload"), b -> reloadCatalog())
                    .bounds(center + 106, viewY + 26, 94, 20).build());
            addRenderableWidget(Button.builder(Component.translatable("screen.tacticalbackpack.gunpacks.install"), b -> installSelected())
                    .bounds(center - 200, height - 48, 120, 20).build());
        } else if (view == View.INSTALLED) {
            addRenderableWidget(Button.builder(Component.translatable("screen.tacticalbackpack.gunpacks.toggle"), b -> toggleSelected())
                    .bounds(center - 200, height - 48, 120, 20).build());
            addRenderableWidget(Button.builder(Component.translatable("screen.tacticalbackpack.gunpacks.uninstall"), b -> uninstallSelected())
                    .bounds(center - 74, height - 48, 120, 20).build());
        } else {
            addRenderableWidget(Button.builder(Component.translatable("screen.tacticalbackpack.gunpacks.cancel"), b -> cancelSelected())
                    .bounds(center - 200, height - 48, 120, 20).build());
            addRenderableWidget(Button.builder(Component.translatable("screen.tacticalbackpack.gunpacks.retry"), b -> {
                GunPackDownloadQueue.retryFailed();
                status = Component.translatable("screen.tacticalbackpack.gunpacks.retry_done").getString();
            }).bounds(center - 74, height - 48, 120, 20).build());
            addRenderableWidget(Button.builder(Component.translatable("screen.tacticalbackpack.gunpacks.clear"), b -> {
                GunPackDownloadQueue.clearFinished();
                selectedIndex = -1;
            }).bounds(center + 52, height - 48, 120, 20).build());
        }

        addRenderableWidget(Button.builder(Component.translatable("screen.tacticalbackpack.gunpacks.folder"), b -> openFolder())
                .bounds(center + 80, height - 48, 120, 20).build());
        if (view == View.QUEUE) {
            // Queue view already occupies this slot; move folder button one row up.
            removeWidget(children().get(children().size() - 1));
            addRenderableWidget(Button.builder(Component.translatable("screen.tacticalbackpack.gunpacks.folder"), b -> openFolder())
                    .bounds(center + 80, height - 72, 120, 20).build());
        }
        addRenderableWidget(Button.builder(Component.translatable("gui.done"), b -> onClose())
                .bounds(center - 50, height - 24, 100, 20).build());
    }

    private void switchPlatform(GunPlatform value) {
        platform = value;
        selectedIndex = -1;
        scroll = 0;
        status = "";
        rebuildWidgets();
    }

    private void switchView(View value) {
        view = value;
        selectedIndex = -1;
        scroll = 0;
        status = "";
        rebuildWidgets();
    }

    private List<GunPackDescriptor> catalogRows() {
        return GunPackCatalogRegistry.browse(Optional.of(platform), search == null ? "" : search.getValue());
    }

    private List<GunPackManager.InstalledPack> installedRows() {
        return GunPackManager.installed(platform);
    }

    private List<GunPackQueueEntry> queueRows() {
        return GunPackDownloadQueue.snapshot().stream()
                .filter(entry -> entry.descriptor().platform() == platform)
                .toList();
    }

    private int rowCount() {
        return switch (view) {
            case CATALOG -> catalogRows().size();
            case INSTALLED -> installedRows().size();
            case QUEUE -> queueRows().size();
        };
    }

    private void reloadCatalog() {
        GunPackCatalogFile.LoadResult result = GunPackCatalogFile.reload();
        selectedIndex = -1;
        status = result.success()
                ? Component.translatable("screen.tacticalbackpack.gunpacks.catalog_loaded", result.loaded()).getString()
                : Component.translatable("screen.tacticalbackpack.gunpacks.catalog_error", String.join("; ", result.errors())).getString();
    }

    private void installSelected() {
        List<GunPackDescriptor> rows = catalogRows();
        if (selectedIndex < 0 || selectedIndex >= rows.size()) return;
        GunPackDescriptor descriptor = rows.get(selectedIndex);
        GunPackDependencyService.CheckResult dependency = GunPackDependencyService.check(descriptor);
        if (!dependency.compatible()) {
            status = String.join("; ", dependency.warnings());
            return;
        }
        GunPackDownloadQueue.enqueue(descriptor);
        status = Component.translatable("screen.tacticalbackpack.gunpacks.queued", descriptor.name()).getString();
        view = View.QUEUE;
        selectedIndex = -1;
        rebuildWidgets();
    }

    private void toggleSelected() {
        List<GunPackManager.InstalledPack> rows = installedRows();
        if (selectedIndex < 0 || selectedIndex >= rows.size()) return;
        GunPackManager.InstalledPack pack = rows.get(selectedIndex);
        GunPackManager.OperationResult result = GunPackManager.setEnabled(pack, !pack.enabled());
        status = result.message();
        selectedIndex = -1;
    }

    private void uninstallSelected() {
        List<GunPackManager.InstalledPack> rows = installedRows();
        if (selectedIndex < 0 || selectedIndex >= rows.size()) return;
        GunPackManager.OperationResult result = GunPackManager.uninstall(rows.get(selectedIndex));
        status = result.message();
        selectedIndex = -1;
    }

    private void cancelSelected() {
        List<GunPackQueueEntry> rows = queueRows();
        if (selectedIndex < 0 || selectedIndex >= rows.size()) return;
        GunPackQueueEntry entry = rows.get(selectedIndex);
        status = GunPackDownloadQueue.cancel(entry.queueId())
                ? Component.translatable("screen.tacticalbackpack.gunpacks.cancelled").getString()
                : Component.translatable("screen.tacticalbackpack.gunpacks.cancel_failed").getString();
    }

    private void openFolder() {
        try {
            GunPackPaths.ensureDirectories();
            Util.getPlatform().openUri(GunPackPaths.primaryInstallDirectory(platform).toUri());
        } catch (Exception exception) {
            status = exception.getMessage() == null ? exception.getClass().getSimpleName() : exception.getMessage();
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 0) {
            int startY = listStartY();
            int visible = visibleRows();
            if (mouseX >= width / 2.0 - 200 && mouseX <= width / 2.0 + 200
                    && mouseY >= startY && mouseY < startY + visible * 18) {
                int row = (int) ((mouseY - startY) / 18) + scroll;
                if (row >= 0 && row < rowCount()) selectedIndex = row;
                return true;
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double delta) {
        int max = Math.max(0, rowCount() - visibleRows());
        scroll = Math.max(0, Math.min(max, scroll + (delta < 0 ? 1 : -1)));
        return true;
    }

    private int listStartY() {
        return view == View.CATALOG ? 104 : 78;
    }

    private int visibleRows() {
        return Math.max(1, (height - listStartY() - 82) / 18);
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        renderBackground(graphics);
        graphics.drawCenteredString(font, title, width / 2, 8, 0xFFFFFF);
        graphics.drawString(font, Component.literal(platform.displayName()), width / 2 - 200, 66, 0xA0A0A0, false);

        int y = listStartY();
        int visible = visibleRows();
        int count = rowCount();
        for (int slot = 0; slot < visible; slot++) {
            int index = scroll + slot;
            if (index >= count) break;
            boolean selected = index == selectedIndex;
            int background = selected ? 0x804060A0 : (slot % 2 == 0 ? 0x40101010 : 0x40202020);
            graphics.fill(width / 2 - 200, y + slot * 18, width / 2 + 200, y + slot * 18 + 17, background);
            graphics.drawString(font, rowText(index), width / 2 - 196, y + slot * 18 + 5, 0xFFFFFF, false);
        }

        if (count == 0) {
            graphics.drawCenteredString(font, Component.translatable("screen.tacticalbackpack.gunpacks.empty"), width / 2, y + 18, 0xA0A0A0);
        }
        if (!status.isBlank()) {
            graphics.drawCenteredString(font, Component.literal(status), width / 2, height - 84, 0xFFD966);
        }
        super.render(graphics, mouseX, mouseY, partialTick);
    }

    private Component rowText(int index) {
        return switch (view) {
            case CATALOG -> {
                GunPackDescriptor descriptor = catalogRows().get(index);
                String update = switch (GunPackManager.updateState(descriptor)) {
                    case NOT_INSTALLED -> "";
                    case CURRENT -> "  [installed]";
                    case UPDATE_AVAILABLE -> "  [update]";
                };
                yield Component.literal(descriptor.name() + "  v" + descriptor.version() + "  • " + descriptor.sourceName() + update);
            }
            case INSTALLED -> {
                GunPackManager.InstalledPack pack = installedRows().get(index);
                String state = pack.enabled() ? "ON" : "OFF";
                yield Component.literal("[" + state + "] " + pack.fileName() + "  • " + humanSize(pack.sizeBytes()) + "  • v" + pack.version());
            }
            case QUEUE -> {
                GunPackQueueEntry entry = queueRows().get(index);
                yield Component.literal("[" + stateLabel(entry.state()) + "] " + entry.descriptor().name() + "  • " + entry.message());
            }
        };
    }

    private static String stateLabel(GunPackQueueState state) {
        return switch (state) {
            case PENDING -> "WAIT";
            case DOWNLOADING -> "DOWN";
            case VALIDATING -> "CHECK";
            case INSTALLING -> "INSTALL";
            case INSTALLED -> "DONE";
            case FAILED -> "FAIL";
            case CANCELLED -> "CANCEL";
        };
    }

    private static String humanSize(long bytes) {
        if (bytes < 1024) return bytes + " B";
        double kib = bytes / 1024.0;
        if (kib < 1024) return String.format(java.util.Locale.ROOT, "%.1f KiB", kib);
        return String.format(java.util.Locale.ROOT, "%.1f MiB", kib / 1024.0);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
