package me.kall.narutooverlay.backplane;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraftforge.fml.ModList;

public class BackplaneRenderer {
    private static final AbstractBackplane BACKPLANE = ModList.get().isLoaded("modernui") ? new MuiBackplane() : new DefaultBackplane();

    public static void render(GuiGraphics graphics, int left, int top, int width, int height, int[] dividers, int borderColor) {
        BACKPLANE.render(graphics, left, top, width, height, dividers, borderColor);
    }

    public static void renderGroup(GuiGraphics graphics, int left, int top, int width, int height, int[] separators, int borderColor) {
        BACKPLANE.renderGroup(graphics, left, top, width, height, separators, borderColor);
    }
}
