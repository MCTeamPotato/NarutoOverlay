package me.kall.narutooverlay.event.fps;

import me.kall.narutooverlay.config.RenderArgs;
import me.kall.narutooverlay.event.api.TaskRegistryEvent;
import me.kall.narutooverlay.event.api.FpsUpdateEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayDeque;
import java.util.Arrays;

public final class FpsTracker {
    private static final double NANOS = 1_000_000_000.0D;
    private static final long NANOS_PER_MILLISECOND = 1_000_000L;

    private static final ArrayDeque<FrameInterval> intervals = new ArrayDeque<>();

    private static long total = 0L;

    private static long last = 0L;

    public static void listen(@NotNull TaskRegistryEvent event) {
        event.register(RenderArgs.FPS_COOLDOWN::get, RenderArgs.FPS::active, () -> {
            pruneWindow(System.nanoTime(), RenderArgs.FPS_WINDOW.get());
            if (intervals.isEmpty()) return;

            int size = intervals.size();
            double inst = NANOS / (double) intervals.peekLast().interval;
            double avg = NANOS * (double) size / (double) total;

            if (size < 10) return;

            long[] sortedIntervals = new long[size];
            int index = 0;
            for (FrameInterval interval : intervals) sortedIntervals[index++] = interval.interval;
            Arrays.sort(sortedIntervals);
            double min = NANOS / (double) sortedIntervals[size - 1];

            int lowCount = Math.max(1, (int) Math.ceil(size * 0.01D));

            double low = 0.0D;
            for (int i = size - lowCount; i < size; i++) low += NANOS / (double) sortedIntervals[i];
            low /= lowCount;

            MinecraftForge.EVENT_BUS.post(new FpsUpdateEvent(inst, min, avg, low));
        });
    }

    public static void tick(TickEvent.@NotNull RenderTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;

        long now = System.nanoTime();

        if (last != 0L) {
            long delta = now - last;
            if (delta > 0L) {
                intervals.addLast(new FrameInterval(now, delta));
                total += delta;
            }
        }

        last = now;
        pruneWindow(now, RenderArgs.FPS_WINDOW.get());
    }

    private static void pruneWindow(long now, int windowMillis) {
        long cutoff = now - windowMillis * NANOS_PER_MILLISECOND;
        while (!intervals.isEmpty() && intervals.peekFirst().timestamp < cutoff) {
            total -= intervals.removeFirst().interval;
        }
    }

    private record FrameInterval(long timestamp, long interval) {}
}
