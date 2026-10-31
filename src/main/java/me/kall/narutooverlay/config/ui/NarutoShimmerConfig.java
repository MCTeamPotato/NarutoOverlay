package me.kall.narutooverlay.config.ui;

import me.kall.narutooverlay.config.RenderArgs;
import me.kall.narutooverlay.theme.Palette;
import me.kall.narutoui.mui.shimmer.MuiShimmerConfig;
import me.kall.narutoui.mui.shimmer.MuiShimmerProfile;
import org.jetbrains.annotations.NotNull;

/**
 * Maps NarutoOverlay's client config and palette onto the generic shimmer settings.
 */
public final class NarutoShimmerConfig implements MuiShimmerConfig {
    /** Canonical config instance; {@link #profile()} exposes it through its profile. */
    private static final NarutoShimmerConfig INSTANCE = new NarutoShimmerConfig();

    /**
     * UI shimmer profile shared by the settings screen and the overlays it opens.
     * <p>
     * It is NarutoOverlay-owned rather than {@link MuiShimmerProfile#defaultProfile()}, so another
     * mod calling {@code MuiShimmer.configure(...)} does not change NarutoOverlay's shimmer. The
     * single instance also keeps the screen and its picker overlays in phase and preserves the
     * smoothed speed/scale state across page rebuilds. Do not call {@code configure(...)} on it:
     * that would change every usage at once.
     */
    private static final MuiShimmerProfile PROFILE = MuiShimmerProfile.create(INSTANCE);

    /**
     * Returns the shimmer profile used by the settings screen and its overlays.
     *
     * @return the shared UI profile
     */
    public static @NotNull MuiShimmerProfile profile() {
        return PROFILE;
    }

    @Override
    public boolean text() {
        return RenderArgs.TEXT_SHIMMER.get();
    }

    @Override
    public boolean configText() {
        return RenderArgs.CONFIG_TEXT_SHIMMER.get();
    }

    @Override
    public boolean controls() {
        return RenderArgs.CONTROL_SHIMMER.get();
    }

    @Override
    public boolean hasAccent() {
        return RenderArgs.palette().hasAccent();
    }

    @Override
    public int fps() {
        return RenderArgs.CONFIG_SHIMMER_FPS.get();
    }

    @Override
    public double speed() {
        return RenderArgs.TEXT_SHIMMER_SPEED.get();
    }

    @Override
    public double scale() {
        return RenderArgs.TEXT_SHIMMER_SCALE.get();
    }

    @Override
    public double strength() {
        return RenderArgs.TEXT_SHIMMER_STRENGTH.get();
    }

    @Override
    public int @NotNull [] ramp() {
        Palette palette = RenderArgs.palette();
        return new int[]{
                0xFFFFFFFF,
                palette.warn() | 0xFF000000,
                palette.accent() | 0xFF000000,
                palette.label() | 0xFF000000,
                palette.critical() | 0xFF000000,
                palette.label() | 0xFF000000,
                palette.accent() | 0xFF000000,
                palette.warn() | 0xFF000000,
                0xFFFFFFFF
        };
    }
}