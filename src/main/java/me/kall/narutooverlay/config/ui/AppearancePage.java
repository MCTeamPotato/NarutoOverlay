package me.kall.narutooverlay.config.ui;

import icyllis.modernui.core.Context;
import icyllis.modernui.graphics.drawable.ShapeDrawable;
import icyllis.modernui.mc.ui.ThemeControl;
import icyllis.modernui.util.ColorStateList;
import icyllis.modernui.view.Gravity;
import icyllis.modernui.view.View;
import icyllis.modernui.widget.EditText;
import icyllis.modernui.widget.FrameLayout;
import icyllis.modernui.widget.Button;
import icyllis.modernui.widget.LinearLayout;
import icyllis.modernui.widget.SeekBar;
import icyllis.modernui.widget.TextView;
import me.kall.narutooverlay.config.ConfigSaver;
import me.kall.narutooverlay.config.RenderArgs;
import me.kall.narutooverlay.theme.Palette;
import me.kall.narutooverlay.theme.Theme;
import me.kall.narutooverlay.theme.ValueLevel;
import org.jetbrains.annotations.NotNull;

import java.util.function.IntConsumer;
import java.util.function.IntSupplier;

public final class AppearancePage extends Page {
    private static final String GROUP = "narutooverlay.config.";

    public static final int PAGE_INDEX = 1;

    public AppearancePage(@NotNull Context context) {
        super(context);
        build();
    }

    @Override
    protected void populate() {
        header("narutooverlay.config.appearance.title", "narutooverlay.config.appearance.subtitle");
        addThemes();
        addDisplay();
        addBackplane();
        addPerformance();
    }

    private void addThemes() {
        LinearLayout card = section("narutooverlay.config.appearance.theme");

        LinearLayout row = UiKit.row(this.context);
        row.setGravity(Gravity.CENTER_VERTICAL);
        row.setPadding(0, 0, 0, 0);
        row.setMinimumHeight(0);

        Theme current = RenderArgs.THEME.get();
        Theme[] themes = Theme.values();
        for (int i = 0; i < themes.length; i++) {
            Theme theme = themes[i];
            View tile = themeTile(theme, theme == current);

            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(0,
                    UiKit.dp(this.context, 116), 1.0F);
            int gap = UiKit.dp(this.context, 10);
            params.setMargins(i == 0 ? 0 : gap, 0, 0, 0);
            row.addView(tile, params);
        }
        card.addView(row, UiKit.fill());
    }

    private @NotNull View themeTile(@NotNull Theme theme, boolean selected) {
        Palette palette = theme.palette();
        int padding = UiKit.dp(this.context, 10);

        FrameLayout tile = new FrameLayout(this.context);
        ShapeDrawable background = new ShapeDrawable();
        background.setCornerRadius(UiKit.dp(this.context, 12));
        background.setColor(ColorStateList.modulateColor(UiKit.surfaceContainerLow(this.context), 0.72F));
        background.setStroke(Math.max(1, UiKit.dp(this.context, selected ? 2 : 1)),
                selected ? UiKit.primary(this.context) : UiKit.outlineVariant(this.context));
        tile.setBackground(background);

        LinearLayout column = UiKit.column(this.context);
        column.setPadding(padding, padding, padding, padding);

        FrameLayout preview = new FrameLayout(this.context);
        ShapeDrawable previewBackground = new ShapeDrawable();
        previewBackground.setCornerRadius(UiKit.dp(this.context, 8));
        previewBackground.setColor(palette.backplane());
        previewBackground.setStroke(Math.max(1, UiKit.dp(this.context, 1)), opaque(palette.border()));
        preview.setBackground(previewBackground);

        LinearLayout lines = UiKit.column(this.context);
        int inset = UiKit.dp(this.context, 7);
        lines.setPadding(inset, inset, inset, inset);
        lines.addView(previewLine("FPS ", "144", palette), UiKit.fill());
        lines.addView(previewLine("RAM ", "8192/16384", palette), UiKit.fill());
        lines.addView(previewLine("GC ", "12 ms", palette), UiKit.fill());
        preview.addView(lines, new FrameLayout.LayoutParams(UiKit.MATCH, UiKit.MATCH));

        LinearLayout.LayoutParams previewParams = new LinearLayout.LayoutParams(UiKit.MATCH, 0, 1.0F);
        column.addView(preview, previewParams);

        LinearLayout footer = UiKit.row(this.context);
        footer.setMinimumHeight(0);
        footer.setPadding(0, 0, 0, 0);
        LinearLayout.LayoutParams footerParams = UiKit.fill();
        footerParams.topMargin = UiKit.dp(this.context, 6);
        footer.addView(UiKit.heading(this.context,
                UiKit.str(GROUP + "theme." + theme.name().toLowerCase(java.util.Locale.ROOT)), 12,
                UiKit.onSurface(this.context)), UiKit.weight());
        if (selected) {
            footer.addView(UiKit.text(this.context, UiKit.str(GROUP + "appearance.inUse"), 10,
                    UiKit.primary(this.context)), UiKit.wrap());
        }
        column.addView(footer, footerParams);

        tile.addView(column, new FrameLayout.LayoutParams(UiKit.MATCH, UiKit.MATCH));

        tile.setOnClickListener(view -> {
            RenderArgs.THEME.set(theme);
            ConfigSaver.changed();

            ConfigFragment host = ConfigFragment.active();
            if (host != null) {
                host.onThemeChanged();
            } else {
                rebuild();
            }
        });
        return tile;
    }

