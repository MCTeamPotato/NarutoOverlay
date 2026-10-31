package me.kall.narutooverlay.config.ui;

import icyllis.modernui.core.Context;
import icyllis.modernui.view.View;
import me.kall.narutooverlay.config.ConfigSaver;
import me.kall.narutooverlay.config.RenderArgs;
import me.kall.narutooverlay.config.ui.widget.Accent;
import me.kall.narutooverlay.config.ui.widget.NarutoShimmerWidgetFactory;
import me.kall.narutoui.mui.background.*;
import me.kall.narutoui.mui.overlay.MuiCropOverlay;
import me.kall.narutoui.mui.screen.MuiConfigFragment;
import me.kall.narutoui.mui.screen.MuiScreenHost;
import me.kall.narutoui.mui.shimmer.MuiShimmerProfile;
import me.kall.narutoui.mui.theme.MuiTheme;
import me.kall.narutoui.mui.widget.MuiControls;
import net.minecraftforge.fml.loading.FMLPaths;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

/**
 * Wires the configuration pages to the Modern UI shell.
 * <p>
 * This class is only a composition root: it tracks the attached fragment and owns the configurable
 * background. Screen services are implemented against the currently attached
 * {@link MuiConfigFragment}.
 */
final class ConfigScreenHost implements MuiScreenHost {
    private final MuiConfigurableBackground background = new MuiConfigurableBackground(new MuiBackgroundSettings() {
        @Override
        public @NotNull MuiBackgroundState load() {
            List<String> order = new ArrayList<>();
            for (String name : RenderArgs.BACKGROUND_ORDER.get()) {
                if (name != null) order.add(name);
            }

            return new MuiBackgroundState(RenderArgs.BACKGROUND_IMAGE.get(), RenderArgs.BACKGROUND_FOLDER.get(), order, RenderArgs.BACKGROUND_INTERVAL.get(), RenderArgs.BACKGROUND_FADE.get(), RenderArgs.BACKGROUND_DIM.get(), new MuiCrop(RenderArgs.BACKGROUND_CROP_X.get(), RenderArgs.BACKGROUND_CROP_Y.get(), RenderArgs.BACKGROUND_CROP_WIDTH.get(), RenderArgs.BACKGROUND_CROP_HEIGHT.get()));
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
    }, new MuiBackgroundLabels(ConfigUi.GROUP + "picker", ConfigUi.GROUP + "filter", ConfigUi.GROUP + "pickerFailed", ConfigUi.GROUP + "folder.picker", ConfigUi.GROUP + "folder.failed", ConfigUi.GROUP + "failed", ConfigUi.GROUP + "saved", new MuiCropOverlay.Labels(ConfigUi.GROUP + "crop.title", ConfigUi.GROUP + "crop.hint", ConfigUi.GROUP + "confirm", ConfigUi.GROUP + "cancel")), new MuiBackgroundFiles(FMLPaths.GAMEDIR.get(), ConfigUi.BASE_NAME), (context, image, width, height, target, cropLabels, listener) -> MuiShimmerProfile.withCurrent(NarutoShimmerConfig.profile(), () -> MuiControls.withFactory(NarutoShimmerWidgetFactory.INSTANCE, () -> new MuiCropOverlay(context, MuiTheme.modern(context).withPrimary(Accent.primary()), image, width, height, target, cropLabels, listener))));

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

    @Override
    public void onThemeChanged() {
        this.reloadCurrentPage();
        ConfigSaver.changed();
    }
}
