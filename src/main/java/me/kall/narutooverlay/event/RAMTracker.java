package me.kall.narutooverlay.event;

import it.unimi.dsi.fastutil.longs.Long2LongFunction;
import me.kall.narutooverlay.config.RenderArgs;
import me.kall.narutooverlay.event.api.*;
import me.kall.narutooverlay.event.api.RAMUpdateEvent;
import me.kall.narutooverlay.event.api.TaskRegistryEvent;
import net.minecraftforge.common.MinecraftForge;
import org.jetbrains.annotations.NotNull;

public final class RAMTracker {
    private static final Long2LongFunction MB = bytes -> bytes / (1024L * 1024L);

    public static void listen(@NotNull TaskRegistryEvent event) {
        event.register(RenderArgs.RAM_COOLDOWN::get, RenderArgs.RAM::active, () -> {
            Runtime runtime = Runtime.getRuntime();
            long max = runtime.maxMemory();
            long total = runtime.totalMemory();
            long used = total - runtime.freeMemory();

            MinecraftForge.EVENT_BUS.post(new RAMUpdateEvent(MB.applyAsLong(max), MB.applyAsLong(total), MB.applyAsLong(used)));
        });
    }
}
