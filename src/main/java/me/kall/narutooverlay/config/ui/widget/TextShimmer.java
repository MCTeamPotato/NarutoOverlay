package me.kall.narutooverlay.config.ui.widget;

import icyllis.modernui.animation.ValueAnimator;
import icyllis.modernui.view.View;
import icyllis.modernui.view.ViewParent;
import me.kall.narutooverlay.config.RenderArgs;
import me.kall.narutooverlay.theme.Palette;
import net.minecraft.Util;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

/**
 * Drives the chakra ripple across the text and accents of the configuration screen.
 * <p>
 * Two slowly interfering waves are sampled at each target's absolute position, so neighbouring
 * labels do not all change to the same colour at the same time. The resulting wave value is
 * mapped to the theme's white / yellow / gold / orange / red ramp.
 */
@SuppressWarnings("BooleanMethodIsAlwaysInverted")
public final class TextShimmer {
    private static final float SPATIAL_X = 0.008F;
    private static final float SPATIAL_Y = 0.014F;
    private static final float SPATIAL_X2 = 0.005F;
    private static final float SPATIAL_Y2 = 0.009F;
    private static final float TIME_SCALE = 0.45F;

    private static final List<ShimmerTarget> TARGETS = new ArrayList<>();
    private static @Nullable ValueAnimator animator;
    private static long lastStep;

    private TextShimmer() {}

    public static void attach(@NotNull ShimmerTarget target) {
        if (!TARGETS.contains(target)) TARGETS.add(target);
        refresh();
    }

    public static void detach(@Nullable ShimmerTarget target) {
        if (target == null) return;
        TARGETS.remove(target);
        if (TARGETS.isEmpty()) stop();
    }

    public static void refresh() {
        if (!enabled()) {
            restore();
            stop();
            return;
        }
        start();
    }

    public static boolean isRunning() {
        return animator != null;
    }

    private static boolean enabled() {
        return RenderArgs.TEXT_SHIMMER.get()
                && RenderArgs.palette().hasAccent()
                && (RenderArgs.CONFIG_TEXT_SHIMMER.get() || RenderArgs.CONTROL_SHIMMER.get());
    }

    private static void start() {
        if (animator != null || TARGETS.isEmpty()) return;

        ValueAnimator valueAnimator = ValueAnimator.ofFloat(0.0F, 1.0F);
        valueAnimator.setDuration(1000L);
        valueAnimator.setRepeatCount(ValueAnimator.INFINITE);
        valueAnimator.addUpdateListener(animation -> tick());
        valueAnimator.start();

        lastStep = 0L;
        animator = valueAnimator;
    }

    private static void stop() {
        if (animator == null) return;
        animator.cancel();
        animator = null;
    }

    private static void restore() {
        for (ShimmerTarget target : TARGETS) {
            target.applyBaseColor();
        }
    }

    private static void tick() {
        long now = Util.getMillis();
        int fps = Math.max(1, RenderArgs.CONFIG_SHIMMER_FPS.get());
        long step = Math.max(1L, 1000L / fps);
        if (now - lastStep < step) return;
        lastStep = now;

        if (!enabled()) {
            restore();
            stop();
            return;
        }

        float time = now / 1000.0F;
        float speed = RenderArgs.TEXT_SHIMMER_SPEED.get().floatValue();
        float scale = RenderArgs.TEXT_SHIMMER_SCALE.get().floatValue();
        float strength = RenderArgs.TEXT_SHIMMER_STRENGTH.get().floatValue();
        int[] location = new int[2];
        int[] colors = ramp();
        float[] stops = {0.0F, 0.12F, 0.25F, 0.40F, 0.55F, 0.70F, 0.82F, 0.92F, 1.0F};

        for (ShimmerTarget target : TARGETS) {
            View host = target.shimmerHost();
            if (!target.shimmerEnabled() || host == null) {
                target.applyBaseColor();
                continue;
            }

            host.getLocationInWindow(location);
            int x = location[0];
            int y = location[1];

            // getLocationInWindow() has already subtracted every ancestor's scroll offset.
            // Add it back so the sampled position is in content space and stays fixed while
            // the page scrolls.
            ViewParent parent = host.getParent();
            while (parent instanceof View view) {
                x += view.getScrollX();
                y += view.getScrollY();
                parent = view.getParent();
            }

            float wave = wave(x, y, time, speed, scale);
            int color = rampColor(colors, stops, wave);
            target.applyShimmerColor(lerp(target.shimmerBaseColor(), color,
                    target.shimmerStrength() * strength));
        }
    }

    private static float wave(float x, float y, float time, float speed, float scale) {
        float t = time * TIME_SCALE * speed;
        float a = (float) Math.sin((x * SPATIAL_X + y * SPATIAL_Y) * scale - t);
        float b = (float) Math.sin((x * SPATIAL_X2 - y * SPATIAL_Y2) * scale + t * 0.7F + 1.3F);
        return (a + b) * 0.25F + 0.5F;
    }

    private static int[] ramp() {
        Palette palette = RenderArgs.palette();
        return new int[]{
                0xFFFFFFFF,
                palette.warn() | 0xFF000000,
                palette.accent() | 0xFF000000,
                palette.label() | 0xFF000000,
                palette.critical() | 0xFF000000,
                palette.label() | 0xFF000000,
                palette.accent() | 0xFF000000,
                palette.warn() | 0xFF000000,
                0xFFFFFFFF
        };
    }

    private static int rampColor(int[] colors, float[] stops, float value) {
        float v = Math.max(0.0F, Math.min(1.0F, value));

        for (int i = 1; i < stops.length; i++) {
            if (v > stops[i]) continue;

            float span = stops[i] - stops[i - 1];
            float fraction = span <= 0.0F ? 0.0F : (v - stops[i - 1]) / span;
            return lerp(colors[i - 1], colors[i], fraction);
        }
        return colors[colors.length - 1];
    }

    private static int lerp(int from, int to, float fraction) {
        float f = Math.max(0.0F, Math.min(1.0F, fraction));
        int r = (int) ((from >> 16 & 0xFF) + ((to >> 16 & 0xFF) - (from >> 16 & 0xFF)) * f);
        int g = (int) ((from >> 8 & 0xFF) + ((to >> 8 & 0xFF) - (from >> 8 & 0xFF)) * f);
        int b = (int) ((from & 0xFF) + ((to & 0xFF) - (from & 0xFF)) * f);
        return (from & 0xFF000000) | r << 16 | g << 8 | b;
    }
}