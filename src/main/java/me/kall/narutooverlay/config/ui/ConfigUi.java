package me.kall.narutooverlay.config.ui;

import icyllis.modernui.mc.MuiModApi;
import icyllis.modernui.util.DataSet;
import me.kall.narutooverlay.card.Card;
import me.kall.narutooverlay.config.ui.background.ConfigBackground;
import me.kall.narutooverlay.event.TextRelocation;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.ChatScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraftforge.client.ConfigScreenHandler;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.glfw.GLFW;

public final class ConfigUi {
    private static boolean installed;

    private ConfigUi() {}

    public static void install(FMLJavaModLoadingContext context) {
        if (installed) return;
        installed = true;

        ConfigBackground.installDefault();

        context.registerExtensionPoint(ConfigScreenHandler.ConfigScreenFactory.class,
                () -> new ConfigScreenHandler.ConfigScreenFactory(
                        (minecraft, previous) -> create(previous, null)));

        MinecraftForge.EVENT_BUS.addListener(ConfigUi::onMouseButton);
    }

    public static void migrateBundledDefaults() {
        ConfigBackground.migrateBundledDefaults();
    }

    public static @NotNull Screen create(@Nullable Screen previous, @Nullable String cardId) {
        return create(previous, cardId, 0);
    }

    public static @NotNull Screen create(@Nullable Screen previous, @Nullable String cardId, int page) {
        DataSet args = new DataSet();
        if (cardId != null) args.putString(ConfigFragment.ARG_CARD, cardId);
        args.putInt(ConfigFragment.ARG_PAGE, page);

        ConfigFragment fragment = new ConfigFragment();
        fragment.setArguments(args);
        return MuiModApi.get().createScreen(fragment, null, previous);
    }

    private static void onMouseButton(InputEvent.MouseButton.@NotNull Pre event) {
        if (event.getButton() != GLFW.GLFW_MOUSE_BUTTON_RIGHT) return;
        if (event.getAction() != GLFW.GLFW_PRESS) return;

        Minecraft minecraft = Minecraft.getInstance();
        if (!(minecraft.screen instanceof ChatScreen)) return;

        Card<?> card = TextRelocation.pick(TextRelocation.mouseX(), TextRelocation.mouseY());
        if (card == null) return;

        event.setCanceled(true);
        minecraft.setScreen(create(minecraft.screen, card.id()));
    }
}
