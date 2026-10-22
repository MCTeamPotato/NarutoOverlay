package me.kall.narutooverlay.util;

import me.kall.narutooverlay.config.RenderArgs;
import me.kall.narutooverlay.theme.ValueLevel;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import org.jetbrains.annotations.NotNull;

public final class Texts {
    private Texts() {}

    public static @NotNull MutableComponent label(@NotNull String text) {
        return colored(text, RenderArgs.palette().label());
    }

    public static @NotNull MutableComponent unit(@NotNull String text) {
        return colored(text, RenderArgs.palette().unit());
    }

    public static @NotNull MutableComponent separator(@NotNull String text) {
        return colored(text, RenderArgs.palette().separator());
    }

    public static @NotNull MutableComponent value(@NotNull String text) {
        return value(text, ValueLevel.NORMAL);
    }

    public static @NotNull MutableComponent value(@NotNull String text, @NotNull ValueLevel level) {
        return colored(text, RenderArgs.palette().value(level));
    }

    private static @NotNull MutableComponent colored(@NotNull String text, int rgb) {
        return Component.literal(text).withStyle(Style.EMPTY.withColor(rgb & 0xFFFFFF));
    }
}
