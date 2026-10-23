package me.kall.narutooverlay.config.ui.widget;

import me.kall.narutooverlay.config.RenderArgs;

import icyllis.modernui.graphics.drawable.ShapeDrawable;
import icyllis.modernui.view.View;
import org.jetbrains.annotations.NotNull;

/**
 * The rounded background of the navigation logo; it ripples with the surrounding text.
 */
public final class ShimmerCircleDrawable extends ShapeDrawable implements ShimmerTarget {
    private final View host;
    private int baseColor;

    public ShimmerCircleDrawable(@NotNull View host, int baseColor) {
        this.host = host;
        this.baseColor = baseColor;
        setColor(baseColor);
    }

    public void setBaseColor(int color) {
        this.baseColor = color;
        if (!TextShimmer.isRunning()) {
            setColor(color);
            invalidateSelf();
        }
    }

    @Override
    public int shimmerBaseColor() {
        return this.baseColor;
    }

    @Override
    public void applyShimmerColor(int color) {
        setColor(color);
        invalidateSelf();
    }

    @Override
    public void applyBaseColor() {
        setColor(this.baseColor);
        invalidateSelf();
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