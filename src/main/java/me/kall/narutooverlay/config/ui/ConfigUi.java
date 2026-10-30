package me.kall.narutooverlay.config.ui;

import icyllis.modernui.core.Context;
import icyllis.modernui.view.View;
import me.kall.narutooverlay.card.Card;
import me.kall.narutooverlay.config.ConfigSaver;
import me.kall.narutooverlay.config.RenderArgs;
import me.kall.narutooverlay.config.ui.background.BundledBackgrounds;
import me.kall.narutooverlay.config.ui.page.ConfigAboutPage;
import me.kall.narutooverlay.config.ui.page.ConfigAppearancePage;
import me.kall.narutooverlay.config.ui.page.ConfigCardsPage;
import me.kall.narutooverlay.config.ui.widget.Accent;
import me.kall.narutooverlay.config.ui.widget.NarutoShimmerWidgetFactory;
import me.kall.narutooverlay.event.TextRelocation;
import me.kall.narutoui.mui.background.*;
import me.kall.narutoui.mui.overlay.*;
import me.kall.narutoui.mui.screen.*;
import me.kall.narutoui.mui.shimmer.*;
import me.kall.narutoui.mui.theme.*;
import me.kall.narutoui.mui.util.*;
import me.kall.narutoui.mui.widget.*;
import net.minecraft.client.gui.screens.ChatScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.Minecraft;
import net.minecraftforge.client.ConfigScreenHandler;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.loading.FMLPaths;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("unused")
public final class ConfigUi {
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
        return create(previous, 0);
    }

    public static @NotNull Screen create(@Nullable Screen previous, int page) {
        ConfigScreenHost host = new ConfigScreenHost();
        int initialPage = Math.max(0, Math.min(2, page));

        MuiSettingsScreen.Builder builder = MuiSettingsScreen.builder("NarutoOverlay")
                .widgetFactory(new NarutoShimmerWidgetFactory())
                .shimmerProfile(MuiShimmerProfile.create(new NarutoShimmerConfig()))
                .background(host.background())
                .initialPage(initialPage)
                .railHeader(ConfigUi::logo)
                .railFooter(ConfigUi::hint)
                .onCreated(host::attach)
                .onDestroyed(fragment -> host.attach(null))
                .page(MuiLocalization.get("narutooverlay.config.nav.cards"), MuiLocalization.get("narutooverlay.config.cards.title"), MuiLocalization.get("narutooverlay.config.cards.subtitle"), cardsPage -> ConfigCardsPage.populate(cardsPage, host))
                .page(MuiLocalization.get("narutooverlay.config.nav.appearance"), MuiLocalization.get("narutooverlay.config.appearance.title"), MuiLocalization.get("narutooverlay.config.appearance.subtitle"), form -> ConfigAppearancePage.populate(form, host))
                .page(MuiLocalization.get("narutooverlay.config.nav.about"), MuiLocalization.get("narutooverlay.config.about.title"), MuiLocalization.get("narutooverlay.config.about.subtitle"), aboutPage -> ConfigAboutPage.populate(aboutPage, host.background()));

        return builder.create(previous);
    }

    private static @NotNull View logo(@NotNull Context context) {
        return MuiLogo.create(context, MuiTheme.modern(context), "N").color(Accent.primary()).textColor(Accent.onPrimary());
    }

    private static @NotNull View hint(@NotNull Context context) {
        return MuiHintBadge.create(context, MuiTheme.modern(context), MuiLocalization.get("narutooverlay.config.footer.escHint")).color(Accent.primary());
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

    /**
     * Wires the configuration pages to the Modern UI shell.
     * <p>
     * This class is only a composition root: it tracks the attached fragment and owns the configurable
     * background. Screen services are implemented against the currently attached
     * {@link MuiConfigFragment}.
     */
    static final class ConfigScreenHost implements MuiScreenHost {
        private final MuiConfigurableBackground background = ConfigBackgroundFactory.create();
        private @Nullable MuiConfigFragment fragment;

        ConfigScreenHost() {}

        public @NotNull MuiConfigurableBackground background() {
            return this.background;
        }

        public void attach(@Nullable MuiConfigFragment fragment) {
            this.fragment = fragment;
        }

        @Override
        public @NotNull Context requireContext() {
            MuiConfigFragment current = this.fragment;
            if (current == null) throw new IllegalStateException("Config screen is not attached");
            return current.requireContext();
        }

        @Override
        public void showOverlay(@NotNull View overlay) {
            MuiConfigFragment current = this.fragment;
            if (current != null) current.showOverlay(overlay);
        }

        @Override
        public void removeOverlay(@NotNull View overlay) {
            MuiConfigFragment current = this.fragment;
            if (current != null) current.removeOverlay(overlay);
        }

        @Override
        public void reloadCurrentPage() {
            MuiConfigFragment current = this.fragment;
            if (current != null) current.reloadCurrentPage();
        }
    }

    /**
     * Builds NarutoOverlay's instance of the generic configurable background.
     * <p>
     * This is the only place that binds the library controller to this mod's configuration values,
     * localization keys, game directory and shimmer styling.
     */
    static final class ConfigBackgroundFactory {
        private static final String GROUP = "narutooverlay.config.background.";
        private static final String BASE_NAME = "narutooverlay-config-bg";

        private ConfigBackgroundFactory() {}

        static @NotNull MuiConfigurableBackground create() {
            return new MuiConfigurableBackground(new ConfigBackgroundSettings(), labels(), new MuiBackgroundFiles(FMLPaths.GAMEDIR.get(), BASE_NAME), cropFactory());
        }

        private static @NotNull MuiBackgroundLabels labels() {
            return new MuiBackgroundLabels(MuiLocalization.get(GROUP + "picker"), MuiLocalization.get(GROUP + "filter"), MuiLocalization.get(GROUP + "pickerFailed"), MuiLocalization.get(GROUP + "folder.picker"), MuiLocalization.get(GROUP + "folder.failed"), MuiLocalization.get(GROUP + "failed"), MuiLocalization.get(GROUP + "saved"), new MuiCropOverlay.Labels(MuiLocalization.get(GROUP + "crop.title"), MuiLocalization.get(GROUP + "crop.hint"), MuiLocalization.get(GROUP + "crop.confirm"), MuiLocalization.get(GROUP + "crop.cancel")));
        }

        private static @NotNull MuiCropOverlayFactory cropFactory() {
            return (context, image, width, height, target, cropLabels, listener) -> MuiShimmerProfile.withCurrent(NarutoShimmerConfig.profile(), () -> MuiControls.withFactory(new NarutoShimmerWidgetFactory(), () -> new MuiCropOverlay(context, MuiTheme.modern(context).withPrimary(Accent.primary()), image, width, height, target, cropLabels, listener)));
        }

        /**
         * Maps the generic {@link MuiBackgroundState} to NarutoOverlay's Forge configuration.
         */
        static final class ConfigBackgroundSettings implements MuiBackgroundSettings {
            @Override
            public @NotNull MuiBackgroundState load() {
                List<String> order = new ArrayList<>();
                for (String name : RenderArgs.BACKGROUND_ORDER.get()) {
                    if (name != null) order.add(name);
                }

                return new MuiBackgroundState(
                        RenderArgs.BACKGROUND_IMAGE.get(),
                        RenderArgs.BACKGROUND_FOLDER.get(),
                        order,
                        RenderArgs.BACKGROUND_INTERVAL.get(),
                        RenderArgs.BACKGROUND_FADE.get(),
                        RenderArgs.BACKGROUND_DIM.get(),
                        new MuiCrop(RenderArgs.BACKGROUND_CROP_X.get(), RenderArgs.BACKGROUND_CROP_Y.get(), RenderArgs.BACKGROUND_CROP_WIDTH.get(), RenderArgs.BACKGROUND_CROP_HEIGHT.get()));
            }

            @Override
            public void save(@NotNull MuiBackgroundState state) {
                RenderArgs.BACKGROUND_IMAGE.set(state.image());
                RenderArgs.BACKGROUND_FOLDER.set(state.folder());
                RenderArgs.BACKGROUND_ORDER.set(new ArrayList<>(state.order()));
                RenderArgs.BACKGROUND_INTERVAL.set(state.interval());
                RenderArgs.BACKGROUND_FADE.set(state.fade());
                RenderArgs.BACKGROUND_DIM.set(state.dim());

                MuiCrop crop = state.crop();
                RenderArgs.BACKGROUND_CROP_X.set(crop.x());
                RenderArgs.BACKGROUND_CROP_Y.set(crop.y());
                RenderArgs.BACKGROUND_CROP_WIDTH.set(crop.width());
                RenderArgs.BACKGROUND_CROP_HEIGHT.set(crop.height());
                ConfigSaver.changed();
            }
        }
    }
}
