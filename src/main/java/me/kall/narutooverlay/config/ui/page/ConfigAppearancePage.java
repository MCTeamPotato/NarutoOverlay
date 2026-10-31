package me.kall.narutooverlay.config.ui.page;

import icyllis.modernui.core.Context;
import icyllis.modernui.graphics.drawable.ShapeDrawable;
import icyllis.modernui.view.View;
import icyllis.modernui.widget.FrameLayout;
import icyllis.modernui.widget.LinearLayout;
import me.kall.narutooverlay.config.ConfigSaver;
import me.kall.narutooverlay.config.RenderArgs;
import me.kall.narutooverlay.config.ui.NarutoShimmerConfig;
import me.kall.narutooverlay.config.ui.widget.Accent;
import me.kall.narutooverlay.config.ui.widget.NarutoShimmerWidgetFactory;
import me.kall.narutooverlay.theme.Palette;
import me.kall.narutooverlay.theme.Theme;
import me.kall.narutooverlay.theme.ValueLevel;
import me.kall.narutoui.mui.overlay.MuiColorPickerOverlay;
import me.kall.narutoui.mui.screen.MuiScreenHost;
import me.kall.narutoui.mui.screen.MuiSettingsPage;
import me.kall.narutoui.mui.shimmer.MuiShimmer;
import me.kall.narutoui.mui.shimmer.MuiShimmerProfile;
import me.kall.narutoui.mui.theme.MuiTheme;
import me.kall.narutoui.mui.theme.MuiWidgetFactory;
import me.kall.narutoui.mui.widget.MuiColorField;
import me.kall.narutoui.mui.widget.MuiControls;
import me.kall.narutoui.mui.widget.MuiThemePicker;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.Locale;

/**
 * Appearance page expressed through the high-level settings DSL plus the generic theme picker.
 */
public final class ConfigAppearancePage {
    private static final String GROUP = "narutooverlay.config.";

    private ConfigAppearancePage() {}

