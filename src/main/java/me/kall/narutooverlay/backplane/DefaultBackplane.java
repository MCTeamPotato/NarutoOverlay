package me.kall.narutooverlay.backplane;

import me.kall.narutooverlay.config.RenderArgs;
import me.kall.narutooverlay.util.Colors;
import me.kall.narutooverlay.theme.Palette;
import net.minecraft.client.gui.GuiGraphics;
import org.jetbrains.annotations.NotNull;

public class DefaultBackplane extends AbstractBackplane {
    private static final int TOP_HIGHLIGHT = 0x14FFFFFF;
    private static final float CORNER_ALPHA = 0.35F;

    @Override
    protected void draw(@NotNull GuiGraphics graphics, @NotNull Card card, @NotNull Palette palette, int @NotNull [] dividers, int borderColor) {
        int left = card.left();
        int top = card.top();
        int right = card.right();
        int bottom = card.bottom();

        int backplane = palette.backplane();

        graphics.fill(left + 1, top, right - 1, bottom, backplane);
        graphics.fill(left, top + 1, left + 1, bottom - 1, backplane);
        graphics.fill(right - 1, top + 1, right, bottom - 1, backplane);

        graphics.fill(left + 1, top + 1, right - 1, top + 2, TOP_HIGHLIGHT);

        if (card.accent()) {
            graphics.fillGradient(card.accentLeft(), top + 1, card.accentRight(), bottom - 1, palette.accent() | 0xFF000000, RenderArgs.borderColor() | 0xFF000000);
        }

        int border = borderColor;
        graphics.fill(left + 1, top, right - 1, top + 1, border);
        graphics.fill(left + 1, bottom - 1, right - 1, bottom, border);
        graphics.fill(left, top + 1, left + 1, bottom - 1, border);
        graphics.fill(right - 1, top + 1, right, bottom - 1, border);

        int corner = Colors.scaleAlpha(border, CORNER_ALPHA);
        graphics.fill(left, top, left + 1, top + 1, corner);
        graphics.fill(right - 1, top, right, top + 1, corner);
        graphics.fill(left, bottom - 1, left + 1, bottom, corner);
        graphics.fill(right - 1, bottom - 1, right, bottom, corner);

        int divider = RenderArgs.borderColor();
        for (int offset : dividers) {
            int x = card.dividerColumn(offset);
            graphics.fill(x, top + 1, x + 1, bottom - 1, divider);
        }
    }
}
