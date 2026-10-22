package me.kall.narutooverlay.event;

import com.mojang.blaze3d.platform.Window;
import me.kall.narutooverlay.card.Card;
import me.kall.narutooverlay.card.CardRegistry;
import me.kall.narutooverlay.config.ConfigSaver;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.ChatScreen;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.event.TickEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.glfw.GLFW;

import java.util.List;

public class TextRelocation {
    private static @Nullable Card<?> selected;

    private static double deltaX;
    private static double deltaY;

    public static void click(InputEvent.MouseButton.@NotNull Pre event) {
        int button = event.getButton();
        int action = event.getAction();

        if (button != GLFW.GLFW_MOUSE_BUTTON_LEFT) return;
        if (action != GLFW.GLFW_PRESS) return;
        if (!(Minecraft.getInstance().screen instanceof ChatScreen)) return;

        double mouseX = mouseX();
        double mouseY = mouseY();

        Card<?> card = pick(mouseX, mouseY);
        if (card != null) {
            selected = card;
            deltaX = mouseX - card.left();
            deltaY = mouseY - card.top();
        }
    }

    public static @Nullable Card<?> pick(double mouseX, double mouseY) {
        List<Card<?>> cards = CardRegistry.all();
        for (int i = cards.size() - 1; i >= 0; i--) {
            Card<?> card = cards.get(i);
            if (card.contains(mouseX, mouseY)) return card;
        }
        return null;
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
        if (!event.phase.equals(TickEvent.Phase.START) || selected == null) return;

        selected.moveTo(mouseX() - deltaX, mouseY() - deltaY);
    }

    public static void release(InputEvent.MouseButton.@NotNull Pre event) {
        int button = event.getButton();
        int action = event.getAction();

        if (button != GLFW.GLFW_MOUSE_BUTTON_LEFT) return;
        if (action != GLFW.GLFW_RELEASE) return;

        if (selected != null) {
            ConfigSaver.changed();
            selected = null;
        }
    }
}
