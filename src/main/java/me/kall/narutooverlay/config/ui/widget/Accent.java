package me.kall.narutooverlay.config.ui.widget;

import me.kall.narutooverlay.config.RenderArgs;
import me.kall.narutooverlay.theme.Palette;
import org.jetbrains.annotations.NotNull;

/**
 * Accent colors of the configuration screen.
 * <p>
 * The screen used to take its accent from the Modern UI theme, which is the Material
 * "Rust" overlay for Minecraft: a pale skin tone that has nothing to do with the
 * overlay cards. Every color here is derived from the {@link Palette} of the selected
 * theme instead, so the configuration screen and the in-game overlay always share the
 * same color family.
 */
public final class Accent {

    private Accent() {}

    private static @NotNull Palette palette() {
        return RenderArgs.palette();
    }

    /**
     * The theme color, i.e. the color the selected theme tile is outlined with.
     * Themes without an accent (such as {@code CLASSIC}) fall back to white.
     */
    public static int primary() {
        Palette palette = palette();
        return palette.hasAccent() ? palette.border() | 0xFF000000 : 0xFFFFFFFF;
    }

    /**
     * Foreground color used on top of {@link #primary()} fills.
     */
    public static int onPrimary() {
        return palette().hasAccent() ? 0xFF2A1400 : 0xFF16171C;
    }

    /**
     * Destructive actions reuse the palette's critical color.
     */
    public static int danger() {
        return palette().critical() | 0xFF000000;
    }
}