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
import me.kall.narutoui.mui.util.MuiLocalization;
import me.kall.narutoui.mui.widget.MuiColorField;
import me.kall.narutoui.mui.widget.MuiControls;
import me.kall.narutoui.mui.widget.MuiThemePicker;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Unmodifiable;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Appearance page expressed through the high-level settings DSL plus the generic theme picker.
 */
public final class ConfigAppearancePage {
    private static final String GROUP = "narutooverlay.config.";

    private ConfigAppearancePage() {}

    public static void populate(@NotNull MuiSettingsPage page, @NotNull MuiScreenHost host) {
        themes(page, host);

        page.section(MuiLocalization.get(GROUP + "appearance.display"), section -> section
                .toggle(MuiLocalization.get(GROUP + "appearance.dropShadow"),
                        MuiLocalization.get(GROUP + "appearance.dropShadow.desc"),
                        RenderArgs.DROP_SHADOW::get,
                        value -> {
                            RenderArgs.DROP_SHADOW.set(value);
                            ConfigSaver.changed();
                        })
                .toggle(MuiLocalization.get(GROUP + "appearance.equalWidth"),
                        MuiLocalization.get(GROUP + "appearance.equalWidth.desc"),
                        RenderArgs.EQUAL_WIDTH::get,
                        value -> {
                            RenderArgs.EQUAL_WIDTH.set(value);
                            ConfigSaver.changed();
                        })
                .toggle(MuiLocalization.get(GROUP + "appearance.drawBackplane"),
                        MuiLocalization.get(GROUP + "appearance.drawBackplane.desc"),
                        RenderArgs.DRAW_BACKPLANE::get,
                        value -> {
                            RenderArgs.DRAW_BACKPLANE.set(value);
                            ConfigSaver.changed();
                        })
                .toggle(MuiLocalization.get(GROUP + "appearance.textShimmer"),
                        MuiLocalization.get(GROUP + "appearance.textShimmer.desc"),
                        RenderArgs.TEXT_SHIMMER::get,
                        value -> {
                            RenderArgs.TEXT_SHIMMER.set(value);
                            ConfigSaver.changed();
                            MuiShimmer.refresh();
                        }));

        shimmer(page);
        backplane(page, host);
        performance(page);
    }

    private static void themes(@NotNull MuiSettingsPage page, @NotNull MuiScreenHost host) {
        List<MuiThemePicker.Option> options = new ArrayList<>();
        for (Theme theme : Theme.values()) options.add(new ThemeOption(theme));

        MuiThemePicker.add(page, MuiLocalization.get(GROUP + "appearance.theme"), options,
                MuiLocalization.get(GROUP + "appearance.inUse"), () -> {
                    ConfigSaver.changed();
                    host.onThemeChanged();
                });
    }

