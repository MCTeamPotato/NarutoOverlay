package me.kall.narutooverlay.theme;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Built-in overlay themes.
 * <p>
 * Every fixed theme is hand-tuned for its own backplane. {@link #CUSTOM} has no fixed palette: the
 * player picks an accent and a backplane in the appearance screen and the remaining colors are
 * derived by {@link ThemeColors#customPalette(int, int)}.
 */
public enum Theme {
    NARUTO(new Palette(
            0xFF8F2A,
            0xE0A64A,
            0xA03A26,
            0xFFFFFF,
            0xFFD64A,
            0xFF4A38,
            0xB01E0A08,
            0xCCE33A26,
            0xFFFFC42E
    )),

    CLASSIC(new Palette(
            0xFFFFFF,
            0xFFFFFF,
            0xFFFFFF,
            0xFFFFFF,
            0xFFFF55,
            0xFF5555,
            0xA0404040,
            0xFFFFFFFF,
            0x00000000
    )),

    JADE(new Palette(
            0xFF9DB8AC,
            0xFF9DB8AC,
            0xFF2B463C,
            0xFFE7F6EF,
            0xFFFFC857,
            0xFFFF6B6B,
            0xB00B1512,
            0xCC3DDC97,
            0xFF3DDC97
    )),

    AZURE(new Palette(
            0xFFA3B6CF,
            0xFFA3B6CF,
            0xFF2B3D55,
            0xFFE8F1FF,
            0xFFFFC857,
            0xFFFF6B6B,
            0xB00A111C,
            0xCC5AA9FF,
            0xFF5AA9FF
    )),

    SAKURA(new Palette(
            0xFFC9A9B6,
            0xFFC9A9B6,
            0xFF4A2E3A,
            0xFFFDEBF1,
            0xFFFFD24A,
            0xFFFF6B6B,
            0xB0150E13,
            0xCCFF8FB3,
            0xFFFF8FB3
    )),

    GRAPHITE(new Palette(
            0xFFA9AEB8,
            0xFFA9AEB8,
            0xFF3A3E47,
            0xFFF2F3F6,
            0xFFFFC857,
            0xFFFF6B6B,
            0xB00E0F12,
            0xCCD6D9E0,
            0xFFD6D9E0
    )),

    CUSTOM(null);

    private final @Nullable Palette palette;

    Theme(@Nullable Palette palette) {
        this.palette = palette;
    }

    /**
     * @return the fixed palette, or {@code null} for {@link #CUSTOM}
     */
    public @Nullable Palette palette() {
        return this.palette;
    }

    /**
     * @return whether this theme needs player-selected colors
     */
    public boolean custom() {
        return this == CUSTOM;
    }
}