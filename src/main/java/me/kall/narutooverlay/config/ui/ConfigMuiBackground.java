package me.kall.narutooverlay.config.ui;

import me.kall.narutoui.mui.MuiLocalization;

import icyllis.modernui.core.Context;
import icyllis.modernui.graphics.Bitmap;
import icyllis.modernui.graphics.Image;
import icyllis.modernui.graphics.drawable.ColorDrawable;
import icyllis.modernui.graphics.drawable.Drawable;
import icyllis.modernui.mc.MuiModApi;
import icyllis.modernui.widget.FrameLayout;
import icyllis.modernui.widget.Toast;
import me.kall.narutoui.mui.MuiBackground;
import me.kall.narutoui.mui.MuiControls;
import me.kall.narutoui.mui.MuiFilePicker;
import me.kall.narutoui.mui.MuiTheme;
import me.kall.narutoui.mui.background.MuiImageDrawable;
import me.kall.narutoui.mui.background.MuiSlideshowBackground;
import me.kall.narutoui.mui.shimmer.MuiShimmerProfile;
import me.kall.narutoui.mui.widget.MuiCropOverlay;
import me.kall.narutooverlay.config.ConfigSaver;
import me.kall.narutooverlay.config.RenderArgs;
import me.kall.narutooverlay.config.ui.background.ConfigBackground;
import me.kall.narutooverlay.config.ui.widget.Accent;
import me.kall.narutooverlay.config.ui.widget.NarutoShimmerWidgetFactory;
import net.minecraft.client.Minecraft;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * Background provider for the Modern UI configuration shell. It keeps the existing slideshow,
 * crop, folder, interval, fade, and dim behaviour
 */
public final class ConfigMuiBackground implements MuiBackground, BackgroundHost {
    private static final Logger LOGGER = LogManager.getLogger("NarutoOverlay");

    private final ConfigUiHost host;
    private @Nullable FrameLayout root;
    private @Nullable MuiSlideshowBackground slideshow;

    public ConfigMuiBackground(@NotNull ConfigUiHost host) {
        this.host = host;
    }

    @Override
    public @NotNull Drawable create(@NotNull Context context) {
        return new ColorDrawable(0xC00A0B0F);
    }

    @Override
    public void attach(@NotNull FrameLayout root) {
        this.root = root;
        this.refresh();
    }

    @Override
    public void detach() {
        this.stopSlideshow();
        this.root = null;
    }

    @Override
    public void chooseBackgroundImage() {
        MuiFilePicker.openImage(
                MuiLocalization.get("narutooverlay.config.background.picker"),
                MuiLocalization.get("narutooverlay.config.background.filter"),
                new MuiFilePicker.Callback() {
                    @Override
                    public void onPicked(@NotNull Path path) {
                        ConfigMuiBackground.this.onImagePicked(path);
                    }

                    @Override
                    public void onFailed() {
                        ConfigMuiBackground.this.toast(MuiLocalization.get("narutooverlay.config.background.pickerFailed"));
                    }
                });
    }

    @Override
    public void chooseBackgroundFolder() {
        MuiFilePicker.openFolder(ConfigBackground.folder(),
                MuiLocalization.get("narutooverlay.config.background.folder.picker"),
                new MuiFilePicker.Callback() {
                    @Override
                    public void onPicked(@NotNull Path path) {
                        RenderArgs.BACKGROUND_FOLDER.set(ConfigBackground.store(path));
                        ConfigSaver.changed();
                        ConfigMuiBackground.this.refresh();
                        ConfigMuiBackground.this.host.reloadCurrentPage();
                    }

                    @Override
                    public void onFailed() {
                        ConfigMuiBackground.this.toast(MuiLocalization.get("narutooverlay.config.background.folder.failed"));
                    }
                });
    }

    @Override
    public void clearBackgroundFolder() {
        RenderArgs.BACKGROUND_FOLDER.set("");
        ConfigSaver.changed();
        this.refresh();
        this.host.reloadCurrentPage();
    }

    @Override
    public void clearBackgroundImage() {
        RenderArgs.BACKGROUND_IMAGE.set("");
        RenderArgs.BACKGROUND_CROP_X.set(0.0D);
        RenderArgs.BACKGROUND_CROP_Y.set(0.0D);
        RenderArgs.BACKGROUND_CROP_WIDTH.set(1.0D);
        RenderArgs.BACKGROUND_CROP_HEIGHT.set(1.0D);
        ConfigSaver.changed();
        ConfigBackground.invalidate();
        this.refresh();
        this.host.reloadCurrentPage();
    }

    @Override
    public void setBackgroundInterval(double seconds) {
        RenderArgs.BACKGROUND_INTERVAL.set(seconds);
        ConfigSaver.changed();
        if (this.slideshow != null) this.slideshow.setInterval(seconds);
    }

    @Override
    public void setBackgroundFade(double seconds) {
        RenderArgs.BACKGROUND_FADE.set(seconds);
        ConfigSaver.changed();
        if (this.slideshow != null) this.slideshow.setFade(seconds);
    }

    @Override
    public void setBackgroundDim(double dim) {
        RenderArgs.BACKGROUND_DIM.set(dim);
        ConfigSaver.changed();
        if (this.slideshow != null) {
            this.slideshow.setDim(dim);
        } else {
            this.refresh();
        }
    }

