package me.kall.narutooverlay.theme;

import me.kall.narutoui.mui.color.MuiColorScheme;
import me.kall.narutoui.mui.color.MuiColorSchemes;
import me.kall.narutoui.mui.color.MuiHudPalette;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

/**
 * Optional NarutoUI bridge.
 * <p>
 * This class is loaded only through reflection when NarutoUI is present, so the rest of the HUD
 * keeps working when NarutoUI is absent. It converts shared UI+HUD schemes into local
 * {@link HudScheme} entries.
 */
public final class NarutoUiSchemeAdapter {
    private NarutoUiSchemeAdapter() {}

    /**
     * @return the shared schemes converted to overlay schemes
     */
    public static @NotNull List<HudScheme> schemes() {
        List<HudScheme> result = new ArrayList<>();
        for (MuiColorScheme scheme : MuiColorSchemes.all()) {
            MuiHudPalette hud = scheme.hud();
            Palette palette = new Palette(
                    hud.label(),
                    hud.unit(),
                    hud.separator(),
                    hud.normal(),
                    hud.warn(),
                    hud.critical(),
                    hud.backplane(),
                    hud.border(),
                    hud.accent()
            );
            result.add(new HudScheme(scheme.id(), scheme.nameKey(), palette, scheme.shimmerRamp()));
        }
        return result;
    }
}