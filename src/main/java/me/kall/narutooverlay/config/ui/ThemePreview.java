package me.kall.narutooverlay.config.ui;

import icyllis.modernui.core.Context;
import icyllis.modernui.graphics.drawable.ShapeDrawable;
import icyllis.modernui.view.Gravity;
import icyllis.modernui.view.View;
import icyllis.modernui.widget.LinearLayout;
import icyllis.modernui.widget.TextView;
import me.kall.narutooverlay.theme.Palette;
import me.kall.narutoui.mui.layout.MuiControls;
import org.jetbrains.annotations.NotNull;

/**
 * A miniature overlay card used by the theme picker.
 * <p>
 * The preview is deliberately static: it shows the candidate palette exactly as it will be drawn,
 * without borrowing the shimmer ramp of the currently selected theme. Letting a JADE card shimmer
 * with Naruto's gold/red ramp was the source of the mixed-color look.
 */
public final class ThemePreview extends LinearLayout {
    /**
     * Creates a preview for one palette.
     *
     * @param context the screen context
     * @param palette the candidate palette
     */
    public ThemePreview(@NotNull Context context, @NotNull Palette palette) {
        super(context);
        this.setOrientation(HORIZONTAL);
        this.setGravity(Gravity.CENTER_VERTICAL);
        this.setMinimumHeight(MuiControls.dp(context, 76));

        ShapeDrawable background = new ShapeDrawable();
        background.setCornerRadius(MuiControls.dp(context, 7));
        background.setColor(palette.backplane());
        background.setStroke(Math.max(1, MuiControls.dp(context, 1)), palette.border());
        this.setBackground(background);

        if (palette.hasAccent()) {
            View accent = this.bar(context, palette.accent(), 60, 2);
            LinearLayout.LayoutParams accentParams = new LinearLayout.LayoutParams(MuiControls.dp(context, 3), MuiControls.dp(context, 60));
            accentParams.leftMargin = MuiControls.dp(context, 4);
            accentParams.rightMargin = MuiControls.dp(context, 6);
            this.addView(accent, accentParams);
        }

        LinearLayout content = new LinearLayout(context);
        content.setOrientation(VERTICAL);
        content.setPadding(MuiControls.dp(context, 8), MuiControls.dp(context, 6), MuiControls.dp(context, 8), MuiControls.dp(context, 6));

        int ramColor = opaque(palette.hasAccent() ? palette.warn() : palette.normal());
        content.addView(this.field(context, palette, "FPS", "144", "fps", opaque(palette.normal())), this.rowParams(context));
        content.addView(this.separator(context, opaque(palette.separator())), this.separatorParams(context));
        content.addView(this.field(context, palette, "GC", "12", "ms", opaque(palette.normal())), this.rowParams(context));
        content.addView(this.separator(context, opaque(palette.separator())), this.separatorParams(context));
        content.addView(this.field(context, palette, "RAM", "62", "%", ramColor), this.rowParams(context));

        this.addView(content, new LinearLayout.LayoutParams(0, MuiControls.WRAP, 1.0F));
    }

    private static int opaque(int color) {
        return color | 0xFF000000;
    }

    private @NotNull View field(
            @NotNull Context context,
            @NotNull Palette palette,
            @NotNull String label,
            @NotNull String value,
            @NotNull String unit,
            int valueColor
    ) {
        LinearLayout row = new LinearLayout(context);
        row.setOrientation(HORIZONTAL);
        row.setGravity(Gravity.CENTER_VERTICAL);

        row.addView(this.text(context, label, 9, opaque(palette.label())), MuiControls.wrap());

        LinearLayout.LayoutParams valueParams = MuiControls.wrap();
        valueParams.leftMargin = MuiControls.dp(context, 5);
        row.addView(this.text(context, value, 11, valueColor), valueParams);

        TextView unitView = this.text(context, unit, 8, opaque(palette.unit()));
        LinearLayout.LayoutParams unitParams = MuiControls.wrap();
        unitParams.leftMargin = MuiControls.dp(context, 2);
        row.addView(unitView, unitParams);

        return row;
    }

    private @NotNull TextView text(@NotNull Context context, @NotNull String text, float size, int color) {
        TextView view = new TextView(context);
        view.setText(text);
        view.setTextSize(size);
        view.setTextColor(color);
        return view;
    }

    private @NotNull View bar(@NotNull Context context, int color, int heightDp, int cornerRadiusDp) {
        View bar = new View(context);
        ShapeDrawable background = new ShapeDrawable();
        background.setCornerRadius(MuiControls.dp(context, cornerRadiusDp));
        background.setColor(color);
        bar.setBackground(background);
        return bar;
    }

    private @NotNull View separator(@NotNull Context context, int color) {
        return this.bar(context, color, 1, 0);
    }

    private @NotNull LinearLayout.LayoutParams separatorParams(@NotNull Context context) {
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(MuiControls.MATCH, Math.max(1, MuiControls.dp(context, 1)));
        params.topMargin = MuiControls.dp(context, 2);
        params.bottomMargin = MuiControls.dp(context, 2);
        return params;
    }

    private @NotNull LinearLayout.LayoutParams rowParams(@NotNull Context context) {
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(MuiControls.MATCH, MuiControls.WRAP);
        params.topMargin = MuiControls.dp(context, 2);
        params.bottomMargin = MuiControls.dp(context, 2);
        return params;
    }
}