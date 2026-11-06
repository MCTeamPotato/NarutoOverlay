package me.kall.narutooverlay.theme;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.regex.Pattern;

/**
 * Color token parsing and palette derivation for the overlay themes.
 * <p>
 * Fixed themes carry hand-tuned palettes. The {@code CUSTOM} theme only receives an accent and a
 * backplane from the player; the label, unit, separator, normal, border and status colors are
 * derived here so a custom scheme cannot end up with unreadable text or an invisible divider.
 */
public final class ThemeColors {
    /** Text color used on light surfaces. */
    public static final int DARK_TEXT = 0xFF14161A;
    /** Text color used on dark surfaces. */
    public static final int LIGHT_TEXT = 0xFFF5F6F8;
    /** Warning color shared by the fixed palettes. */
    public static final int WARNING = 0xFFFFC42E;
    /** Critical color shared by the fixed palettes. */
    public static final int CRITICAL = 0xFFFF4A38;

    private static final Pattern COLOR_TOKEN = Pattern.compile("#?(?:[0-9a-fA-F]{6}|[0-9a-fA-F]{8})");
    private static final float[] SHIMMER_STOPS = {0.0F, 0.12F, 0.25F, 0.40F, 0.55F, 0.70F, 0.82F, 0.92F, 1.0F};
    private static final int[] NEUTRAL_SHIMMER = {
            0xFFFFFFFF, 0xFFE6E6E6, 0xFFD0D0D0, 0xFFB8B8B8, 0xFF8E8E8E,
            0xFFB8B8B8, 0xFFD0D0D0, 0xFFE6E6E6, 0xFFFFFFFF
    };

    private ThemeColors() {}

    /**
     * Forces a color opaque.
     *
     * @param color ARGB color
     * @return the opaque color
     */
    public static int opaque(int color) {
        return color | 0xFF000000;
    }

    /**
     * Linearly blends two colors.
     *
     * @param from   start color
     * @param to     end color
     * @param amount blend amount, clamped to {@code 0..1}
     * @return the blended color
     */
    public static int mix(int from, int to, double amount) {
        double a = Math.max(0.0D, Math.min(1.0D, amount));
        int alpha = (int) Math.round(((from >>> 24) & 0xFF) + (((to >>> 24) & 0xFF) - ((from >>> 24) & 0xFF)) * a);
        int red = (int) Math.round(((from >> 16) & 0xFF) + (((to >> 16) & 0xFF) - ((from >> 16) & 0xFF)) * a);
        int green = (int) Math.round(((from >> 8) & 0xFF) + (((to >> 8) & 0xFF) - ((from >> 8) & 0xFF)) * a);
        int blue = (int) Math.round((from & 0xFF) + ((to & 0xFF) - (from & 0xFF)) * a);
        return (alpha << 24) | (red << 16) | (green << 8) | blue;
    }

    /**
     * Returns the relative luminance of a color, ignoring alpha.
     *
     * @param color ARGB color
     * @return luminance in {@code 0..1}
     */
    public static double luminance(int color) {
        double red = linear((color >> 16) & 0xFF);
        double green = linear((color >> 8) & 0xFF);
        double blue = linear(color & 0xFF);
        return 0.2126D * red + 0.7152D * green + 0.0722D * blue;
    }

    /**
     * Selects dark or light content for a filled surface.
     *
     * @param background surface color
     * @return a readable text color
     */
    public static int readableOn(int background) {
        return luminance(background) > 0.18D ? DARK_TEXT : LIGHT_TEXT;
    }

    /**
     * Builds the palette for the custom theme.
     *
     * @param accent    accent color
     * @param backplane backplane color, including its alpha
     * @return the derived palette
     */
    public static @NotNull Palette customPalette(int accent, int backplane) {
        int accentColor = opaque(accent);
        int plane = opaque(backplane);
        int normal = readableOn(plane);
        int label = mix(normal, plane, 0.30D);
        int separator = mix(normal, plane, 0.55D);
        int border = (0xCC << 24) | (accentColor & 0xFFFFFF);

        return new Palette(
                label,
                label,
                separator,
                normal,
                WARNING,
                CRITICAL,
                backplane,
                border,
                accentColor
        );
    }

