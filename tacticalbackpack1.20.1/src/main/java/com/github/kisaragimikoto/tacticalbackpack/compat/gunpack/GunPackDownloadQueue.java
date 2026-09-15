package com.github.kisaragimikoto.tacticalbackpack.compat.gunpack;

import com.github.kisaragimikoto.tacticalbackpack.config.BackpackConfig;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Single-worker queue. Downloads never run on the Minecraft server thread.
 * Queue mutations are safe to expose to a future screen/network layer.
 */
public final class GunPackDownloadQueue {
    private static final CopyOnWriteArrayList<GunPackQueueEntry> ENTRIES = new CopyOnWriteArrayList<>();
    private static final ExecutorService WORKER = Executors.newSingleThreadExecutor(runnable -> {
        Thread thread = new Thread(runnable, "TacticalBackpack-GunPackQueue");
        thread.setDaemon(true);
        return thread;
    });
    private static final AtomicBoolean RUNNING = new AtomicBoolean(false);

    public static GunPackQueueEntry enqueue(GunPackDescriptor descriptor) {
        GunPackQueueEntry entry = GunPackQueueEntry.pending(descriptor);
        ENTRIES.add(entry);
        drainAsync();
        return entry;
    }

    public static List<GunPackQueueEntry> snapshot() {
        List<GunPackQueueEntry> copy = new ArrayList<>(ENTRIES);
        copy.sort(Comparator.comparing(GunPackQueueEntry::createdAt));
        return List.copyOf(copy);
    }

    public static Optional<GunPackQueueEntry> find(UUID id) {
        return ENTRIES.stream().filter(entry -> entry.queueId().equals(id)).findFirst();
    }

    public static boolean cancel(UUID id) {
        Optional<GunPackQueueEntry> found = find(id);
        if (found.isEmpty()) return false;
        GunPackQueueEntry entry = found.get();
        if (entry.state() != GunPackQueueState.PENDING) return false;
        replace(entry, entry.withState(GunPackQueueState.CANCELLED, "Cancelled"));
        return true;
    }

    public static void retryFailed() {
        for (GunPackQueueEntry entry : snapshot()) {
            if (entry.state() == GunPackQueueState.FAILED) {
                replace(entry, GunPackQueueEntry.pending(entry.descriptor()));
            }
        }
        drainAsync();
    }


    public static void clearFinished() {
        ENTRIES.removeIf(entry -> entry.state() == GunPackQueueState.INSTALLED
                || entry.state() == GunPackQueueState.FAILED
                || entry.state() == GunPackQueueState.CANCELLED);
    }

    private static void drainAsync() {
        if (!RUNNING.compareAndSet(false, true)) return;
        WORKER.execute(() -> {
            try {
                while (true) {
                    Optional<GunPackQueueEntry> next = ENTRIES.stream()
                            .filter(entry -> entry.state() == GunPackQueueState.PENDING)
                            .findFirst();
                    if (next.isEmpty()) break;
                    process(next.get());
                }
            } finally {
                RUNNING.set(false);
                if (ENTRIES.stream().anyMatch(entry -> entry.state() == GunPackQueueState.PENDING)) drainAsync();
            }
        });
    }

    private static void process(GunPackQueueEntry queued) {
        if (!BackpackConfig.ENABLE_GUN_PACK_MANAGER.get()) {
            replace(queued, queued.withState(GunPackQueueState.FAILED, "Gun Pack Manager is disabled."));
            return;
        }
        if (!BackpackConfig.ALLOW_GUN_PACK_DOWNLOADS.get()) {
            replace(queued, queued.withState(GunPackQueueState.FAILED, "Downloads are disabled in config."));
            return;
        }
        GunPackQueueEntry downloading = queued.withState(GunPackQueueState.DOWNLOADING, "Downloading");
        replace(queued, downloading);
        GunPackInstallResult result = GunPackInstaller.install(downloading.descriptor());
        GunPackQueueEntry current = find(downloading.queueId()).orElse(downloading);
        replace(current, current.withState(result.success() ? GunPackQueueState.INSTALLED : GunPackQueueState.FAILED, result.message()));
    }

    private static void replace(GunPackQueueEntry oldEntry, GunPackQueueEntry newEntry) {
        int index = ENTRIES.indexOf(oldEntry);
        if (index >= 0) ENTRIES.set(index, newEntry);
    }

    private GunPackDownloadQueue() {}
}