    private static void shimmer(@NotNull MuiSettingsPage page) {
        page.section(MuiLocalization.get(GROUP + "appearance.shimmer"), section -> section
                .toggle(MuiLocalization.get(GROUP + "appearance.shimmerHud"),
                        MuiLocalization.get(GROUP + "appearance.shimmerHud.desc"),
                        RenderArgs.HUD_TEXT_SHIMMER::get,
                        value -> {
                            RenderArgs.HUD_TEXT_SHIMMER.set(value);
                            ConfigSaver.changed();
                        })
                .toggle(MuiLocalization.get(GROUP + "appearance.shimmerConfig"),
                        MuiLocalization.get(GROUP + "appearance.shimmerConfig.desc"),
                        RenderArgs.CONFIG_TEXT_SHIMMER::get,
                        value -> {
                            RenderArgs.CONFIG_TEXT_SHIMMER.set(value);
                            ConfigSaver.changed();
                            MuiShimmer.refresh();
                        })
                .toggle(MuiLocalization.get(GROUP + "appearance.shimmerControls"),
                        MuiLocalization.get(GROUP + "appearance.shimmerControls.desc"),
                        RenderArgs.CONTROL_SHIMMER::get,
                        value -> {
                            RenderArgs.CONTROL_SHIMMER.set(value);
                            ConfigSaver.changed();
                            MuiShimmer.refresh();
                        })
                .slider(MuiLocalization.get(GROUP + "appearance.shimmerSpeed"), 2, 30,
                        () -> (int) Math.round(RenderArgs.TEXT_SHIMMER_SPEED.get() * 10.0D),
                        value -> {
                            RenderArgs.TEXT_SHIMMER_SPEED.set(value / 10.0D);
                            ConfigSaver.changed();
                        },
                        value -> String.format(Locale.ROOT, "%.1fx", value / 10.0D))
                .slider(MuiLocalization.get(GROUP + "appearance.shimmerDensity"), 2, 30,
                        () -> (int) Math.round(RenderArgs.TEXT_SHIMMER_SCALE.get() * 10.0D),
                        value -> {
                            RenderArgs.TEXT_SHIMMER_SCALE.set(value / 10.0D);
                            ConfigSaver.changed();
                        },
                        value -> String.format(Locale.ROOT, "%.1fx", value / 10.0D))
                .slider(MuiLocalization.get(GROUP + "appearance.shimmerStrength"), 0, 100,
                        () -> (int) Math.round(RenderArgs.TEXT_SHIMMER_STRENGTH.get() * 100.0D),
                        value -> {
                            RenderArgs.TEXT_SHIMMER_STRENGTH.set(value / 100.0D);
                            ConfigSaver.changed();
                        },
                        value -> value + "%")
                .slider(MuiLocalization.get(GROUP + "appearance.shimmerFps"), 5, 240,
                        RenderArgs.TEXT_SHIMMER_FPS::get,
                        value -> {
                            RenderArgs.TEXT_SHIMMER_FPS.set(value);
                            ConfigSaver.changed();
                        })
                .slider(MuiLocalization.get(GROUP + "appearance.configShimmerFps"), 5, 120,
                        RenderArgs.CONFIG_SHIMMER_FPS::get,
                        value -> {
                            RenderArgs.CONFIG_SHIMMER_FPS.set(value);
                            ConfigSaver.changed();
                        })
                .toggle(MuiLocalization.get(GROUP + "appearance.shimmerCache"),
                        MuiLocalization.get(GROUP + "appearance.shimmerCache.desc"),
                        RenderArgs.TEXT_SHIMMER_CACHE::get,
                        value -> {
                            RenderArgs.TEXT_SHIMMER_CACHE.set(value);
                            ConfigSaver.changed();
                        }));
    }

    private static void backplane(@NotNull MuiSettingsPage page, @NotNull MuiScreenHost host) {
        page.section(MuiLocalization.get(GROUP + "appearance.backplane"), section -> section
                .slider(MuiLocalization.get(GROUP + "appearance.borderWidth"), 0, 400,
                        () -> (int) Math.round(RenderArgs.BACKPLANE_BORDER_WIDTH.get() * 100.0D),
                        value -> {
                            RenderArgs.BACKPLANE_BORDER_WIDTH.set(value / 100.0D);
                            ConfigSaver.changed();
                        },
                        value -> String.format(Locale.ROOT, "%.2f", value / 100.0D))
                .toggle(MuiLocalization.get(GROUP + "appearance.borderOverride"),
                        MuiLocalization.get(GROUP + "appearance.borderOverride.desc"),
                        RenderArgs.BACKPLANE_BORDER_OVERRIDE::get,
                        value -> {
                            RenderArgs.BACKPLANE_BORDER_OVERRIDE.set(value);
                            ConfigSaver.changed();
                        })
                .custom(row -> addColorPicker(row, host)));
    }