    /**
     * Builds a nine-stop shimmer ramp in the theme's own color family.
     * <p>
     * Naruto keeps its white/gold/orange/red chakra ramp. The other themes get a mirrored
     * light-to-deep ramp built from their accent and separator, so the wave has the same kind of
     * tonal depth instead of only running between white and the accent.
     *
     * @param theme   active theme
     * @param palette active palette
     * @return nine ARGB stops
     */
    public static int @NotNull [] shimmerRamp(@NotNull Theme theme, @NotNull Palette palette) {
        if (!palette.hasAccent()) {
            return NEUTRAL_SHIMMER.clone();
        }

        int accent = opaque(palette.accent());

        if (theme == Theme.NARUTO) {
            return new int[]{
                    0xFFFFFFFF,
                    opaque(palette.warn()),
                    accent,
                    opaque(palette.label()),
                    opaque(palette.critical()),
                    opaque(palette.label()),
                    accent,
                    opaque(palette.warn()),
                    0xFFFFFFFF
            };
        }

        int light2 = mix(accent, 0xFFFFFFFF, 0.75D);
        int light1 = mix(accent, 0xFFFFFFFF, 0.45D);
        int accentLight = mix(accent, 0xFFFFFFFF, 0.20D);
        int deep = theme == Theme.CUSTOM
                ? mix(accent, 0xFF000000, 0.40D)
                : mix(opaque(palette.separator()), accent, 0.45D);

        return new int[]{
                light2,
                light1,
                accentLight,
                accent,
                deep,
                accent,
                accentLight,
                light1,
                light2
        };
    }

    /**
     * Samples a shimmer ramp at a wave position.
     *
     * @param ramp     nine ARGB stops
     * @param position wave position in {@code 0..1}
     * @return the interpolated color
     */
    public static int sampleShimmer(int @NotNull [] ramp, float position) {
        float value = Math.max(0.0F, Math.min(1.0F, position));
        for (int i = 1; i < SHIMMER_STOPS.length; i++) {
            if (value <= SHIMMER_STOPS[i]) {
                float span = SHIMMER_STOPS[i] - SHIMMER_STOPS[i - 1];
                float fraction = span <= 0.0F ? 0.0F : (value - SHIMMER_STOPS[i - 1]) / span;
                return mix(ramp[i - 1], ramp[i], fraction);
            }
        }
        return ramp[ramp.length - 1];
    }

    /**
     * Checks a config color token.
     *
     * @param value candidate token
     * @return {@code true} for {@code #?RRGGBB} or {@code #?AARRGGBB}
     */
    public static boolean isColorToken(@Nullable CharSequence value) {
        return value != null && COLOR_TOKEN.matcher(value).matches();
    }

    /**
     * Parses a config color token.
     *
     * @param value    token
     * @param fallback fallback when the token is missing or invalid
     * @return the ARGB color
     */
    public static int parseColorToken(@Nullable String value, int fallback) {
        if (value == null) {
            return fallback;
        }

        String token = value.startsWith("#") ? value.substring(1) : value;
        if (token.length() != 6 && token.length() != 8) {
            return fallback;
        }

        try {
            int color = (int) Long.parseLong(token, 16);
            return token.length() == 6 ? opaque(color) : color;
        } catch (NumberFormatException exception) {
            return fallback;
        }
    }

    /**
     * Formats a color for the Forge config.
     *
     * @param color ARGB color
     * @return {@code AARRGGBB}
     */
    public static @NotNull String toColorToken(int color) {
        return String.format("%08X", color);
    }

    /**
     * Formats a color for a UI label.
     *
     * @param color ARGB color
     * @return {@code #RRGGBB} when opaque, otherwise {@code #AARRGGBB}
     */
    public static @NotNull String format(int color) {
        int alpha = (color >>> 24) & 0xFF;
        return alpha == 0xFF
                ? String.format("#%06X", color & 0xFFFFFF)
                : String.format("#%08X", color);
    }

    private static double linear(int channel) {
        double value = channel / 255.0D;
        return value <= 0.04045D ? value / 12.92D : Math.pow((value + 0.055D) / 1.055D, 2.4D);
    }
}