    public static void populate(@NotNull MuiSettingsPage page, @NotNull MuiScreenHost host) {
        page.section(GROUP + "appearance.theme", section -> section.themePicker(Arrays.stream(Theme.values()).map(theme -> {
                    Palette palette = theme.palette();
                    return MuiThemePicker.Option.of(GROUP + "theme." + theme.name().toLowerCase(Locale.ROOT), RenderArgs.THEME.get() == theme, () -> RenderArgs.THEME.set(theme), palette.hasAccent() ? palette.accent() : palette.normal(), palette.backplane()).preview((context, ignoredTheme) -> previewTile(context, theme));
                }).toList(), GROUP + "appearance.inUse", host::onThemeChanged))
                .section(GROUP + "appearance.display", section -> section
                        .toggle(GROUP + "appearance.dropShadow", GROUP + "appearance.dropShadow.desc", RenderArgs.DROP_SHADOW::get, value -> RenderArgs.set(RenderArgs.DROP_SHADOW, value))
                        .toggle(GROUP + "appearance.equalWidth", GROUP + "appearance.equalWidth.desc", RenderArgs.EQUAL_WIDTH::get, value -> RenderArgs.set(RenderArgs.EQUAL_WIDTH, value))
                        .toggle(GROUP + "appearance.drawBackplane", GROUP + "appearance.drawBackplane.desc", RenderArgs.DRAW_BACKPLANE::get, value -> RenderArgs.set(RenderArgs.DRAW_BACKPLANE, value))
                        .toggle(GROUP + "appearance.textShimmer", GROUP + "appearance.textShimmer.desc", RenderArgs.TEXT_SHIMMER::get, value -> {
                            RenderArgs.set(RenderArgs.TEXT_SHIMMER, value);
                            MuiShimmer.refresh();
                        }))
                .section(GROUP + "appearance.shimmer", section -> section
                        .toggle(GROUP + "appearance.shimmerHud", GROUP + "appearance.shimmerHud.desc", RenderArgs.HUD_TEXT_SHIMMER::get, value -> RenderArgs.set(RenderArgs.HUD_TEXT_SHIMMER, value))
                        .toggle(GROUP + "appearance.shimmerConfig", GROUP + "appearance.shimmerConfig.desc", RenderArgs.CONFIG_TEXT_SHIMMER::get, value -> {
                            RenderArgs.set(RenderArgs.CONFIG_TEXT_SHIMMER, value);
                            MuiShimmer.refresh();
                        })
                        .toggle(GROUP + "appearance.shimmerControls", GROUP + "appearance.shimmerControls.desc", RenderArgs.CONTROL_SHIMMER::get, value -> {
                            RenderArgs.set(RenderArgs.CONTROL_SHIMMER, value);
                            MuiShimmer.refresh();
                        })
                        .slider(GROUP + "appearance.shimmerSpeed", 2, 30, () -> (int) Math.round(RenderArgs.TEXT_SHIMMER_SPEED.get() * 10.0D), value -> RenderArgs.set(RenderArgs.TEXT_SHIMMER_SPEED, value / 10.0D), value -> String.format(Locale.ROOT, "%.1fx", value / 10.0D))
                        .slider(GROUP + "appearance.shimmerDensity", 2, 30, () -> (int) Math.round(RenderArgs.TEXT_SHIMMER_SCALE.get() * 10.0D), value -> RenderArgs.set(RenderArgs.TEXT_SHIMMER_SCALE, value / 10.0D), value -> String.format(Locale.ROOT, "%.1fx", value / 10.0D))
                        .slider(GROUP + "appearance.shimmerStrength", 0, 100, () -> (int) Math.round(RenderArgs.TEXT_SHIMMER_STRENGTH.get() * 100.0D), value -> RenderArgs.set(RenderArgs.TEXT_SHIMMER_STRENGTH, value / 100.0D), value -> value + "%")
                        .slider(GROUP + "appearance.shimmerFps", 5, 240, RenderArgs.TEXT_SHIMMER_FPS::get, value -> RenderArgs.set(RenderArgs.TEXT_SHIMMER_FPS, value))
                        .slider(GROUP + "appearance.configShimmerFps", 5, 120, RenderArgs.CONFIG_SHIMMER_FPS::get, value -> RenderArgs.set(RenderArgs.CONFIG_SHIMMER_FPS, value))
                        .toggle(GROUP + "appearance.shimmerCache", GROUP + "appearance.shimmerCache.desc", RenderArgs.TEXT_SHIMMER_CACHE::get, value -> RenderArgs.set(RenderArgs.TEXT_SHIMMER_CACHE, value)))
                .section(GROUP + "appearance.backplane", section -> section
                        .slider(GROUP + "appearance.borderWidth", 0, 400, () -> (int) Math.round(RenderArgs.BACKPLANE_BORDER_WIDTH.get() * 100.0D), value -> RenderArgs.set(RenderArgs.BACKPLANE_BORDER_WIDTH, value / 100.0D), value -> String.format(Locale.ROOT, "%.2f", value / 100.0D))
                        .toggle(GROUP + "appearance.borderOverride", GROUP + "appearance.borderOverride.desc", RenderArgs.BACKPLANE_BORDER_OVERRIDE::get, value -> RenderArgs.set(RenderArgs.BACKPLANE_BORDER_OVERRIDE, value))
                        .customLayout(row -> {
                            Context context = row.getContext();
                            row.add(MuiColorField.create(context, MuiTheme.modern(context), GROUP + "appearance.borderColor", RenderArgs.borderColor())
                                    .buttonLabel(GROUP + "appearance.pickColor")
                                    .onPick(() -> {
                                        Context pickerContext = host.requireContext();
                                        MuiShimmerProfile.withCurrent(NarutoShimmerConfig.profile(), () ->
                                                MuiControls.withFactory(NarutoShimmerWidgetFactory.INSTANCE, () -> host.showOverlay(new MuiColorPickerOverlay(pickerContext, MuiTheme.modern(pickerContext).withPrimary(Accent.primary()), RenderArgs.borderColor(), new MuiColorPickerOverlay.Labels(GROUP + "color.title", GROUP + "color.brightness", GROUP + "color.alpha", GROUP + "color.apply", GROUP + "color.cancel"), argb -> {
                                                    RenderArgs.BACKPLANE_BORDER_OVERRIDE.set(true);
                                                    RenderArgs.BACKPLANE_BORDER_RED.set((argb >> 16) & 0xFF);
                                                    RenderArgs.BACKPLANE_BORDER_GREEN.set((argb >> 8) & 0xFF);
                                                    RenderArgs.BACKPLANE_BORDER_BLUE.set(argb & 0xFF);
                                                    RenderArgs.BACKPLANE_BORDER_ALPHA.set((argb >>> 24) & 0xFF);
                                                    ConfigSaver.changed();
                                                    host.reloadCurrentPage();
                                                }))));
                                    }), MuiControls.fill());
                        }))
                .section(GROUP + "appearance.performance", section -> section
                        .slider(GROUP + "appearance.fpsInterval", 1, 200, RenderArgs.FPS_COOLDOWN::get, value -> RenderArgs.set(RenderArgs.FPS_COOLDOWN, value))
                        .slider(GROUP + "appearance.gcInterval", 1, 200, RenderArgs.GC_COOLDOWN::get, value -> RenderArgs.set(RenderArgs.GC_COOLDOWN, value))
                        .slider(GROUP + "appearance.ramInterval", 1, 200, RenderArgs.RAM_COOLDOWN::get, value -> RenderArgs.set(RenderArgs.RAM_COOLDOWN, value))
                        .slider(GROUP + "appearance.fpsWindow", 10, 1000, RenderArgs.FPS_WINDOW::get, value -> RenderArgs.set(RenderArgs.FPS_WINDOW, value))
                );
    }

