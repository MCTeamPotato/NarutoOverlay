package me.kall.narutooverlay.theme;

import org.jetbrains.annotations.NotNull;

public record Palette(
        int label,
        int unit,
        int separator,
        int normal,
        int warn,
        int critical,
        int backplane,
        int border,
        int accent
) {
    public int value(@NotNull ValueLevel level) {
        return switch (level) {
            case NORMAL -> this.normal;
            case WARN -> this.warn;
            case CRITICAL -> this.critical;
        };
    }

    public boolean hasAccent() {
        return (this.accent >>> 24) != 0;
    }
}
