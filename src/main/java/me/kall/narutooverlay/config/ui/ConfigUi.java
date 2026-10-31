package me.kall.narutooverlay.config.ui;

import me.kall.narutooverlay.card.Card;
import me.kall.narutooverlay.config.ui.background.BundledBackgrounds;
import me.kall.narutooverlay.config.ui.page.ConfigAboutPage;
import me.kall.narutooverlay.config.ui.page.ConfigAppearancePage;
import me.kall.narutooverlay.config.ui.page.ConfigCardsPage;
import me.kall.narutooverlay.config.ui.widget.Accent;
import me.kall.narutooverlay.config.ui.widget.NarutoShimmerWidgetFactory;
import me.kall.narutooverlay.event.TextRelocation;
import me.kall.narutoui.mui.screen.*;
import me.kall.narutoui.mui.shimmer.*;
import me.kall.narutoui.mui.theme.*;
import me.kall.narutoui.mui.widget.*;
import net.minecraft.client.gui.screens.ChatScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.Minecraft;
import net.minecraftforge.client.ConfigScreenHandler;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.glfw.GLFW;

@SuppressWarnings("unused")
public final class ConfigUi {
    static final String GROUP = "narutooverlay.config.background.";
    static final String BASE_NAME = "narutooverlay-config-bg";

    private static boolean installed;

    private ConfigUi() {}

    public static void install(FMLJavaModLoadingContext context) {
        if (installed) return;
        installed = true;

        BundledBackgrounds.install();

        context.registerExtensionPoint(ConfigScreenHandler.ConfigScreenFactory.class, () -> new ConfigScreenHandler.ConfigScreenFactory((minecraft, previous) -> create(previous)));

        MinecraftForge.EVENT_BUS.addListener(ConfigUi::onMouseButton);
    }

    public static void migrateBundledDefaults() {
        BundledBackgrounds.migrateLegacyDefault();
    }

    public static @NotNull Screen create(@Nullable Screen previous) {
        ConfigScreenHost host = new ConfigScreenHost();

        return MuiSettingsScreen.builder("NarutoOverlay")
                .widgetFactory(NarutoShimmerWidgetFactory.INSTANCE)
                .shimmerProfile(MuiShimmerProfile.create(new NarutoShimmerConfig()))
                .background(host.background())
                .initialPage(0)
                .railHeader(context -> MuiLogo.create(context, MuiTheme.modern(context), "N").color(Accent.primary()).textColor(Accent.onPrimary()))
                .railFooter(context -> MuiHintBadge.create(context, MuiTheme.modern(context), "narutooverlay.config.footer.escHint").color(Accent.primary()))
                .onCreated(host::attach)
                .onDestroyed(fragment -> host.attach(null))
                .page("narutooverlay.config.nav.cards", "narutooverlay.config.cards.title", "narutooverlay.config.cards.subtitle", cardsPage -> ConfigCardsPage.populate(cardsPage, host))
                .page("narutooverlay.config.nav.appearance", "narutooverlay.config.appearance.title", "narutooverlay.config.appearance.subtitle", form -> ConfigAppearancePage.populate(form, host))
                .page("narutooverlay.config.nav.about", "narutooverlay.config.about.title", "narutooverlay.config.about.subtitle", aboutPage -> ConfigAboutPage.populate(aboutPage, host.background()))
                .create(previous);
    }

    private static void onMouseButton(InputEvent.MouseButton.@NotNull Pre event) {
        if (event.getButton() != GLFW.GLFW_MOUSE_BUTTON_RIGHT) return;
        if (event.getAction() != GLFW.GLFW_PRESS) return;

        Minecraft minecraft = Minecraft.getInstance();
        if (!(minecraft.screen instanceof ChatScreen)) return;

        Card<?> card = TextRelocation.pick(TextRelocation.mouseX(), TextRelocation.mouseY());
        if (card == null) return;

        event.setCanceled(true);
        minecraft.setScreen(create(minecraft.screen));
    }
}