    private static int opaque(int color) {
        return color | 0xFF000000;
    }

    private @NotNull LinearLayout previewLine(@NotNull String label, @NotNull String value,
                                              @NotNull Palette palette) {
        LinearLayout line = UiKit.row(this.context);
        line.setMinimumHeight(0);
        line.setPadding(0, 0, 0, 0);
        line.addView(UiKit.text(this.context, label, 10, opaque(palette.label())), UiKit.wrap());
        line.addView(UiKit.text(this.context, value, 10, opaque(palette.value(ValueLevel.NORMAL))), UiKit.wrap());
        return line;
    }

    private void addDisplay() {
        LinearLayout card = section("narutooverlay.config.appearance.display");

        card.addView(UiKit.switchRow(this.context,
                UiKit.str(GROUP + "appearance.dropShadow"),
                UiKit.str(GROUP + "appearance.dropShadow.desc"),
                RenderArgs.DROP_SHADOW.get(),
                checked -> {
                    RenderArgs.DROP_SHADOW.set(checked);
                    ConfigSaver.changed();
                }), UiKit.fill());
        card.addView(UiKit.divider(this.context), dividerParams());

        card.addView(UiKit.switchRow(this.context,
                UiKit.str(GROUP + "appearance.equalWidth"),
                UiKit.str(GROUP + "appearance.equalWidth.desc"),
                RenderArgs.EQUAL_WIDTH.get(),
                checked -> {
                    RenderArgs.EQUAL_WIDTH.set(checked);
                    ConfigSaver.changed();
                }), UiKit.fill());
        card.addView(UiKit.divider(this.context), dividerParams());

        card.addView(UiKit.switchRow(this.context,
                UiKit.str(GROUP + "appearance.drawBackplane"),
                UiKit.str(GROUP + "appearance.drawBackplane.desc"),
                RenderArgs.DRAW_BACKPLANE.get(),
                checked -> {
                    RenderArgs.DRAW_BACKPLANE.set(checked);
                    ConfigSaver.changed();
                }), UiKit.fill());
    }