    private static @NotNull View previewTile(@NotNull Context context, @NotNull Theme theme) {
        return MuiControls.withFactory(MuiWidgetFactory.modern(), () -> {
            Palette palette = theme.palette();

            FrameLayout preview = new FrameLayout(context);
            ShapeDrawable background = new ShapeDrawable();
            background.setCornerRadius(MuiControls.dp(context, 8));
            background.setColor(palette.backplane());
            background.setStroke(Math.max(1, MuiControls.dp(context, 1)), opaque(palette.border()));
            preview.setBackground(background);
            preview.setMinimumHeight(MuiControls.dp(context, 58));

            LinearLayout lines = MuiControls.column(context);
            lines.setPadding(MuiControls.dp(context, 7), MuiControls.dp(context, 7), MuiControls.dp(context, 7), MuiControls.dp(context, 7));
            lines.addView(previewLine(context, "FPS ", "144", palette), MuiControls.fill());
            lines.addView(previewLine(context, "RAM ", "8192/16384", palette), MuiControls.fill());
            lines.addView(previewLine(context, "GC ", "12 ms", palette), MuiControls.fill());

            preview.addView(lines, new FrameLayout.LayoutParams(MuiControls.MATCH, MuiControls.MATCH));
            return preview;
        });
    }

    private static @NotNull LinearLayout previewLine(@NotNull Context context, @NotNull String label, @NotNull String value, @NotNull Palette palette) {
        LinearLayout line = MuiControls.row(context);
        line.setMinimumHeight(0);
        line.setPadding(0, 0, 0, 0);
        MuiTheme theme = MuiTheme.modern(context);
        line.addView(MuiControls.text(context, theme, label, 10, opaque(palette.label())), MuiControls.wrap());
        line.addView(MuiControls.text(context, theme, value, 10, opaque(palette.value(ValueLevel.NORMAL))), MuiControls.wrap());
        return line;
    }

    private static int opaque(int color) {
        return color | 0xFF000000;
    }
}