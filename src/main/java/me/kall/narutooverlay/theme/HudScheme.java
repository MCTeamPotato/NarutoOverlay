package me.kall.narutooverlay.theme;

import org.jetbrains.annotations.NotNull;

import java.util.Locale;

/**
 * One overlay color scheme.
 * <p>
 * Built-ins live in {@link HudSchemes}; when NarutoUI is installed its shared
 * {@code MuiColorScheme} catalog is merged in, so new NarutoUI schemes become selectable here too.
 * The id is the value stored in the config.
 *
 * @param id          stable id, for example {@code JADE}
 * @param nameKey     translation key for the display name
 * @param palette     HUD palette
 * @param shimmerRamp nine ARGB shimmer stops
 */
public record HudScheme(@NotNull String id, @NotNull String nameKey, @NotNull Palette palette,
                        int @NotNull [] shimmerRamp) {
    public HudScheme {
        id = id.toUpperCase(Locale.ROOT);
        shimmerRamp = shimmerRamp.clone();
    }

    @Override
    public int @NotNull [] shimmerRamp() {
        return shimmerRamp.clone();
    }
}