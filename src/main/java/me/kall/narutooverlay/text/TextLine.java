package me.kall.narutooverlay.text;

import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import me.kall.narutooverlay.config.RenderArgs;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextColor;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.FormattedCharSink;
import net.minecraft.Util;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public final class TextLine {
    public static final int DIVIDER_GAP = 7;
    private static final HudShimmerClock SHIMMER_CLOCK = new HudShimmerClock();

    public enum Align {
        LEFT,
        RIGHT
    }

    private record Run(Component text, FormattedCharSequence sequence, int x) {}

    private final Run[] runs;
    private final int[] dividers;
    private final int width;

    private final CachedSequence[] shimmerCache;
    private long shimmerCacheAt;
    private int shimmerCacheLeft = Integer.MIN_VALUE;
    private boolean shimmerCacheValid;

    @Contract(pure = true)
    private TextLine(Run @NotNull [] runs, int[] dividers, int width) {
        this.runs = runs;
        this.dividers = dividers;
        this.width = width;
        this.shimmerCache = new CachedSequence[runs.length];
    }

    public int width() {
        return this.width;
    }

    public int[] dividers() {
        return this.dividers;
    }

    public void draw(@NotNull GuiGraphics graphics, @NotNull Font font, int left, int top, boolean shadow) {
        if (RenderArgs.TEXT_SHIMMER.get() && RenderArgs.HUD_TEXT_SHIMMER.get()
                && RenderArgs.palette().hasAccent()) {
            if (RenderArgs.TEXT_SHIMMER_CACHE.get()) {
                this.drawShimmerCached(graphics, font, left, top, shadow);
            } else {
                this.drawShimmerLegacy(graphics, font, left, top, shadow);
            }
            return;
        }

        float y = top + textOffset();
        for (Run run : this.runs) {
            drawRun(graphics, font, run.sequence(), left + run.x(), y, shadow);
        }
    }

    private void drawShimmerCached(@NotNull GuiGraphics graphics, @NotNull Font font,
                                   int left, int top, boolean shadow) {
        long now = Util.getMillis();
        int fps = Math.max(1, RenderArgs.TEXT_SHIMMER_FPS.get());
        long interval = Math.max(1L, 1000L / fps);

        if (!this.shimmerCacheValid || left != this.shimmerCacheLeft
                || now - this.shimmerCacheAt >= interval) {
            this.rebuildShimmerCache(left, now);
        }

        float y = top + textOffset();
        for (int i = 0; i < this.runs.length; i++) {
            CachedSequence sequence = this.shimmerCache[i];
            if (sequence == null) continue;

            drawRun(graphics, font, sequence, left + this.runs[i].x(), y, shadow);
        }
    }

    private void rebuildShimmerCache(int left, long now) {
        ShimmerState state = shimmerState();

        for (int i = 0; i < this.runs.length; i++) {
            this.shimmerCache[i] = buildShimmerCache(this.runs[i], left, state);
        }

        this.shimmerCacheAt = now;
        this.shimmerCacheLeft = left;
        this.shimmerCacheValid = true;
    }

    @Contract("_, _, _ -> new")
    private static @NotNull CachedSequence buildShimmerCache(@NotNull Run run, int left,
                                                             @NotNull ShimmerState state) {
        float baseX = left + run.x();
        IntArrayList indices = new IntArrayList();
        IntArrayList codepoints = new IntArrayList();
        ObjectArrayList<Style> styles = new ObjectArrayList<>();

        run.sequence().accept((index, style, codePoint) -> {
            indices.add(index);
            codepoints.add(codePoint);
            styles.add(style.withColor(TextColor.fromRgb(shimmerColor(style, baseX, index, state))));
            return true;
        });

        return new CachedSequence(indices.toIntArray(), codepoints.toIntArray(),
                styles.toArray(new Style[0]));
    }

    private void drawShimmerLegacy(@NotNull GuiGraphics graphics, @NotNull Font font,
                                   int left, int top, boolean shadow) {
        ShimmerState state = shimmerState();
        float y = top + textOffset();

        for (Run run : this.runs) {
            float baseX = left + run.x();

            FormattedCharSequence shimmered = sink -> run.sequence().accept((index, style, codePoint) -> {
                int color = shimmerColor(style, baseX, index, state);
                return sink.accept(index, style.withColor(TextColor.fromRgb(color)), codePoint);
            });

            drawRun(graphics, font, shimmered, baseX, y, shadow);
        }
    }

    private static @NotNull ShimmerState shimmerState() {
        SHIMMER_CLOCK.update();
        float time = (float) SHIMMER_CLOCK.time();
        float scaleA = (float) SHIMMER_CLOCK.scaleFrom();
        float scaleB = (float) SHIMMER_CLOCK.scaleTo();
        float scaleMix = (float) SHIMMER_CLOCK.scaleMix();
        float strength = (float) SHIMMER_CLOCK.strength();
        int accent = RenderArgs.palette().accent() | 0xFF000000;
        int highlight = lerp(accent, 0xFFFFFF, 0.25F);
        return new ShimmerState(time, scaleA, scaleB, scaleMix, strength, highlight);
    }

    private static int shimmerColor(@NotNull Style style, float baseX, int index, @NotNull ShimmerState state) {
        int base = style.getColor() == null ? 0xFFFFFF : style.getColor().getValue();
        float phaseA = (baseX + index * 5.6F * state.scaleA()) * 0.075F - state.time() * 1.9F;
        float phaseB = (baseX + index * 5.6F * state.scaleB()) * 0.075F - state.time() * 1.9F;
        float waveA = 0.5F + 0.5F * (float) Math.sin(phaseA);
        float waveB = 0.5F + 0.5F * (float) Math.sin(phaseB);
        float wave = waveA + (waveB - waveA) * state.scaleMix();
        return lerp(base, state.highlight(), wave * wave * state.strength());
    }

    private static void drawRun(@NotNull GuiGraphics graphics, @NotNull Font font,
                                @NotNull FormattedCharSequence sequence, float x, float y, boolean shadow) {
        font.drawInBatch(sequence, x, y, 0xFFFFFF, shadow,
                graphics.pose().last().pose(), graphics.bufferSource(),
                Font.DisplayMode.NORMAL, 0, 15728880);
    }

    private record ShimmerState(float time, float scaleA, float scaleB, float scaleMix,
                                float strength, int highlight) {}

    private record CachedSequence(int[] indices, int[] codepoints, Style[] styles) implements FormattedCharSequence {
        @Override
            public boolean accept(@NotNull FormattedCharSink sink) {
                for (int i = 0; i < this.codepoints.length; i++) {
                    if (!sink.accept(this.indices[i], this.styles[i], this.codepoints[i])) {
                        return false;
                    }
                }
                return true;
            }
        }

    private static float textOffset() {
        return (float) (double) RenderArgs.TEXT_OFFSET_Y.get();
    }

    private static int lerp(int from, int to, float fraction) {
        float f = Math.max(0.0F, Math.min(1.0F, fraction));
        int r = (int) ((from >> 16 & 0xFF) + ((to >> 16 & 0xFF) - (from >> 16 & 0xFF)) * f);
        int g = (int) ((from >> 8 & 0xFF) + ((to >> 8 & 0xFF) - (from >> 8 & 0xFF)) * f);
        int b = (int) ((from & 0xFF) + ((to & 0xFF) - (from & 0xFF)) * f);
        return r << 16 | g << 8 | b;
    }

    public static @NotNull Builder builder(@NotNull Font font) {
        return new Builder(font);
    }

    public static final class Builder {
        private final Font font;
        private final List<Run> runs = new ObjectArrayList<>();
        private final IntArrayList dividers = new IntArrayList();

        private @Nullable MutableComponent pending;
        private int pendingX;
        private int cursor;

        private Builder(Font font) {
            this.font = font;
        }

        public @NotNull Builder text(@NotNull Component text) {
            if (this.pending == null) {
                this.pending = Component.empty();
                this.pendingX = this.cursor;
            }

            this.pending.append(text);
            this.cursor += this.font.width(text);
            return this;
        }

        public @NotNull Builder slot(@NotNull Component text, @NotNull String sample, @NotNull Align align) {
            this.flush();

            int textWidth = this.font.width(text);
            int slotWidth = Math.max(textWidth, this.font.width(sample));
            int x = align == Align.RIGHT ? this.cursor + slotWidth - textWidth : this.cursor;

            this.runs.add(new Run(text, text.getVisualOrderText(), x));
            this.cursor += slotWidth;
            return this;
        }

        public void divider() {
            this.flush();

            this.dividers.add(this.cursor + DIVIDER_GAP / 2);
            this.cursor += DIVIDER_GAP;
        }

        public @NotNull TextLine build() {
            this.flush();
            return new TextLine(this.runs.toArray(new Run[0]), this.dividers.toIntArray(), this.cursor);
        }

        private void flush() {
            if (this.pending == null) return;

            this.runs.add(new Run(this.pending, this.pending.getVisualOrderText(), this.pendingX));
            this.pending = null;
        }
    }
}
