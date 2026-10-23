package me.kall.narutooverlay.theme;

import org.jetbrains.annotations.NotNull;

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
    ));

    private final Palette palette;

    Theme(Palette palette) {
        this.palette = palette;
    }

    public @NotNull Palette palette() {
        return this.palette;
    }
}
