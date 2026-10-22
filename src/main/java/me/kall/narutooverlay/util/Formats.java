package me.kall.narutooverlay.util;

import org.jetbrains.annotations.NotNull;

import java.util.Locale;

public final class Formats {
    public record Quantity(@NotNull String number, @NotNull String unit) {}

    public static @NotNull String memory(long megabytes) {
        return Long.toString(megabytes);
    }

    public static @NotNull String memoryUnit() {
        return " MB";
    }

    public static @NotNull Quantity elapsed(double seconds) {
        if (seconds < 0.0D) seconds = 0.0D;
        if (seconds < 1.0D) return new Quantity(Long.toString((long) (seconds * 1000.0D)), " ms");
        if (seconds < 60.0D) return new Quantity(String.format(Locale.ROOT, "%.1f", seconds), " s");

        long whole = (long) seconds;

        if (whole < 3600L) return new Quantity(String.format(Locale.ROOT, "%d:%02d", whole / 60L, whole % 60L), " min");
        return new Quantity(String.format(Locale.ROOT, "%d:%02d", whole / 3600L, whole % 3600L / 60L), " h");
    }
}
