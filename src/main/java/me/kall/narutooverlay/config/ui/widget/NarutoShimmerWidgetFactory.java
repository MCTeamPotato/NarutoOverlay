package me.kall.narutooverlay.config.ui.widget;

import icyllis.modernui.core.Context;
import icyllis.modernui.view.View;
import icyllis.modernui.widget.Button;
import icyllis.modernui.widget.SeekBar;
import icyllis.modernui.widget.Switch;
import icyllis.modernui.widget.TextView;
import me.kall.narutoui.mui.theme.MuiTheme;
import me.kall.narutoui.mui.theme.MuiWidgetFactory;
import org.jetbrains.annotations.NotNull;

/**
 * Maps NarutoOverlay's palette onto the API's shimmer widget factory.
 *
 * <p>Widget construction, shimmer attachment and styling now live in
 * {@link MuiWidgetFactory#shimmer()}; this adapter only supplies Naruto's accent colors.</p>
 */
public final class NarutoShimmerWidgetFactory implements MuiWidgetFactory {
    private static final MuiWidgetFactory SHIMMER = MuiWidgetFactory.shimmer();

    private static @NotNull MuiTheme naruto(@NotNull MuiTheme theme) {
        return theme.withPrimary(Accent.primary()).withOnPrimary(0xFF000000);
    }

    @Override
    public @NotNull TextView text(@NotNull Context context, @NotNull CharSequence text, float size, int color) {
        return SHIMMER.text(context, text, size, color);
    }

    @Override
    public @NotNull Switch switchControl(@NotNull Context context, @NotNull MuiTheme theme) {
        return SHIMMER.switchControl(context, naruto(theme));
    }

    @Override
    public @NotNull SeekBar slider(@NotNull Context context, @NotNull MuiTheme theme, int max, int progress) {
        return SHIMMER.slider(context, naruto(theme), max, progress);
    }

    @Override
    public @NotNull Button button(@NotNull Context context, @NotNull MuiTheme theme, @NotNull CharSequence label, boolean outlined, @NotNull View.OnClickListener onClick) {
        return SHIMMER.button(context, naruto(theme), label, outlined, onClick);
    }
}