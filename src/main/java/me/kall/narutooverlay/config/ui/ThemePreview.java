package me.kall.narutooverlay.config.ui;

import icyllis.modernui.core.Context;
import icyllis.modernui.graphics.Canvas;
import icyllis.modernui.graphics.Paint;
import icyllis.modernui.graphics.drawable.ShapeDrawable;
import icyllis.modernui.view.Gravity;
import icyllis.modernui.view.View;
import icyllis.modernui.widget.LinearLayout;
import icyllis.modernui.widget.TextView;
import me.kall.narutooverlay.theme.Palette;
import me.kall.narutoui.mui.layout.MuiControls;
import me.kall.narutoui.mui.shimmer.MuiShimmer;
import me.kall.narutoui.mui.shimmer.MuiShimmerContainer;
import me.kall.narutoui.mui.shimmer.MuiShimmerProfile;
import me.kall.narutoui.mui.shimmer.MuiShimmerScope;
import me.kall.narutoui.mui.shimmer.MuiShimmerable;
import me.kall.narutoui.mui.shimmer.ShimmerTarget;
import me.kall.narutoui.mui.shimmer.widget.ShimmerTextView;
import org.jetbrains.annotations.NotNull;

/**
 * A miniature overlay card used by the theme picker. It follows the HUD card layout: a rounded
 * backplane, an optional vertical accent strip, and three stacked metric rows.
 * <p>
 * Themes with an accent color (NARUTO) use shimmer-aware text and bars. Themes without an accent
 * (CLASSIC) deliberately use plain white text and static bars so the preview keeps its own palette.
 */
public final class ThemePreview extends LinearLayout implements MuiShimmerContainer {
    /** Whether this preview should participate in shimmer. */
    private final boolean shimmerText;

    /**
     * Creates a preview for one palette.
     *
     * @param context the screen context
     * @param palette the candidate palette
     */
    public ThemePreview(@NotNull Context context, @NotNull Palette palette) {
        super(context);
        this.shimmerText = palette.hasAccent();
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

    @Override
    public void setShimmerable(@NotNull MuiShimmerable marker) {
        if (!this.shimmerText) return;

        for (int i = 0; i < this.getChildCount(); i++) {
            MuiShimmerScope.apply(this.getChildAt(i), marker);
        }
        MuiShimmer.refresh();
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
        TextView view = this.shimmerText ? new ShimmerTextView(context) : new TextView(context);
        view.setText(text);
        view.setTextSize(size);
        view.setTextColor(color);
        return view;
    }

    private @NotNull View bar(@NotNull Context context, int color, int heightDp, int cornerRadiusDp) {
        if (this.shimmerText) return new PreviewBar(context, color, heightDp, cornerRadiusDp);

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

    /**
     * A tiny rounded bar that participates in the shared shimmer instead of using a static drawable.
     */
    private static final class PreviewBar extends View implements ShimmerTarget {
        private final Paint paint = new Paint();
        private final MuiShimmerProfile profile;
        private final int cornerRadius;
        private final int baseColor;
        private MuiShimmerable shimmerable = MuiShimmerable.NEVER;

        PreviewBar(@NotNull Context context, int color, int heightDp, int cornerRadiusDp) {
            super(context);
            this.profile = MuiShimmerProfile.current();
            this.baseColor = color;
            this.cornerRadius = MuiControls.dp(context, cornerRadiusDp);
            this.paint.setAntiAlias(true);
            this.paint.setColor(color);
            this.setMinimumHeight(MuiControls.dp(context, heightDp));
        }

        @Override
        protected void onDraw(@NotNull Canvas canvas) {
            if (this.getWidth() <= 0 || this.getHeight() <= 0) return;

            if (this.cornerRadius > 0) {
                canvas.drawRoundRect(0.0F, 0.0F, this.getWidth(), this.getHeight(), this.cornerRadius, this.paint);
            } else {
                canvas.drawRect(0.0F, 0.0F, this.getWidth(), this.getHeight(), this.paint);
            }
        }

        @Override
        public int shimmerBaseColor() {
            return this.baseColor;
        }

        @Override
        public void applyShimmerColor(int color) {
            this.paint.setColor(color);
            this.invalidate();
        }

        @Override
        public void applyBaseColor() {
            this.paint.setColor(this.baseColor);
            this.invalidate();
        }

        @Override
        public boolean shimmerEnabled() {
            return this.shimmerable.shimmer() && this.isAttachedToWindow();
        }

        @Override
        public void setShimmerable(@NotNull MuiShimmerable marker) {
            this.shimmerable = marker;
            MuiShimmer.refresh();
        }

        @Override
        public @NotNull MuiShimmerProfile shimmerProfile() {
            return this.profile;
        }

        @Override
        public @NotNull View shimmerHost() {
            return this;
        }

        @Override
        protected void onAttachedToWindow() {
            super.onAttachedToWindow();
            MuiShimmer.attach(this);
        }

        @Override
        protected void onDetachedFromWindow() {
            MuiShimmer.detach(this);
            super.onDetachedFromWindow();
        }
    }
}