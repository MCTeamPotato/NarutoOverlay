package me.kall.narutooverlay.config.ui.background;

import icyllis.modernui.animation.Animator;
import icyllis.modernui.animation.AnimatorListener;
import icyllis.modernui.animation.MotionEasingUtils;
import icyllis.modernui.animation.ValueAnimator;
import icyllis.modernui.graphics.BitmapFactory;
import icyllis.modernui.graphics.Image;
import icyllis.modernui.mc.MuiModApi;
import icyllis.modernui.view.View;
import me.kall.narutooverlay.config.RenderArgs;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * Plays the pictures of the configured background folder and cross-fades between them.
 */
public final class BackgroundSlideshow {
    private static final Logger LOGGER = LogManager.getLogger("NarutoOverlay");

    private final View host;
    private final List<Path> files;
    private final SlideBackgroundDrawable drawable = new SlideBackgroundDrawable();
    private final Runnable tick = this::advance;

    private int index = -1;
    private @Nullable Image current;
    private @Nullable Image incoming;
    private @Nullable ValueAnimator fade;
    private boolean running;
    private boolean loading;
    private int token;

    public BackgroundSlideshow(@NotNull View host, @NotNull List<Path> files) {
        this.host = host;
        this.files = files;
        this.drawable.setDim(RenderArgs.BACKGROUND_DIM.get());
        host.setBackground(this.drawable);
    }

    public void start() {
        if (this.files.isEmpty()) return;

        this.running = true;
        this.token++;
        load(0, image -> {
            if (!this.running) {
                image.close();
                return;
            }
            this.current = image;
            this.index = 0;
            this.drawable.setImages(null, image);
            this.schedule();
        });
    }

    public void stop() {
        this.running = false;
        this.loading = false;
        this.token++;
        this.host.removeCallbacks(this.tick);

        ValueAnimator animator = this.fade;
        this.fade = null;
        if (animator != null) animator.cancel();

        this.drawable.setImages(null, null);
        closeImages();
    }

    public void setDim(double dim) {
        this.drawable.setDim(dim);
    }

    /**
     * Restarts the countdown, e.g. after the interval setting changed.
     */
    public void reschedule() {
        if (!this.running || this.fade != null || this.loading) return;
        this.host.removeCallbacks(this.tick);
        this.schedule();
    }

    private void schedule() {
        if (!this.running) return;
        long interval = Math.max(1000L, Math.round(RenderArgs.BACKGROUND_INTERVAL.get() * 1000.0D));
        this.host.postDelayed(this.tick, interval);
    }

    private void advance() {
        if (!this.running || this.fade != null || this.loading) return;
        if (this.files.size() < 2) {
            this.schedule();
            return;
        }

        int next = (this.index + 1) % this.files.size();
        load(next, image -> {
            if (!this.running) {
                image.close();
                return;
            }
            this.beginFade(image, next);
        });
    }

    private void beginFade(@NotNull Image image, int nextIndex) {
        Image previous = this.current;
        this.incoming = image;
        this.drawable.setImages(previous, image);
        this.drawable.setProgress(0.0F);

        long duration = Math.max(0L, Math.round(RenderArgs.BACKGROUND_FADE.get() * 1000.0D));
        if (duration <= 0L) {
            finishFade(image, nextIndex);
            return;
        }

        ValueAnimator animator = ValueAnimator.ofFloat(0.0F, 1.0F);
        animator.setDuration(duration);
        animator.setInterpolator(MotionEasingUtils.MOTION_EASING_STANDARD);
        animator.addUpdateListener(animation -> {
            if (this.fade != animator) return;
            this.drawable.setProgress((Float) animation.getAnimatedValue());
        });
        animator.addListener(new AnimatorListener() {
            @Override
            public void onAnimationEnd(@NotNull Animator animation) {
                if (BackgroundSlideshow.this.fade != animator) return;
                BackgroundSlideshow.this.fade = null;
                finishFade(image, nextIndex);
            }
        });
        this.fade = animator;
        animator.start();
    }

    private void finishFade(@NotNull Image image, int nextIndex) {
        Image previous = this.current;
        this.current = image;
        this.incoming = null;
        this.index = nextIndex;
        this.drawable.setImages(null, image);
        this.drawable.setProgress(1.0F);

        if (previous != null && previous != image) previous.close();
        this.schedule();
    }

    private void load(int fileIndex, @NotNull Callback callback) {
        if (this.loading) return;
        this.loading = true;

        Path path = this.files.get(fileIndex);
        int myToken = ++this.token;

        CompletableFuture.supplyAsync(() -> {
            try {
                return BitmapFactory.decodePath(path);
            } catch (Throwable error) {
                LOGGER.warn("Could not decode the background picture {}", path, error);
                return null;
            }
        }).whenComplete((bitmap, error) -> {
            if (bitmap == null) {
                MuiModApi.postToUiThread(() -> {
                    if (!this.running || myToken != this.token) return;
                    this.loading = false;
                    this.index = fileIndex;
                    this.schedule();
                });
                return;
            }

            MuiModApi.postToUiThread(() -> {
                if (!this.running || myToken != this.token) {
                    bitmap.close();
                    this.loading = false;
                    return;
                }

                Image created = Image.createTextureFromBitmap(bitmap);
                bitmap.close();
                this.loading = false;
                if (created == null) {
                    this.schedule();
                    return;
                }
                callback.onLoaded(created);
            });
        });
    }

    private void closeImages() {
        if (this.current != null) {
            this.current.close();
            this.current = null;
        }
        if (this.incoming != null) {
            this.incoming.close();
            this.incoming = null;
        }
    }

    private interface Callback {
        void onLoaded(@NotNull Image image);
    }
}