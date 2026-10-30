package me.kall.narutooverlay.event;

import com.mojang.blaze3d.platform.Window;
import me.kall.narutooverlay.card.Card;
import me.kall.narutooverlay.card.CardRegistry;
import me.kall.narutooverlay.config.ConfigSaver;
import net.minecraft.client.gui.screens.ChatScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.Minecraft;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.event.TickEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.glfw.GLFW;

public class TextRelocation {
    private static @Nullable Card<?> dragging;

    private static double deltaX;
    private static double deltaY;

    public static void click(InputEvent.MouseButton.@NotNull Pre event) {
        if (!isLeftMouse(event, GLFW.GLFW_PRESS)) return;
        if (!(Minecraft.getInstance().screen instanceof ChatScreen)) return;

        double mouseX = mouseX();
        double mouseY = mouseY();

        Card<?> card = pick(mouseX, mouseY);

        if (card == null) {
            if (!Screen.hasShiftDown() && !Screen.hasControlDown()) CardRegistry.select(null);
            return;
        }

        if (Screen.hasControlDown()) {
            if (CardRegistry.split(card)) ConfigSaver.changed();
            CardRegistry.select(null);
            dragging = null;
            event.setCanceled(true);
            return;
        }

        if (Screen.hasShiftDown()) {
            CardRegistry.select(CardRegistry.isSelected(card) ? null : card);
            dragging = null;
            event.setCanceled(true);
            return;
        }

        Card<?> mergeSource = CardRegistry.selected();
        if (mergeSource != null) {
            CardRegistry.select(null);
            dragging = null;

            if (mergeSource != card && CardRegistry.merge(mergeSource, card)) {
                ConfigSaver.changed();
            }

            event.setCanceled(true);
            return;
        }

        dragging = card;
        deltaX = mouseX - card.left();
        deltaY = mouseY - card.top();
    }

    public static @Nullable Card<?> pick(double mouseX, double mouseY) {
        return CardRegistry.pick(mouseX, mouseY);
    }

    public static double mouseX() {
        Minecraft minecraft = Minecraft.getInstance();
        Window window = minecraft.getWindow();
        return minecraft.mouseHandler.xpos() * window.getGuiScaledWidth() / window.getScreenWidth();
    }

    public static double mouseY() {
        Minecraft minecraft = Minecraft.getInstance();
        Window window = minecraft.getWindow();
        return minecraft.mouseHandler.ypos() * window.getGuiScaledHeight() / window.getScreenHeight();
    }

    public static void drag(TickEvent.@NotNull ClientTickEvent event) {
        if (!event.phase.equals(TickEvent.Phase.START) || dragging == null) return;

        Card<?> moving = dragging;
        int oldLeft = moving.left();
        int oldTop = moving.top();

        moving.moveTo(mouseX() - deltaX, mouseY() - deltaY);

        int dx = moving.left() - oldLeft;
        int dy = moving.top() - oldTop;
        if (dx == 0 && dy == 0) return;

        for (Card<?> member : CardRegistry.groupMembers(moving)) {
            if (member != moving) member.moveTo(member.left() + dx, member.top() + dy);
        }
    }

    public static void release(InputEvent.MouseButton.@NotNull Pre event) {
        if (isLeftMouse(event, GLFW.GLFW_RELEASE)) {
            if (dragging != null) {
                ConfigSaver.changed();
                dragging = null;
            }
        }
    }

    private static boolean isLeftMouse(InputEvent.MouseButton.@NotNull Pre event, int action) {
        return event.getButton() == GLFW.GLFW_MOUSE_BUTTON_LEFT && event.getAction() == action;
    }
}
