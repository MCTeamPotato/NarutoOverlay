package me.kall.narutooverlay.card;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import me.kall.narutooverlay.text.TextLine;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraftforge.client.event.RenderGuiOverlayEvent;
import net.minecraftforge.client.gui.overlay.VanillaGuiOverlay;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.UnmodifiableView;

import java.util.Collections;
import java.util.List;

public final class CardRegistry {
    private static final List<Card<?>> CARDS = new ObjectArrayList<>(3);

    private CardRegistry() {}

    static void register(@NotNull Card<?> card) {
        CARDS.add(card);
    }

    @Contract(pure = true)
    public static @NotNull @UnmodifiableView List<Card<?>> all() {
        return Collections.unmodifiableList(CARDS);
    }

    public static void render(@NotNull RenderGuiOverlayEvent.Post event) {
        if (event.getOverlay() != VanillaGuiOverlay.HOTBAR.type()) return;

        GuiGraphics graphics = event.getGuiGraphics();
        for (Card<?> card : CARDS) card.render(graphics);
    }

    static int sharedWidth() {
        int width = 0;
        for (Card<?> card : CARDS) {
            TextLine line = card.line();
            if (line != null) width = Math.max(width, line.width());
        }
        return width;
    }

    public static void resetPositions() {
        for (Card<?> card : CARDS) card.resetPosition();
    }

    public static void resetAll() {
        for (Card<?> card : CARDS) card.resetAll();
    }
}
