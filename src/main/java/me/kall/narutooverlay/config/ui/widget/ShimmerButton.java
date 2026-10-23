package me.kall.narutooverlay.config.ui.widget;

import me.kall.narutooverlay.config.RenderArgs;

import icyllis.modernui.core.Context;
import icyllis.modernui.resources.ResourceId;
import icyllis.modernui.util.AttributeSet;
import icyllis.modernui.util.ColorStateList;
import icyllis.modernui.view.View;
import icyllis.modernui.widget.Button;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * A button that participates in the configuration screen's chakra ripple.
 * Disabled buttons keep their original state colours instead of shimmering.
 */
public final class ShimmerButton extends Button implements ShimmerTarget {
    private ColorStateList baseColors = ColorStateList.valueOf(0xFFFFFFFF);
    private int baseColor = 0xFFFFFFFF;

    public ShimmerButton(@NotNull Context context, @Nullable AttributeSet attrs,
                         @Nullable ResourceId defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    public void setTextColor(int color) {
        this.baseColor = color;
        this.baseColors = ColorStateList.valueOf(color);
        if (getTextColors() == null || !TextShimmer.isRunning()) super.setTextColor(color);
    }

    @Override
    public void setTextColor(@NotNull ColorStateList colors) {
        this.baseColors = colors;
        this.baseColor = colors.getDefaultColor();
        if (getTextColors() == null || !TextShimmer.isRunning()) super.setTextColor(colors);
    }

    @Override
    public int shimmerBaseColor() {
        return this.baseColors.getColorForState(getDrawableState(), this.baseColor);
    }

    @Override
    public void applyShimmerColor(int color) {
        super.setTextColor(color);
    }

    @Override
    public void applyBaseColor() {
        super.setTextColor(this.baseColors);
    }

    @Override
    public boolean shimmerEnabled() {
        return isAttachedToWindow() && RenderArgs.CONFIG_TEXT_SHIMMER.get();
    }

    @Override
    public float shimmerStrength() {
        return isEnabled() ? 0.65F : 0.3F;
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