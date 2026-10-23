package me.kall.narutooverlay.config.ui.widget;

import me.kall.narutooverlay.config.RenderArgs;

import icyllis.modernui.graphics.drawable.ShapeDrawable;
import icyllis.modernui.util.ColorStateList;
import icyllis.modernui.view.View;
import org.jetbrains.annotations.NotNull;

/**
 * The rounded outline behind the bottom-left ESC hint; its outline follows the chakra ripple.
 */
public final class ShimmerHintDrawable extends ShapeDrawable implements ShimmerTarget {
    private final View host;
    private final int strokeWidth;
    private int baseColor;

    public ShimmerHintDrawable(@NotNull View host, int baseColor) {
        this.host = host;
        this.baseColor = baseColor;
        this.strokeWidth = Math.max(1, UiKit.dp(host.getContext(), 1));
        this.applyColor(baseColor);
    }

    public void setBaseColor(int color) {
        this.baseColor = color;
        if (!TextShimmer.isRunning()) {
            this.applyColor(color);
            invalidateSelf();
        }
    }

    @Override
    public int shimmerBaseColor() {
        return this.baseColor;
    }

    @Override
    public void applyShimmerColor(int color) {
        this.applyColor(color);
        invalidateSelf();
    }

    @Override
    public void applyBaseColor() {
        this.applyColor(this.baseColor);
        invalidateSelf();
    }

    private void applyColor(int color) {
        setColor(ColorStateList.modulateColor(color, 0.14F));
        setStroke(this.strokeWidth, ColorStateList.modulateColor(color, 0.7F));
    }

    @Override
    public boolean shimmerEnabled() {
        return this.host.isAttachedToWindow() && RenderArgs.CONTROL_SHIMMER.get();
    }

    @Override
    public @NotNull View shimmerHost() {
        return this.host;
    }
}