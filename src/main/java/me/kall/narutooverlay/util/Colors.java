package me.kall.narutooverlay.util;

public final class Colors {
    public static int scaleAlpha(int argb, float factor) {
        int alpha = Math.round((argb >>> 24) * Math.max(0.0F, Math.min(1.0F, factor)));
        return alpha << 24 | argb & 0xFFFFFF;
    }
}
