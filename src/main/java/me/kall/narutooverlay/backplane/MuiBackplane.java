package me.kall.narutooverlay.backplane;

import icyllis.modernui.mc.ExtendedGuiGraphics;
import me.kall.narutooverlay.config.RenderArgs;
import me.kall.narutooverlay.theme.Palette;
import net.minecraft.client.gui.GuiGraphics;
import org.jetbrains.annotations.NotNull;

public class MuiBackplane extends AbstractBackplane {
    private static final float CORNER_RADIUS = 4.0F;
    private static final float ACCENT_RADIUS = 1.0F;
    private static final float ACCENT_INSET = 2.0F;
    private static final float MIN_DIVIDER_WIDTH = 0.5F;

    @Override
    protected void draw(@NotNull GuiGraphics graphics, @NotNull Card card, @NotNull Palette palette, int @NotNull [] dividers) {
        ExtendedGuiGraphics extended = new ExtendedGuiGraphics(graphics);

        float cardLeft = card.left();
        float cardTop = card.top();
        float cardRight = card.right();
        float cardBottom = card.bottom();

        extended.setColor(palette.backplane());
        extended.fillRoundRect(cardLeft, cardTop, cardRight, cardBottom, CORNER_RADIUS);

        if (card.accent()) {
            extended.setGradient(ExtendedGuiGraphics.Orientation.TOP_BOTTOM, palette.accent() | 0xFF000000, RenderArgs.borderColor() | 0xFF000000);
            extended.fillRoundRect(card.accentLeft(), cardTop + ACCENT_INSET, card.accentRight(), cardBottom - ACCENT_INSET, ACCENT_RADIUS);
        }

        float borderWidth = RenderArgs.BACKPLANE_BORDER_WIDTH.get().floatValue();
        if (borderWidth > 0) {
            float halfBorder = borderWidth / 2;
            extended.setColor(RenderArgs.borderColor());
            extended.setStrokeWidth(borderWidth);
            extended.strokeRoundRect(cardLeft - halfBorder, cardTop - halfBorder, cardRight + halfBorder, cardBottom + halfBorder, CORNER_RADIUS + halfBorder);
        }

        if (dividers.length == 0) return;

        float dividerWidth = Math.max(borderWidth, MIN_DIVIDER_WIDTH);
        float halfDivider = dividerWidth / 2;

        extended.setColor(RenderArgs.borderColor());
        for (int offset : dividers) {
            float center = card.dividerColumn(offset) + 0.5F;
            extended.fillRoundRect(center - halfDivider, cardTop, center + halfDivider, cardBottom, halfDivider);
        }
    }
}