    private void addBackplane() {
        LinearLayout card = section("narutooverlay.config.appearance.backplane");

        TextView value = UiKit.text(this.context, String.format(java.util.Locale.ROOT, "%.2f",
                RenderArgs.BACKPLANE_BORDER_WIDTH.get()), 12, UiKit.primary(this.context));

        LinearLayout titleRow = UiKit.row(this.context);
        titleRow.addView(UiKit.text(this.context, UiKit.str(GROUP + "appearance.borderWidth"), 14,
                UiKit.onSurface(this.context)), UiKit.weight());
        titleRow.addView(value, UiKit.wrap());
        card.addView(titleRow, UiKit.fill());

        int progress = (int) Math.round(RenderArgs.BACKPLANE_BORDER_WIDTH.get() * 100.0D);
        SeekBar slider = UiKit.slider(this.context, 400, progress);
        LinearLayout.LayoutParams sliderParams = UiKit.fill();
        sliderParams.setMargins(UiKit.dp(this.context, 2), 0, UiKit.dp(this.context, 2), 0);
        card.addView(slider, sliderParams);

        slider.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (!fromUser) return;
                double width = progress / 100.0D;
                RenderArgs.BACKPLANE_BORDER_WIDTH.set(width);
                value.setText(String.format(java.util.Locale.ROOT, "%.2f", width));
                ConfigSaver.changed();
            }
        });

        card.addView(UiKit.divider(this.context), dividerParams());

        card.addView(UiKit.switchRow(this.context,
                UiKit.str(GROUP + "appearance.borderOverride"),
                UiKit.str(GROUP + "appearance.borderOverride.desc"),
                RenderArgs.BACKPLANE_BORDER_OVERRIDE.get(),
                checked -> {
                    RenderArgs.BACKPLANE_BORDER_OVERRIDE.set(checked);
                    ConfigSaver.changed();
                    rebuild();
                }), UiKit.fill());

        addColorBlock(card);
    }

    private void addColorBlock(@NotNull LinearLayout card) {
        LinearLayout block = UiKit.column(this.context);

        View swatch = new View(this.context);
        swatch.setBackground(swatchDrawable(RenderArgs.borderColor()));

        LinearLayout preview = UiKit.row(this.context);
        LinearLayout.LayoutParams swatchParams = new LinearLayout.LayoutParams(UiKit.dp(this.context, 26),
                UiKit.dp(this.context, 26));
        swatchParams.setMargins(0, 0, UiKit.dp(this.context, 10), 0);
        preview.addView(swatch, swatchParams);
        preview.addView(UiKit.text(this.context, UiKit.str(GROUP + "appearance.borderColor"), 13,
                UiKit.onSurface(this.context)), UiKit.weight());

        Button pick = UiKit.button(this.context, UiKit.str(GROUP + "appearance.pickColor"), true,
                view -> {
                    ConfigFragment host = ConfigFragment.active();
                    if (host == null) return;
                    host.showOverlay(new ColorOverlay(host.requireContext(), RenderArgs.borderColor(),
                            this::applyPickedColor));
                });
        LinearLayout.LayoutParams pickParams = UiKit.wrap();
        pickParams.setMargins(UiKit.dp(this.context, 10), 0, 0, 0);
        preview.addView(pick, pickParams);

        block.addView(preview, UiKit.fill());

        LinearLayout channels = UiKit.row(this.context);
        channels.addView(channel("R", RenderArgs.BACKPLANE_BORDER_RED::get,
                RenderArgs.BACKPLANE_BORDER_RED::set, swatch), channelParams(0));
        channels.addView(channel("G", RenderArgs.BACKPLANE_BORDER_GREEN::get,
                RenderArgs.BACKPLANE_BORDER_GREEN::set, swatch), channelParams(1));
        channels.addView(channel("B", RenderArgs.BACKPLANE_BORDER_BLUE::get,
                RenderArgs.BACKPLANE_BORDER_BLUE::set, swatch), channelParams(2));
        channels.addView(channel("A", RenderArgs.BACKPLANE_BORDER_ALPHA::get,
                RenderArgs.BACKPLANE_BORDER_ALPHA::set, swatch), channelParams(3));
        block.addView(channels, UiKit.fill());

        ThemeControl.setViewAndChildrenEnabled(block, RenderArgs.BACKPLANE_BORDER_OVERRIDE.get());
        card.addView(block, UiKit.fill());
    }

    private @NotNull View channel(@NotNull String label, @NotNull IntSupplier getter,
                                  @NotNull IntConsumer setter, @NotNull View swatch) {
        LinearLayout box = UiKit.column(this.context);
        box.addView(UiKit.text(this.context, label, 10, UiKit.onSurfaceVariant(this.context)), UiKit.wrap());

        EditText field = UiKit.numberField(this.context, getter.getAsInt());
        field.setTextSize(12);
        UiKit.bindNumber(field, 0, 255, value -> {
            setter.accept(value);
            ConfigSaver.changed();
            swatch.setBackground(swatchDrawable(RenderArgs.borderColor()));
        });
        box.addView(field, UiKit.fill());
        return box;
    }

    private @NotNull ShapeDrawable swatchDrawable(int color) {
        ShapeDrawable drawable = new ShapeDrawable();
        drawable.setCornerRadius(UiKit.dp(this.context, 6));
        drawable.setColor(color);
        drawable.setStroke(Math.max(1, UiKit.dp(this.context, 1)),
                ColorStateList.modulateColor(0xFFFFFFFF, 0.25F));
        return drawable;
    }

    private void applyPickedColor(int argb) {
        RenderArgs.BACKPLANE_BORDER_OVERRIDE.set(true);
        RenderArgs.BACKPLANE_BORDER_RED.set((argb >> 16) & 0xFF);
        RenderArgs.BACKPLANE_BORDER_GREEN.set((argb >> 8) & 0xFF);
        RenderArgs.BACKPLANE_BORDER_BLUE.set(argb & 0xFF);
        RenderArgs.BACKPLANE_BORDER_ALPHA.set((argb >>> 24) & 0xFF);
        ConfigSaver.changed();

        rebuild();
    }

    private void addPerformance() {
        LinearLayout card = section("narutooverlay.config.appearance.performance");

        card.addView(numberRow("narutooverlay.config.appearance.fpsInterval",
                RenderArgs.FPS_COOLDOWN.get(), 1, Integer.MAX_VALUE,
                value -> {
                    RenderArgs.FPS_COOLDOWN.set(value);
                    ConfigSaver.changed();
                }), UiKit.fill());
        card.addView(UiKit.divider(this.context), dividerParams());

        card.addView(numberRow("narutooverlay.config.appearance.gcInterval",
                RenderArgs.GC_COOLDOWN.get(), 1, Integer.MAX_VALUE,
                value -> {
                    RenderArgs.GC_COOLDOWN.set(value);
                    ConfigSaver.changed();
                }), UiKit.fill());
        card.addView(UiKit.divider(this.context), dividerParams());

        card.addView(numberRow("narutooverlay.config.appearance.ramInterval",
                RenderArgs.RAM_COOLDOWN.get(), 1, Integer.MAX_VALUE,
                value -> {
                    RenderArgs.RAM_COOLDOWN.set(value);
                    ConfigSaver.changed();
                }), UiKit.fill());
        card.addView(UiKit.divider(this.context), dividerParams());

        card.addView(numberRow("narutooverlay.config.appearance.fpsWindow",
                RenderArgs.FPS_WINDOW.get(), 10, 1000000,
                value -> {
                    RenderArgs.FPS_WINDOW.set(value);
                    ConfigSaver.changed();
                }), UiKit.fill());
    }

    private @NotNull LinearLayout numberRow(@NotNull String titleKey, int value, int min, int max,
                                            @NotNull IntConsumer setter) {
        LinearLayout row = UiKit.row(this.context);
        row.addView(UiKit.text(this.context, UiKit.str(titleKey), 14, UiKit.onSurface(this.context)),
                UiKit.weight());

        EditText field = UiKit.numberField(this.context, value);
        field.setTextSize(12);
        UiKit.bindNumber(field, min, max, setter);

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(UiKit.dp(this.context, 96),
                UiKit.WRAP);
        params.setMargins(UiKit.dp(this.context, 12), 0, 0, 0);
        row.addView(field, params);
        return row;
    }

    private @NotNull LinearLayout.LayoutParams dividerParams() {
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(UiKit.MATCH, UiKit.dp(this.context, 1));
        params.setMargins(0, UiKit.dp(this.context, 2), 0, UiKit.dp(this.context, 2));
        return params;
    }

    private @NotNull LinearLayout.LayoutParams channelParams(int index) {
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(0, UiKit.WRAP, 1.0F);
        int gap = UiKit.dp(this.context, 8);
        params.setMargins(index == 0 ? 0 : gap, UiKit.dp(this.context, 6), 0, 0);
        return params;
    }
}
