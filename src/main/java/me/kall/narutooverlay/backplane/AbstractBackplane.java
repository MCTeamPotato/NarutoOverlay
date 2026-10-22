package me.kall.narutooverlay.backplane;

import me.kall.narutooverlay.config.RenderArgs;
import me.kall.narutooverlay.theme.Palette;
import net.minecraft.client.gui.GuiGraphics;
import org.jetbrains.annotations.NotNull;

public abstract class AbstractBackplane {
    public static final int PADDING_X = 4;
    public static final int PADDING_RIGHT = 3;
    public static final int PADDING_TOP = 3;
    public static final int PADDING_BOTTOM = 1;
    public static final int ACCENT_WIDTH = 2;

    public final void render(@NotNull GuiGraphics graphics, int left, int top, int width, int height, int @NotNull [] dividers) {
        Palette palette = RenderArgs.palette();
        boolean accent = palette.hasAccent();

        int accentExtra = accent ? ACCENT_WIDTH + 1 : 0;

        Card card = new Card(left - PADDING_X - accentExtra, top - PADDING_TOP, left + width + PADDING_RIGHT, top + height + PADDING_BOTTOM, accent, left);

        this.draw(graphics, card, palette, dividers);
    }

    protected abstract void draw(@NotNull GuiGraphics graphics, @NotNull Card card, @NotNull Palette palette, int @NotNull [] dividers);

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
