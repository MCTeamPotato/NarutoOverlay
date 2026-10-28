package me.kall.narutooverlay.config.ui;

import me.kall.narutoui.mui.MuiLocalization;

import icyllis.modernui.core.Context;
import icyllis.modernui.view.Gravity;
import icyllis.modernui.view.View;
import icyllis.modernui.widget.FrameLayout;
import icyllis.modernui.widget.TextView;
import me.kall.narutoui.mui.MuiControls;
import me.kall.narutoui.mui.MuiSettingsScreen;
import me.kall.narutoui.mui.MuiTheme;
import me.kall.narutoui.mui.shimmer.MuiShimmerProfile;
import me.kall.narutoui.mui.shimmer.ShimmerCircleDrawable;
import me.kall.narutoui.mui.shimmer.ShimmerHintDrawable;
import me.kall.narutooverlay.card.Card;
import me.kall.narutooverlay.config.ui.background.ConfigBackground;
import me.kall.narutooverlay.config.ui.page.ConfigAboutPage;
import me.kall.narutooverlay.config.ui.page.ConfigAppearancePage;
import me.kall.narutooverlay.config.ui.page.ConfigCardsPage;
import me.kall.narutooverlay.config.ui.widget.Accent;
import me.kall.narutooverlay.config.ui.widget.NarutoShimmerWidgetFactory;
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

@SuppressWarnings("unused")
public final class ConfigUi {
    private static boolean installed;

    private ConfigUi() {}

    public static void install(FMLJavaModLoadingContext context) {
        if (installed) return;
        installed = true;

        ConfigBackground.installDefault();

        context.registerExtensionPoint(ConfigScreenHandler.ConfigScreenFactory.class, () -> new ConfigScreenHandler.ConfigScreenFactory((minecraft, previous) -> create(previous)));

        MinecraftForge.EVENT_BUS.addListener(ConfigUi::onMouseButton);
    }

    public static void migrateBundledDefaults() {
        ConfigBackground.migrateBundledDefaults();
    }

    public static @NotNull Screen create(@Nullable Screen previous) {
        return create(previous, 0);
    }

    public static @NotNull Screen create(@Nullable Screen previous, int page) {
        ConfigMuiHost host = new ConfigMuiHost();
        int initialPage = Math.max(0, Math.min(2, page));

        MuiSettingsScreen.Builder builder = MuiSettingsScreen.builder("NarutoOverlay")
                .widgetFactory(new NarutoShimmerWidgetFactory())
                .shimmerProfile(MuiShimmerProfile.create(new NarutoShimmerConfig()))
                .background(host.background())
                .initialPage(initialPage)
                .railHeader(context -> logo(context, host))
                .railFooter(context -> hint(context, host))
                .onCreated(fragment -> {
                    host.attach(fragment);
                    ConfigUiHost.activate(host);
                })
                .onDestroyed(fragment -> {
                    host.attach(null);
                    if (ConfigUiHost.active() == host) ConfigUiHost.activate(null);
                })
                .page(MuiLocalization.get("narutooverlay.config.nav.cards"),
                        MuiLocalization.get("narutooverlay.config.cards.title"),
                        MuiLocalization.get("narutooverlay.config.cards.subtitle"),
                        ConfigCardsPage::populate)
                .page(MuiLocalization.get("narutooverlay.config.nav.appearance"),
                        MuiLocalization.get("narutooverlay.config.appearance.title"),
                        MuiLocalization.get("narutooverlay.config.appearance.subtitle"),
                        form -> ConfigAppearancePage.populate(form, host))
                .page(MuiLocalization.get("narutooverlay.config.nav.about"),
                        MuiLocalization.get("narutooverlay.config.about.title"),
                        MuiLocalization.get("narutooverlay.config.about.subtitle"),
                        aboutPage -> ConfigAboutPage.populate(aboutPage, host));

        return builder.create(previous);
    }

    private static @NotNull View logo(@NotNull Context context, @NotNull ConfigMuiHost host) {
        FrameLayout wrap = new FrameLayout(context);
        MuiTheme theme = MuiTheme.modern(context);
        TextView logo = MuiControls.heading(context, theme, "N", 16, Accent.onPrimary());
        logo.setGravity(Gravity.CENTER);

        ShimmerCircleDrawable circle = new ShimmerCircleDrawable(logo, Accent.primary());
        circle.setCornerRadius(MuiControls.dp(context, 18));
        logo.setBackground(circle);
        host.addChromeShimmer(circle);

        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(MuiControls.dp(context, 36), MuiControls.dp(context, 36), Gravity.CENTER_HORIZONTAL);
        wrap.addView(logo, params);
        return wrap;
    }

    private static @NotNull View hint(@NotNull Context context, @NotNull ConfigMuiHost host) {
        MuiTheme theme = MuiTheme.modern(context);
        TextView hint = MuiControls.text(context, theme, MuiLocalization.get("narutooverlay.config.footer.escHint"), 9, theme.onSurfaceVariant());
        hint.setGravity(Gravity.CENTER);
        hint.setPadding(MuiControls.dp(context, 4), MuiControls.dp(context, 4), MuiControls.dp(context, 4), MuiControls.dp(context, 4));

        ShimmerHintDrawable box = new ShimmerHintDrawable(hint, Accent.primary());
        box.setCornerRadius(MuiControls.dp(context, 8));
        hint.setBackground(box);
        host.addChromeShimmer(box);
        return hint;
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
