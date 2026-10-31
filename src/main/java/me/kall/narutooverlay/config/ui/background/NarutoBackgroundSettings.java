package me.kall.narutooverlay.config.ui.background;

import icyllis.modernui.core.Context;
import icyllis.modernui.graphics.Image;
import me.kall.narutooverlay.config.ConfigSaver;
import me.kall.narutooverlay.config.RenderArgs;
import me.kall.narutooverlay.config.ui.NarutoShimmerConfig;
import me.kall.narutooverlay.config.ui.widget.Accent;
import me.kall.narutooverlay.config.ui.widget.NarutoShimmerWidgetFactory;
import me.kall.narutoui.mui.background.*;
import me.kall.narutoui.mui.overlay.MuiCropOverlay;
import me.kall.narutoui.mui.shimmer.MuiShimmerProfile;
import me.kall.narutoui.mui.theme.MuiTheme;
import me.kall.narutoui.mui.widget.MuiControls;
import net.minecraftforge.fml.loading.FMLPaths;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public final class NarutoBackgroundSettings implements MuiBackgroundSettings {
    public static final MuiConfigurableBackground BACKGROUND = MuiConfigurableBackground
            .builder(new NarutoBackgroundSettings(), labels(), new MuiBackgroundFiles(FMLPaths.GAMEDIR.get(), "narutooverlay-config-bg"))
            .cropFactory(NarutoBackgroundSettings::cropOverlay)
            .build();

    private static @NotNull MuiBackgroundLabels labels() {
        return new MuiBackgroundLabels(
                "narutooverlay.config.background.picker",
                "narutooverlay.config.background.filter",
                "narutooverlay.config.background.pickerFailed",
                "narutooverlay.config.background.folder.picker",
                "narutooverlay.config.background.folder.failed",
                "narutooverlay.config.background.failed",
                "narutooverlay.config.background.saved",
                new MuiCropOverlay.Labels("narutooverlay.config.background.crop.title", "narutooverlay.config.background.crop.hint", "narutooverlay.config.background.confirm", "narutooverlay.config.background.cancel"));
    }

    private static @NotNull MuiCropOverlay cropOverlay(@NotNull Context context, @NotNull Image image, int imageWidth, int imageHeight, float targetAspect, @NotNull MuiCropOverlay.Labels cropLabels, @NotNull MuiCropOverlay.Listener listener) {
        return MuiShimmerProfile.withCurrent(NarutoShimmerConfig.profile(), () -> MuiControls.withFactory(NarutoShimmerWidgetFactory.INSTANCE, () -> new MuiCropOverlay(context, MuiTheme.modern(context).withPrimary(Accent.primary()), image, imageWidth, imageHeight, targetAspect, cropLabels, listener)));
    }

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
}
