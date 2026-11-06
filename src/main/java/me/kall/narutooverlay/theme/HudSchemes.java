package me.kall.narutooverlay.theme;

import net.minecraftforge.fml.ModList;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Overlay theme catalog.
 * <p>
 * The local built-ins always work, so the HUD does not require NarutoUI. When NarutoUI is present,
 * its shared {@code MuiColorSchemes} catalog is merged in reflectively: matching ids update the
 * local entry and new NarutoUI schemes are appended. That keeps the HUD optional-dependency-safe
 * while still making newly registered NarutoUI schemes appear in the theme picker automatically.
 */
public final class HudSchemes {
    private static final Logger LOGGER = LogManager.getLogger("NarutoOverlay/Themes");
    private static final String CUSTOM = "CUSTOM";

    private static final List<HudScheme> BUILT_INS = List.of(
            scheme("NARUTO", "narutoui.scheme.naruto", new Palette(
                    0xFF8F2A, 0xE0A64A, 0xA03A26, 0xFFFFFF, 0xFFD64A,
                    0xFF4A38, 0xB01E0A08, 0xCCE33A26, 0xFFFFC42E)),
            scheme("CLASSIC", "narutoui.scheme.classic", new Palette(
                    0xFFFFFF, 0xFFFFFF, 0xFFFFFF, 0xFFFFFF, 0xFFFF55,
                    0xFF5555, 0xA0404040, 0xFFFFFFFF, 0x00000000)),
            scheme("JADE", "narutoui.scheme.jade", new Palette(
                    0xFF9DB8AC, 0xFF9DB8AC, 0xFF2B463C, 0xFFE7F6EF, 0xFFFFC857,
                    0xFFFF6B6B, 0xB00B1512, 0xCC3DDC97, 0xFF3DDC97)),
            scheme("AZURE", "narutoui.scheme.azure", new Palette(
                    0xFFA3B6CF, 0xFFA3B6CF, 0xFF2B3D55, 0xFFE8F1FF, 0xFFFFC857,
                    0xFFFF6B6B, 0xB00A111C, 0xCC5AA9FF, 0xFF5AA9FF)),
            scheme("SAKURA", "narutoui.scheme.sakura", new Palette(
                    0xFFC9A9B6, 0xFFC9A9B6, 0xFF4A2E3A, 0xFFFDEBF1, 0xFFFFD24A,
                    0xFFFF6B6B, 0xB0150E13, 0xCCFF8FB3, 0xFFFF8FB3)),
            scheme("GRAPHITE", "narutoui.scheme.graphite", new Palette(
                    0xFFA9AEB8, 0xFFA9AEB8, 0xFF3A3E47, 0xFFF2F3F6, 0xFFFFC857,
                    0xFFFF6B6B, 0xB00E0F12, 0xCCD6D9E0, 0xFFD6D9E0))
    );

    private static volatile List<HudScheme> schemes;
    private static volatile boolean merged;

    private HudSchemes() {}

    private static @NotNull HudScheme scheme(@NotNull String id, @NotNull String nameKey, @NotNull Palette palette) {
        return new HudScheme(id, nameKey, palette, ThemeColors.shimmerRamp(id, palette));
    }

    /**
     * @return all selectable schemes, including any shared NarutoUI schemes
     */
    public static @NotNull List<HudScheme> all() {
        ensureMerged();
        return schemes;
    }

    /**
     * @param id scheme id, case-insensitive
     * @return the scheme, or {@code null}
     */
    public static @Nullable HudScheme byId(@Nullable String id) {
        if (id == null || id.isBlank()) {
            return null;
        }
        ensureMerged();
        for (HudScheme scheme : schemes) {
            if (scheme.id().equalsIgnoreCase(id)) {
                return scheme;
            }
        }
        return null;
    }

    /**
     * @param id scheme id
     * @return the palette, falling back to Naruto for unknown ids
     */
    public static @NotNull Palette paletteFor(@Nullable String id) {
        HudScheme scheme = byId(id);
        return scheme != null ? scheme.palette() : BUILT_INS.get(0).palette();
    }

    /**
     * @param id scheme id
     * @return the display name key, falling back to the id
     */
    public static @NotNull String nameKey(@Nullable String id) {
        HudScheme scheme = byId(id);
        return scheme != null ? scheme.nameKey() : String.valueOf(id);
    }

    /**
     * @param id scheme id
     * @return the scheme's shimmer ramp, or a generated ramp for {@code CUSTOM}
     */
    public static int @NotNull [] shimmerRamp(@Nullable String id, @NotNull Palette palette) {
        if (id != null && CUSTOM.equalsIgnoreCase(id)) {
            return ThemeColors.shimmerRamp(CUSTOM, palette);
        }
        HudScheme scheme = byId(id);
        if (scheme != null) {
            return scheme.shimmerRamp();
        }
        return ThemeColors.shimmerRamp(id, palette);
    }

    /**
     * @param id scheme id
     * @return whether this is the custom scheme
     */
    public static boolean custom(@Nullable String id) {
        return id != null && CUSTOM.equalsIgnoreCase(id);
    }

    private static void ensureMerged() {
        if (merged) {
            return;
        }
        synchronized (HudSchemes.class) {
            if (merged) {
                return;
            }

            Map<String, HudScheme> map = new LinkedHashMap<>();
            for (HudScheme scheme : BUILT_INS) {
                map.put(scheme.id(), scheme);
            }

            try {
                if (ModList.get().isLoaded("narutoui")) {
                    Class<?> adapter = Class.forName("me.kall.narutooverlay.theme.NarutoUiSchemeAdapter");
                    Method method = adapter.getMethod("schemes");
                    Object result = method.invoke(null);
                    if (result instanceof Iterable<?> iterable) {
                        for (Object value : iterable) {
                            if (value instanceof HudScheme scheme) {
                                map.put(scheme.id().toUpperCase(Locale.ROOT), scheme);
                            }
                        }
                    }
                }
            } catch (Throwable throwable) {
                LOGGER.warn("Could not merge shared NarutoUI color schemes; using local schemes", throwable);
            }

            schemes = List.copyOf(new ArrayList<>(map.values()));
            merged = true;
        }
    }
}