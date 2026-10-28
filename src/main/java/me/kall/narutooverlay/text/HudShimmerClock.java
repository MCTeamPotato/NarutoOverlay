package me.kall.narutooverlay.text;

import me.kall.narutooverlay.config.RenderArgs;

/**
 * HUD-local shimmer clock. It deliberately does not depend on the NarutoUI API, so the HUD keeps
 * working when the optional configuration-screen library is absent.
 */
final class HudShimmerClock {
    private static final double MAX_STEP_SECONDS = 0.1D;
    private static final double SMOOTHING = 10.0D;
    private static final long SCALE_DEBOUNCE_MS = 140L;
    private static final long SCALE_TRANSITION_MS = 900L;
    private static final double SCALE_EPSILON = 0.001D;

    private boolean initialized;
    private long lastMillis;
    private double time;
    private double speed = 1.0D;
    private double strength = 1.0D;
    private double scaleFrom = 1.0D;
    private double scaleTo = 1.0D;
    private double scaleMix = 1.0D;
    private double pendingScale = 1.0D;
    private long pendingSince;
    private long scaleTransitionStart;
    private boolean scaleTransition;

    synchronized void update() {
        long now = System.currentTimeMillis();

        double targetSpeed = RenderArgs.TEXT_SHIMMER_SPEED.get();
        double targetScale = RenderArgs.TEXT_SHIMMER_SCALE.get();
        double targetStrength = RenderArgs.TEXT_SHIMMER_STRENGTH.get();

        if (!this.initialized) {
            this.initialized = true;
            this.lastMillis = now;
            this.pendingSince = now;
            this.speed = targetSpeed;
            this.strength = targetStrength;
            this.scaleFrom = targetScale;
            this.scaleTo = targetScale;
            this.pendingScale = targetScale;
            this.scaleMix = 1.0D;
            return;
        }

        double dt = Math.max(0.0D, Math.min(MAX_STEP_SECONDS, (now - this.lastMillis) / 1000.0D));
        this.lastMillis = now;

        if (dt > 0.0D) {
            double blend = 1.0D - Math.exp(-dt * SMOOTHING);
            this.speed += (targetSpeed - this.speed) * blend;
            this.strength += (targetStrength - this.strength) * blend;
            this.time += dt * this.speed;
        }

        if (Math.abs(targetScale - this.pendingScale) > SCALE_EPSILON) {
            this.pendingScale = targetScale;
            this.pendingSince = now;
        }

        if (this.scaleTransition) {
            double progress = (now - this.scaleTransitionStart) / (double) SCALE_TRANSITION_MS;
            if (progress >= 1.0D) {
                this.scaleFrom = this.scaleTo;
                this.scaleMix = 1.0D;
                this.scaleTransition = false;
            } else {
                double p = Math.max(0.0D, progress);
                this.scaleMix = p * p * (3.0D - 2.0D * p);
            }
        }

        if (!this.scaleTransition && now - this.pendingSince >= SCALE_DEBOUNCE_MS
                && Math.abs(this.pendingScale - this.scaleTo) > SCALE_EPSILON) {
            this.scaleFrom = this.scaleTo;
            this.scaleTo = this.pendingScale;
            this.scaleMix = 0.0D;
            this.scaleTransitionStart = now;
            this.scaleTransition = true;
        }
    }

    synchronized double time() {
        return this.time;
    }

    synchronized double scaleFrom() {
        return this.scaleFrom;
    }

    synchronized double scaleTo() {
        return this.scaleTo;
    }

    synchronized double scaleMix() {
        return this.scaleMix;
    }

    synchronized double strength() {
        return this.strength;
    }
}