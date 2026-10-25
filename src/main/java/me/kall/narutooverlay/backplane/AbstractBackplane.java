package me.kall.narutooverlay.backplane;

import me.kall.narutooverlay.config.RenderArgs;
import me.kall.narutooverlay.theme.Palette;
import net.minecraft.client.gui.GuiGraphics;
import org.jetbrains.annotations.NotNull;

public abstract class AbstractBackplane {
    public static final int PADDING_X = 4;
    public static final int PADDING_RIGHT = 3;
    public static final int PADDING_TOP = 2;
    public static final int PADDING_BOTTOM = 2;
    public static final int ACCENT_WIDTH = 2;

    public final void render(@NotNull GuiGraphics graphics, int left, int top, int width, int height, int @NotNull [] dividers) {
        this.render(graphics, left, top, width, height, dividers, RenderArgs.borderColor());
    }

    public final void render(@NotNull GuiGraphics graphics, int left, int top, int width, int height, int @NotNull [] dividers, int borderColor) {
        Palette palette = RenderArgs.palette();
        boolean accent = palette.hasAccent();

        int accentExtra = accent ? ACCENT_WIDTH + 1 : 0;

        Card card = new Card(left - PADDING_X - accentExtra, top - PADDING_TOP, left + width + PADDING_RIGHT, top + height + PADDING_BOTTOM, accent, left);

        this.draw(graphics, card, palette, dividers, borderColor);
    }

    public final void renderGroup(@NotNull GuiGraphics graphics, int left, int top, int width, int height, int @NotNull [] separators) {
        this.renderGroup(graphics, left, top, width, height, separators, RenderArgs.borderColor());
    }

    public final void renderGroup(@NotNull GuiGraphics graphics, int left, int top, int width, int height, int @NotNull [] separators, int borderColor) {
        Palette palette = RenderArgs.palette();
        boolean accent = palette.hasAccent();

        int accentExtra = accent ? ACCENT_WIDTH + 1 : 0;

        Card card = new Card(left - PADDING_X - accentExtra, top - PADDING_TOP, left + width + PADDING_RIGHT, top + height + PADDING_BOTTOM, accent, left);

        this.draw(graphics, card, palette, new int[0], borderColor);

        int separator = palette.separator() | 0xFF000000;
        int start = top + 1;
        int end = top + height - 1;
        for (int y : separators) {
            if (y <= start || y >= end) continue;
            graphics.fill(left + 1, y, left + width - 1, y + 1, separator);
        }
    }

    protected abstract void draw(@NotNull GuiGraphics graphics, @NotNull Card card, @NotNull Palette palette, int @NotNull [] dividers, int borderColor);

    protected record Card(int left, int top, int right, int bottom, boolean accent, int origin) {
        public int accentLeft() {
            return this.left + 1;
        }

        public int accentRight() {
            return this.left + 1 + ACCENT_WIDTH;
        }

        public int dividerColumn(int offset) {
            return this.origin + offset;
        }
    }
}
