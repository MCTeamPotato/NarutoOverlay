package me.kall.narutooverlay.text;

import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public final class TextLine {
    public static final int DIVIDER_GAP = 7;

    public enum Align {
        LEFT,
        RIGHT
    }

    private record Run(Component text, int x) {}

    private final Run[] runs;
    private final int[] dividers;
    private final int width;

    private TextLine(Run[] runs, int[] dividers, int width) {
        this.runs = runs;
        this.dividers = dividers;
        this.width = width;
    }

    public int width() {
        return this.width;
    }

    public int[] dividers() {
        return this.dividers;
    }

    public void draw(@NotNull GuiGraphics graphics, @NotNull Font font, int left, int top, boolean shadow) {
        for (Run run : this.runs) {
            graphics.drawString(font, run.text(), left + run.x(), top, 0xFFFFFF, shadow);
        }
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

            this.runs.add(new Run(text, x));
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

            this.runs.add(new Run(this.pending, this.pendingX));
            this.pending = null;
        }
    }
}