    private static void addColorPicker(@NotNull LinearLayout row, @NotNull MuiScreenHost host) {
        Context context = row.getContext();
        MuiTheme theme = MuiTheme.modern(context);
        MuiColorField field = MuiColorField.create(context, theme,
                        MuiLocalization.get(GROUP + "appearance.borderColor"), RenderArgs.borderColor())
                .buttonLabel(MuiLocalization.get(GROUP + "appearance.pickColor"))
                .onPick(() -> {
                    Context pickerContext = host.requireContext();
                    MuiShimmerProfile.withCurrent(NarutoShimmerConfig.profile(), () ->
                            MuiControls.withFactory(new NarutoShimmerWidgetFactory(), () ->
                                    host.showOverlay(new MuiColorPickerOverlay(pickerContext,
                                            MuiTheme.modern(pickerContext).withPrimary(Accent.primary()),
                                            RenderArgs.borderColor(),
                                            new MuiColorPickerOverlay.Labels(
                                                    MuiLocalization.get(GROUP + "color.title"),
                                                    MuiLocalization.get(GROUP + "color.brightness"),
                                                    MuiLocalization.get(GROUP + "color.alpha"),
                                                    MuiLocalization.get(GROUP + "color.apply"),
                                                    MuiLocalization.get(GROUP + "color.cancel")),
                                            argb -> {
                                                RenderArgs.BACKPLANE_BORDER_OVERRIDE.set(true);
                                                RenderArgs.BACKPLANE_BORDER_RED.set((argb >> 16) & 0xFF);
                                                RenderArgs.BACKPLANE_BORDER_GREEN.set((argb >> 8) & 0xFF);
                                                RenderArgs.BACKPLANE_BORDER_BLUE.set(argb & 0xFF);
                                                RenderArgs.BACKPLANE_BORDER_ALPHA.set((argb >>> 24) & 0xFF);
                                                ConfigSaver.changed();
                                                host.reloadCurrentPage();
                                            }))));
                });
        row.addView(field, MuiControls.fill());
    }

    private static void performance(@NotNull MuiSettingsPage page) {
        page.section(MuiLocalization.get(GROUP + "appearance.performance"), section -> section
                .slider(MuiLocalization.get(GROUP + "appearance.fpsInterval"), 1, 200,
                        RenderArgs.FPS_COOLDOWN::get,
                        value -> {
                            RenderArgs.FPS_COOLDOWN.set(value);
                            ConfigSaver.changed();
                        })
                .slider(MuiLocalization.get(GROUP + "appearance.gcInterval"), 1, 200,
                        RenderArgs.GC_COOLDOWN::get,
                        value -> {
                            RenderArgs.GC_COOLDOWN.set(value);
                            ConfigSaver.changed();
                        })
                .slider(MuiLocalization.get(GROUP + "appearance.ramInterval"), 1, 200,
                        RenderArgs.RAM_COOLDOWN::get,
                        value -> {
                            RenderArgs.RAM_COOLDOWN.set(value);
                            ConfigSaver.changed();
                        })
                .slider(MuiLocalization.get(GROUP + "appearance.fpsWindow"), 10, 1000,
                        RenderArgs.FPS_WINDOW::get,
                        value -> {
                            RenderArgs.FPS_WINDOW.set(value);
                            ConfigSaver.changed();
                        }));
    }

    private record ThemeOption(Theme theme) implements MuiThemePicker.Option {
        @Override
        public @NotNull @Unmodifiable CharSequence title() {
            return MuiLocalization.get(GROUP + "theme." + this.theme.name().toLowerCase(Locale.ROOT));
        }

        @Override
        public boolean selected() {
            return RenderArgs.THEME.get() == this.theme;
        }

        @Override
        public void select() {
            RenderArgs.THEME.set(this.theme);
        }

        @Override
        public int accent() {
            Palette palette = this.theme.palette();
            return palette.hasAccent() ? palette.accent() : palette.normal();
        }

        @Override
        public int surface() {
            return this.theme.palette().backplane();
        }

        @Override
        public @NotNull View preview(@NotNull Context context, @NotNull MuiTheme theme) {
            return MuiControls.withFactory(MuiWidgetFactory.modern(), () -> {
                Palette palette = this.theme.palette();

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
    }

    private static int opaque(int color) {
        return color | 0xFF000000;
    }
}