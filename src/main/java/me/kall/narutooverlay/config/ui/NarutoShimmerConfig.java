package me.kall.narutooverlay.config.ui;

import me.kall.narutoui.mui.shimmer.MuiShimmerConfig;
import me.kall.narutoui.mui.shimmer.MuiShimmerProfile;
import me.kall.narutooverlay.config.RenderArgs;
import me.kall.narutooverlay.theme.Palette;
import org.jetbrains.annotations.NotNull;

/**
 * Maps NarutoOverlay's client config and palette onto the generic shimmer settings.
 */
public final class NarutoShimmerConfig implements MuiShimmerConfig {
    /**
     * NarutoOverlay-owned HUD profile. It is deliberately not the API default profile, so another
     * mod can configure its own shimmer settings without changing the HUD.
     */
    private static final MuiShimmerProfile HUD_PROFILE = MuiShimmerProfile.create(new NarutoShimmerConfig());

    /**
     * Returns NarutoOverlay's HUD shimmer profile.
     *
     * @return the HUD profile
     */
    public static @NotNull MuiShimmerProfile profile() {
        return HUD_PROFILE;
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