package me.kall.narutooverlay.config.ui.widget;

import icyllis.modernui.view.View;
import org.jetbrains.annotations.Nullable;

/**
 * Anything that can take part in the configuration screen's chakra ripple.
 */
public interface ShimmerTarget {
    int shimmerBaseColor();

    void applyShimmerColor(int color);

    void applyBaseColor();

    boolean shimmerEnabled();

    default float shimmerStrength() {
        return 0.65F;
    }

    @Nullable View shimmerHost();
}