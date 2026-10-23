package me.kall.narutooverlay.config.ui.widget;

import me.kall.narutooverlay.config.RenderArgs;

import icyllis.modernui.core.Context;
import icyllis.modernui.util.ColorStateList;
import icyllis.modernui.view.View;
import icyllis.modernui.widget.SeekBar;
import org.jetbrains.annotations.NotNull;

/**
 * A seek bar whose progress line and thumb follow the configuration screen's chakra ripple.
 */
public final class ShimmerSeekBar extends SeekBar implements ShimmerTarget {
    private int baseColor = 0xFFFFFFFF;
    private boolean bound;

    public ShimmerSeekBar(@NotNull Context context) {
        super(context);
    }

    void bind(int baseColor) {
        this.baseColor = baseColor;
        this.bound = true;
        if (isAttachedToWindow()) TextShimmer.attach(this);
    }

    @Override
    public int shimmerBaseColor() {
        return this.baseColor;
    }

    @Override
    public void applyShimmerColor(int color) {
        setProgressTintList(ColorStateList.valueOf(color));
        setThumbTintList(ColorStateList.valueOf(color));
    }

    @Override
    public void applyBaseColor() {
        applyShimmerColor(this.baseColor);
    }

    @Override
    public boolean shimmerEnabled() {
        return this.bound && isEnabled() && RenderArgs.CONTROL_SHIMMER.get();
    }

    @Override
    public @NotNull View shimmerHost() {
        return this;
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        if (this.bound) TextShimmer.attach(this);
    }

    @Override
    protected void onDetachedFromWindow() {
        TextShimmer.detach(this);
        super.onDetachedFromWindow();
    }
}