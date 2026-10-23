package me.kall.narutooverlay.config.ui.widget;

import me.kall.narutooverlay.config.RenderArgs;

import icyllis.modernui.core.Context;
import icyllis.modernui.util.ColorStateList;
import icyllis.modernui.view.View;
import icyllis.modernui.widget.TextView;
import org.jetbrains.annotations.NotNull;

/**
 * A text view that participates in the configuration screen's chakra ripple.
 * The base color is remembered so the ripple can always be blended from it.
 */
public final class ShimmerTextView extends TextView implements ShimmerTarget {
    private int baseColor = 0xFFFFFFFF;
    private boolean shimmer = true;

    public ShimmerTextView(@NotNull Context context) {
        super(context);
    }

    public void setShimmerEnabled(boolean enabled) {
        this.shimmer = enabled;
    }

    @Override
    public void setTextColor(int color) {
        this.baseColor = color;
        // During TextView's own constructor mTextColor is still null; it must be initialised,
        // otherwise the subsequent setHintTextColor() call inside the constructor NPEs.
        if (getTextColors() == null || !TextShimmer.isRunning()) super.setTextColor(color);
    }

    @Override
    public void setTextColor(@NotNull ColorStateList colors) {
        this.baseColor = colors.getDefaultColor();
        if (getTextColors() == null || !TextShimmer.isRunning()) super.setTextColor(colors);
    }

    @Override
    public int shimmerBaseColor() {
        return this.baseColor;
    }

    @Override
    public void applyShimmerColor(int color) {
        super.setTextColor(color);
    }

    @Override
    public void applyBaseColor() {
        super.setTextColor(this.baseColor);
    }

    @Override
    public boolean shimmerEnabled() {
        return this.shimmer && isEnabled() && RenderArgs.CONFIG_TEXT_SHIMMER.get();
    }

    @Override
    public @NotNull View shimmerHost() {
        return this;
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        TextShimmer.attach(this);
    }

    @Override
    protected void onDetachedFromWindow() {
        TextShimmer.detach(this);
        super.onDetachedFromWindow();
    }
}