    private void refresh() {
        FrameLayout root = this.root;
        if (root == null) return;

        this.stopSlideshow();

        List<Path> files = ConfigBackground.images();
        if (!files.isEmpty()) {
            MuiSlideshowBackground show = new MuiSlideshowBackground(files,
                    RenderArgs.BACKGROUND_INTERVAL.get(),
                    RenderArgs.BACKGROUND_FADE.get(),
                    RenderArgs.BACKGROUND_DIM.get());
            this.slideshow = show;
            show.attach(root);
            return;
        }

        if (!RenderArgs.BACKGROUND_FOLDER.get().isBlank()) {
            root.setBackground(new ColorDrawable(0xC00A0B0F));
            return;
        }

        Image image = ConfigBackground.image();
        if (image != null) {
            root.setBackground(new MuiImageDrawable(image,
                    RenderArgs.BACKGROUND_CROP_X.get(),
                    RenderArgs.BACKGROUND_CROP_Y.get(),
                    RenderArgs.BACKGROUND_CROP_WIDTH.get(),
                    RenderArgs.BACKGROUND_CROP_HEIGHT.get(),
                    RenderArgs.BACKGROUND_DIM.get()));
        } else {
            root.setBackground(new ColorDrawable(0xC00A0B0F));
            ConfigBackground.loadAsync(this::refresh);
        }
    }

    private void stopSlideshow() {
        if (this.slideshow == null) return;
        this.slideshow.detach();
        this.slideshow = null;
    }

    private void onImagePicked(@NotNull Path path) {
        ConfigBackground.decode(path).whenComplete((bitmap, error) -> MuiModApi.postToUiThread(() -> {
            if (bitmap == null) {
                this.toast(MuiLocalization.get("narutooverlay.config.background.failed"));
                return;
            }
            this.startCrop(path, bitmap);
        }));
    }

    private void startCrop(@NotNull Path path, @NotNull Bitmap bitmap) {
        Context context = this.host.requireContext();
        int imageWidth = bitmap.getWidth();
        int imageHeight = bitmap.getHeight();

        Minecraft minecraft = Minecraft.getInstance();
        float target = (float) minecraft.getWindow().getWidth() / (float) minecraft.getWindow().getHeight();
        float actual = (float) imageWidth / (float) imageHeight;
        if (Math.abs(actual - target) <= 0.01F) {
            bitmap.close();
            this.saveBackground(path, imageWidth, imageHeight, 0, 0, imageWidth, imageHeight);
            return;
        }

        Image preview = Image.createTextureFromBitmap(bitmap);
        bitmap.close();
        if (preview == null) return;

        MuiCropOverlay[] holder = new MuiCropOverlay[1];
        MuiCropOverlay.Listener listener = new MuiCropOverlay.Listener() {
            @Override
            public void onCrop(int x, int y, int width, int height) {
                ConfigMuiBackground.this.host.removeOverlay(holder[0]);
                preview.close();
                ConfigMuiBackground.this.saveBackground(path, imageWidth, imageHeight, x, y, width, height);
            }

            @Override
            public void onCancel() {
                ConfigMuiBackground.this.host.removeOverlay(holder[0]);
                preview.close();
            }
        };

        MuiShimmerProfile.withCurrent(NarutoShimmerConfig.profile(), () ->
                MuiControls.withFactory(new NarutoShimmerWidgetFactory(), () -> {
                    holder[0] = new MuiCropOverlay(context,
                            MuiTheme.modern(context).withPrimary(Accent.primary()),
                            preview, imageWidth, imageHeight, target,
                            new MuiCropOverlay.Labels(
                                    MuiLocalization.get("narutooverlay.config.background.crop.title"),
                                    MuiLocalization.get("narutooverlay.config.background.crop.hint"),
                                    MuiLocalization.get("narutooverlay.config.background.confirm"),
                                    MuiLocalization.get("narutooverlay.config.background.cancel")),
                            listener);
                    this.host.showOverlay(holder[0]);
                }));
    }

    private void saveBackground(@NotNull Path path, int imageWidth, int imageHeight,
                                int x, int y, int width, int height) {
        CompletableFuture.supplyAsync(() -> {
            try {
                return ConfigBackground.copyInto(path);
            } catch (Throwable error) {
                LOGGER.error("Could not store the background image", error);
                return null;
            }
        }).whenComplete((name, error) -> MuiModApi.postToUiThread(() -> {
            if (name == null) {
                this.toast(MuiLocalization.get("narutooverlay.config.background.failed"));
                return;
            }

            double cropX = clamp01((double) x / Math.max(1, imageWidth));
            double cropY = clamp01((double) y / Math.max(1, imageHeight));
            double cropWidth = Math.max(0.01D, Math.min(1.0D - cropX,
                    (double) width / Math.max(1, imageWidth)));
            double cropHeight = Math.max(0.01D, Math.min(1.0D - cropY,
                    (double) height / Math.max(1, imageHeight)));

            RenderArgs.BACKGROUND_IMAGE.set(name);
            RenderArgs.BACKGROUND_CROP_X.set(cropX);
            RenderArgs.BACKGROUND_CROP_Y.set(cropY);
            RenderArgs.BACKGROUND_CROP_WIDTH.set(cropWidth);
            RenderArgs.BACKGROUND_CROP_HEIGHT.set(cropHeight);
            ConfigSaver.changed();
            ConfigBackground.invalidate();
            this.refresh();
            this.host.reloadCurrentPage();
            this.toast(MuiLocalization.get("narutooverlay.config.background.saved"));
        }));
    }

    private void toast(@NotNull CharSequence message) {
        Toast.makeText(this.host.requireContext(), message, Toast.LENGTH_SHORT).show();
    }

    private static double clamp01(double value) {
        return Math.max(0.0D, Math.min(1.0D, value));
    }
}