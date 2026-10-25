package me.kall.narutooverlay.text;

import me.kall.narutooverlay.config.RenderArgs;
import net.minecraft.Util;

/**
 * Shared clock for the HUD and configuration-screen shimmer.
 * <p>
 * Speed is integrated over time instead of multiplying the absolute uptime, so changing the speed
 * slider only changes the rate from that point on and never teleports the phase.
 * <p>
 * Density is applied as a short cross-fade between the previous and the new spatial frequency
 * after the slider has been idle for a moment. Moving the density slider therefore freezes the
 * current colours instead of sweeping every glyph through several hues, and the eventual change
 * is a smooth morph rather than a flash.
 */
public final class ShimmerClock {
    private static final double MAX_STEP_SECONDS = 0.1D;
    private static final double SMOOTHING = 10.0D;
    private static final long SCALE_DEBOUNCE_MS = 140L;
    private static final long SCALE_TRANSITION_MS = 900L;
    private static final double SCALE_EPSILON = 0.001D;

    private static boolean initialized;
    private static long lastMillis;
    private static double time;
    private static double speed = 1.0D;
    private static double strength = 1.0D;

    private static double scaleFrom = 1.0D;
    private static double scaleTo = 1.0D;
    private static double scaleMix = 1.0D;
    private static double pendingScale = 1.0D;
    private static long pendingSince;
    private static long scaleTransitionStart;
    private static boolean scaleTransition;

    private ShimmerClock() {}

    public static synchronized void update() {
        long now = Util.getMillis();

        double targetSpeed = RenderArgs.TEXT_SHIMMER_SPEED.get();
        double targetScale = RenderArgs.TEXT_SHIMMER_SCALE.get();
        double targetStrength = RenderArgs.TEXT_SHIMMER_STRENGTH.get();

        if (!initialized) {
            initialized = true;
            lastMillis = now;
            pendingSince = now;
            speed = targetSpeed;
            strength = targetStrength;
            scaleFrom = targetScale;
            scaleTo = targetScale;
            pendingScale = targetScale;
            scaleMix = 1.0D;
            return;
        }

        double dt = Math.max(0.0D, Math.min(MAX_STEP_SECONDS, (now - lastMillis) / 1000.0D));
        lastMillis = now;

        if (dt > 0.0D) {
            double blend = 1.0D - Math.exp(-dt * SMOOTHING);
            speed += (targetSpeed - speed) * blend;
            strength += (targetStrength - strength) * blend;
            time += dt * speed;
        }

        if (Math.abs(targetScale - pendingScale) > SCALE_EPSILON) {
            pendingScale = targetScale;
            pendingSince = now;
        }

        if (scaleTransition) {
            double progress = (now - scaleTransitionStart) / (double) SCALE_TRANSITION_MS;
            if (progress >= 1.0D) {
                scaleFrom = scaleTo;
                scaleMix = 1.0D;
                scaleTransition = false;
            } else {
                double p = Math.max(0.0D, progress);
                scaleMix = p * p * (3.0D - 2.0D * p);
            }
        }

        if (!scaleTransition && now - pendingSince >= SCALE_DEBOUNCE_MS
                && Math.abs(pendingScale - scaleTo) > SCALE_EPSILON) {
            scaleFrom = scaleTo;
            scaleTo = pendingScale;
            scaleMix = 0.0D;
            scaleTransitionStart = now;
            scaleTransition = true;
        }
    }

    public static synchronized double time() {
        return time;
    }

    public static synchronized double scaleFrom() {
        return scaleFrom;
    }

    public static synchronized double scaleTo() {
        return scaleTo;
    }

    public static synchronized double scaleMix() {
        return scaleMix;
    }

    public static synchronized double strength() {
        return strength;
    }
}