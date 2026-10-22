package me.kall.narutooverlay.theme;

import org.jetbrains.annotations.NotNull;

public enum Theme {
    NARUTO(new Palette(
            0xE8934A,
            0xB07A45,
            0x6E5A48,
            0xFFFFFF,
            0xFFD400,
            0xFF3B5C,
            0xB01B1C21,
            0xCCFF8A1F,
            0xFFFFD400
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
