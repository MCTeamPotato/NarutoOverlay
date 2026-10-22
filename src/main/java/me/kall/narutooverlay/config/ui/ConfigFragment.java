package me.kall.narutooverlay.config.ui;

import icyllis.modernui.animation.Animator;
import icyllis.modernui.animation.AnimatorListener;
import icyllis.modernui.animation.ColorEvaluator;
import icyllis.modernui.animation.MotionEasingUtils;
import icyllis.modernui.animation.ValueAnimator;
import icyllis.modernui.core.Context;
import icyllis.modernui.fragment.Fragment;
import icyllis.modernui.graphics.Bitmap;
import icyllis.modernui.graphics.BitmapFactory;
import icyllis.modernui.graphics.Image;
import icyllis.modernui.graphics.drawable.ColorDrawable;
import icyllis.modernui.graphics.drawable.ShapeDrawable;
import icyllis.modernui.mc.MuiModApi;
import icyllis.modernui.mc.ui.ThemeControl;
import icyllis.modernui.util.ColorStateList;
import icyllis.modernui.util.DataSet;
import icyllis.modernui.view.Gravity;
import icyllis.modernui.view.LayoutInflater;
import icyllis.modernui.view.View;
import icyllis.modernui.view.ViewGroup;
import icyllis.modernui.widget.FrameLayout;
import icyllis.modernui.widget.LinearLayout;
import icyllis.modernui.widget.ScrollView;
import icyllis.modernui.widget.TextView;
import icyllis.modernui.widget.Toast;
import me.kall.narutooverlay.config.ConfigSaver;
import me.kall.narutooverlay.config.RenderArgs;
import net.minecraft.client.Minecraft;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public final class ConfigFragment extends Fragment implements MuiModApi.OnWindowResizeListener, BackgroundHost {
    public static final String ARG_CARD = "card";
    public static final String ARG_PAGE = "page";

    private static final Logger LOGGER = LogManager.getLogger("NarutoOverlay");

    private static @Nullable ConfigFragment active;

    public static @Nullable ConfigFragment active() {
        return active;
    }

    private static final String[] NAV_KEYS = {
            "narutooverlay.config.nav.cards",
            "narutooverlay.config.nav.appearance",
            "narutooverlay.config.nav.about"
    };

    private static final int RAIL_WIDTH = 112;
    private static final int RAIL_ITEM_MARGIN = 12;

    private final List<TextView> navItems = new ArrayList<>(NAV_KEYS.length);

    private @Nullable FrameLayout rootView;
    private @Nullable FrameLayout content;
    private @Nullable LinearLayout rail;
    private @Nullable TextView logo;
    private @Nullable TextView hintView;
    private @Nullable String initialCard;
    private @Nullable ValueAnimator pageAnimator;
    private @Nullable ValueAnimator themeAnimator;
    private int pageToken;
    private int themeToken;
    private int accentColor;
    private int onAccentColor;
    private int selected = -1;

    @Override
    public @NotNull View onCreateView(@NotNull LayoutInflater inflater, @Nullable ViewGroup container,
                                      @Nullable DataSet savedInstanceState) {
        Context context = requireContext();

        DataSet args = getArguments();
        this.initialCard = args == null ? null : args.getString(ARG_CARD);
        int page = args == null ? 0 : args.getInt(ARG_PAGE, 0);

        FrameLayout root = new FrameLayout(context);
        root.setBackground(new ColorDrawable(0xC00A0B0F));
        this.rootView = root;

        this.accentColor = Accent.primary();
        this.onAccentColor = Accent.onPrimary();

        LinearLayout shell = new LinearLayout(context);
        shell.setOrientation(LinearLayout.HORIZONTAL);
        shell.setShowDividers(LinearLayout.SHOW_DIVIDER_MIDDLE);
        shell.setDividerDrawable(ThemeControl.makeDivider(shell, true));
        root.addView(shell, UiKit.MATCH, UiKit.MATCH);

        shell.addView(this.rail = buildRail(context),
                new LinearLayout.LayoutParams(UiKit.dp(context, RAIL_WIDTH), UiKit.MATCH));

        this.content = new FrameLayout(context);
        shell.addView(this.content, new LinearLayout.LayoutParams(0, UiKit.MATCH, 1.0F));

        select(Math.max(0, Math.min(NAV_KEYS.length - 1, page)));
        refreshBackground();
        return root;
    }

    @Override
    public void onViewCreated(@NotNull View view, @Nullable DataSet savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        active = this;
        MuiModApi.addOnWindowResizeListener(this);
    }

    public void showOverlay(@NotNull View overlay) {
        if (this.rootView == null) return;
        this.rootView.addView(overlay, new FrameLayout.LayoutParams(UiKit.MATCH, UiKit.MATCH));
    }

    @Override
    public void onWindowResize(int width, int height, int guiScale, int oldGuiScale) {
        View root = getView();
        if (root != null) {
            root.post(this::rebuildPage);
        }
    }

    @Override
    public void chooseBackgroundImage() {
        ImagePicker.open(new ImagePicker.Callback() {
            @Override
            public void onPicked(@NotNull Path path) {
                ConfigFragment.this.onImagePicked(path);
            }

            @Override
            public void onFailed() {
                toast(UiKit.str("narutooverlay.config.background.pickerFailed"));
            }
        });
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
        refreshBackground();
        rebuildPage();
    }

    @Override
    public void setBackgroundDim(double dim) {
        RenderArgs.BACKGROUND_DIM.set(dim);
        ConfigSaver.changed();
        refreshBackground();
    }

    private void refreshBackground() {
        FrameLayout root = this.rootView;
        if (root == null) return;

        Image image = ConfigBackground.image();
        if (image != null) {
            root.setBackground(new ImageBackgroundDrawable(image,
                    RenderArgs.BACKGROUND_CROP_X.get(),
                    RenderArgs.BACKGROUND_CROP_Y.get(),
                    RenderArgs.BACKGROUND_CROP_WIDTH.get(),
                    RenderArgs.BACKGROUND_CROP_HEIGHT.get(),
                    RenderArgs.BACKGROUND_DIM.get()));
        } else {
            root.setBackground(new ColorDrawable(0xC00A0B0F));
            ConfigBackground.loadAsync(this::refreshBackground);
        }
    }

    private void onImagePicked(@NotNull Path path) {
        CompletableFuture.supplyAsync(() -> {
            try {
                return BitmapFactory.decodePath(path);
            } catch (Throwable ignored) {
                return null;
            }
        }).whenComplete((bitmap, error) -> MuiModApi.postToUiThread(() -> {
            if (bitmap == null) {
                toast(UiKit.str("narutooverlay.config.background.failed"));
                return;
            }
            startCrop(path, bitmap);
        }));
    }

    private void startCrop(@NotNull Path path, @NotNull Bitmap bitmap) {
        Context context = getContext();
        FrameLayout root = this.rootView;
        int imageWidth = bitmap.getWidth();
        int imageHeight = bitmap.getHeight();
        if (context == null || root == null) {
            bitmap.close();
            return;
        }

        Minecraft minecraft = Minecraft.getInstance();
        float target = (float) minecraft.getWindow().getWidth() / (float) minecraft.getWindow().getHeight();
        float actual = (float) imageWidth / (float) imageHeight;
        if (Math.abs(actual - target) <= 0.01F) {
            bitmap.close();
            saveBackground(path, imageWidth, imageHeight, 0, 0, imageWidth, imageHeight);
            return;
        }

        Image preview = Image.createTextureFromBitmap(bitmap);
        bitmap.close();
        if (preview == null) return;

        CropOverlay[] holder = new CropOverlay[1];
        CropOverlay.Listener listener = new CropOverlay.Listener() {
            @Override
            public void onCrop(int x, int y, int width, int height) {
                root.removeView(holder[0]);
                preview.close();
                saveBackground(path, imageWidth, imageHeight, x, y, width, height);
            }

            @Override
            public void onCancel() {
                root.removeView(holder[0]);
                preview.close();
            }
        };

        holder[0] = new CropOverlay(context, preview, imageWidth, imageHeight, target, listener);
        root.addView(holder[0], new FrameLayout.LayoutParams(UiKit.MATCH, UiKit.MATCH));
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
                toast(UiKit.str("narutooverlay.config.background.failed"));
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
            refreshBackground();
            rebuildPage();
            toast(UiKit.str("narutooverlay.config.background.saved"));
        }));
    }

    private static double clamp01(double value) {
        return Math.max(0.0D, Math.min(1.0D, value));
    }

    private void toast(@NotNull CharSequence message) {
        Context context = getContext();
        if (context != null) {
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
        }
    }

    public void onThemeChanged() {
        int toAccent = Accent.primary();
        int toOnAccent = Accent.onPrimary();
        if (toAccent == this.accentColor && toOnAccent == this.onAccentColor) return;

        this.crossFadeAccent(this.accentColor, this.onAccentColor, toAccent, toOnAccent);
    }

    private void crossFadeAccent(int fromAccent, int fromOnAccent, int toAccent, int toOnAccent) {
        FrameLayout content = this.content;
        Context context = getContext();
        if (content == null || context == null) return;

        cancelThemeTransition();

        View oldPage = content.getChildCount() == 0
                ? null : content.getChildAt(content.getChildCount() - 1);
        Page page = createPage(this.selected);
        if (page == null) return;

        View newPage = page.root();
        newPage.setAlpha(0.0F);
        content.addView(newPage, new FrameLayout.LayoutParams(UiKit.MATCH, UiKit.MATCH,
                Gravity.CENTER_HORIZONTAL));

        if (oldPage instanceof ScrollView oldScroll && newPage instanceof ScrollView newScroll) {
            int scrollY = oldScroll.getScrollY();
            newScroll.scrollTo(0, scrollY);
            newScroll.post(() -> newScroll.scrollTo(0, scrollY));
        }

        final int token = ++this.themeToken;
        ValueAnimator animator = ValueAnimator.ofFloat(0.0F, 1.0F);
        animator.setDuration(1000L);
        animator.setInterpolator(MotionEasingUtils.MOTION_EASING_STANDARD);
        animator.addUpdateListener(animation -> {
            if (token != this.themeToken) return;
            float fraction = (Float) animation.getAnimatedValue();
            newPage.setAlpha(fraction);
            if (oldPage != null) oldPage.setAlpha(1.0F - fraction);

            int accent = ColorEvaluator.evaluate(fraction, fromAccent, toAccent);
            int onAccent = ColorEvaluator.evaluate(fraction, fromOnAccent, toOnAccent);
            this.accentColor = accent;
            this.onAccentColor = onAccent;
            this.applyChromeAccent(accent, onAccent);
        });
        animator.addListener(new AnimatorListener() {
            @Override
            public void onAnimationEnd(@NotNull Animator animation) {
                if (token != ConfigFragment.this.themeToken) return;
                if (oldPage != null && oldPage.getParent() == content) content.removeView(oldPage);
                newPage.setAlpha(1.0F);
                ConfigFragment.this.accentColor = toAccent;
                ConfigFragment.this.onAccentColor = toOnAccent;
                ConfigFragment.this.applyChromeAccent(toAccent, toOnAccent);
                ConfigFragment.this.themeAnimator = null;
            }
        });
        this.themeAnimator = animator;
        animator.start();
    }

    private void applyChromeAccent(int accent, int onAccent) {
        TextView logo = this.logo;
        if (logo != null) {
            ShapeDrawable circle = new ShapeDrawable();
            circle.setCornerRadius(UiKit.dp(logo.getContext(), 18));
            circle.setColor(accent);
            logo.setBackground(circle);
            logo.setTextColor(onAccent);
        }

        for (int i = 0; i < this.navItems.size(); i++) {
            this.styleNav(this.navItems.get(i), i == this.selected, accent);
        }

        this.styleHint(accent);
    }

    private void cancelThemeTransition() {
        this.themeToken++;
        if (this.themeAnimator != null) {
            this.themeAnimator.cancel();
            this.themeAnimator = null;
        }
        finishThemeTransition();
    }

    private void finishThemeTransition() {
        FrameLayout content = this.content;
        if (content == null) return;
        while (content.getChildCount() > 1) {
            content.removeViewAt(0);
        }
        View page = content.getChildAt(0);
        if (page != null) page.setAlpha(1.0F);
    }

    private void rebuildPage() {
        if (this.content == null) return;
        this.refreshChrome(requireContext());

        int index = this.selected;
        this.selected = -1;
        select(index);
    }

    private void refreshChrome(@NotNull Context context) {
        if (this.rail != null) {
            LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) this.rail.getLayoutParams();
            params.width = UiKit.dp(context, RAIL_WIDTH);
            this.rail.setLayoutParams(params);
            this.rail.setPadding(0, UiKit.dp(context, 18), 0, UiKit.dp(context, 18));
        }

        if (this.logo != null) {
            LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) this.logo.getLayoutParams();
            params.width = UiKit.dp(context, 36);
            params.height = UiKit.dp(context, 36);
            this.logo.setLayoutParams(params);

            ShapeDrawable circle = new ShapeDrawable();
            circle.setCornerRadius(UiKit.dp(context, 18));
            circle.setColor(UiKit.primary(context));
            this.logo.setBackground(circle);
            this.logo.setTextColor(UiKit.onPrimary(context));
            this.logo.setTextSize(16);
        }

        for (TextView item : this.navItems) {
            item.setTextSize(12);
            item.setPadding(0, UiKit.dp(context, 12), 0, UiKit.dp(context, 12));
            LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) item.getLayoutParams();
            params.setMargins(UiKit.dp(context, RAIL_ITEM_MARGIN), UiKit.dp(context, 4),
                    UiKit.dp(context, RAIL_ITEM_MARGIN), UiKit.dp(context, 4));
            item.setLayoutParams(params);
        }

        this.styleHint(context);
        this.accentColor = UiKit.primary(context);
        this.onAccentColor = UiKit.onPrimary(context);
    }

    private void styleHint(@NotNull Context context) {
        this.styleHint(UiKit.primary(context));
    }

    private void styleHint(int accent) {
        TextView hint = this.hintView;
        if (hint == null) return;
        Context context = hint.getContext();

        ShapeDrawable box = new ShapeDrawable();
        box.setCornerRadius(UiKit.dp(context, 8));
        box.setColor(ColorStateList.modulateColor(accent, 0.14F));
        box.setStroke(Math.max(1, UiKit.dp(context, 1)),
                ColorStateList.modulateColor(accent, 0.7F));
        hint.setBackground(box);
        hint.setTextColor(UiKit.onSurfaceVariant(context));
    }

    private @NotNull LinearLayout buildRail(@NotNull Context context) {
        LinearLayout rail = UiKit.column(context);
        rail.setGravity(Gravity.CENTER_HORIZONTAL);
        rail.setBackground(new ColorDrawable(
                ColorStateList.modulateColor(UiKit.surfaceContainer(context), 0.72F)));
        rail.setPadding(0, UiKit.dp(context, 18), 0, UiKit.dp(context, 18));

        TextView logo = UiKit.heading(context, "N", 16, UiKit.onPrimary(context));
        logo.setGravity(Gravity.CENTER);
        ShapeDrawable circle = new ShapeDrawable();
        circle.setCornerRadius(UiKit.dp(context, 18));
        circle.setColor(UiKit.primary(context));
        logo.setBackground(circle);
        rail.addView(logo, new LinearLayout.LayoutParams(UiKit.dp(context, 36), UiKit.dp(context, 36)));
        this.logo = logo;

        LinearLayout nav = UiKit.column(context);
        nav.setGravity(Gravity.CENTER);
        rail.addView(nav, new LinearLayout.LayoutParams(UiKit.MATCH, 0, 1.0F));

        for (int i = 0; i < NAV_KEYS.length; i++) {
            final int index = i;
            TextView item = UiKit.text(context, UiKit.str(NAV_KEYS[i]), 12, UiKit.onSurfaceVariant(context));
            item.setGravity(Gravity.CENTER);
            item.setPadding(0, UiKit.dp(context, 12), 0, UiKit.dp(context, 12));
            item.setOnClickListener(view -> select(index));

            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(UiKit.MATCH, UiKit.WRAP);
            params.setMargins(UiKit.dp(context, RAIL_ITEM_MARGIN), UiKit.dp(context, 4),
                    UiKit.dp(context, RAIL_ITEM_MARGIN), UiKit.dp(context, 4));
            nav.addView(item, params);
            this.navItems.add(item);
        }

        TextView hint = UiKit.text(context, UiKit.str("narutooverlay.config.footer.escHint"), 9,
                UiKit.onSurfaceVariant(context));
        hint.setGravity(Gravity.CENTER);
        hint.setPadding(UiKit.dp(context, 4), UiKit.dp(context, 4),
                UiKit.dp(context, 4), UiKit.dp(context, 4));
        this.hintView = hint;
        this.styleHint(context);

        LinearLayout.LayoutParams hintParams = new LinearLayout.LayoutParams(UiKit.MATCH, UiKit.WRAP);
        hintParams.setMargins(UiKit.dp(context, 4), UiKit.dp(context, 8),
                UiKit.dp(context, 4), 0);
        rail.addView(hint, hintParams);
        return rail;
    }

    private void select(int index) {
        if (index == this.selected) return;
        int previous = this.selected;
        this.selected = index;

        for (int i = 0; i < this.navItems.size(); i++) {
            styleNav(this.navItems.get(i), i == index);
        }

        if (this.content == null) return;

        if (previous < 0) {

            cancelPageAnimation();
            swapPage(index);
        } else {
            animatePageChange(index);
        }
    }

    private void swapPage(int index) {
        FrameLayout content = this.content;
        if (content == null) return;
        cancelThemeTransition();
        content.removeAllViews();

        Context context = getContext();
        if (context != null) this.refreshChrome(context);

        Page page = createPage(index);
        if (page == null) return;

        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(UiKit.MATCH, UiKit.MATCH,
                Gravity.CENTER_HORIZONTAL);
        content.addView(page.root(), params);
    }

    private @Nullable Page createPage(int index) {
        Context context = getContext();
        if (context == null) return null;

        Page page = switch (index) {
            case 0 -> new CardsPage(context, this.initialCard);
            case 1 -> new AppearancePage(context);
            default -> new AboutPage(context, this);
        };

        this.initialCard = null;
        return page;
    }

    private void animatePageChange(int index) {
        FrameLayout content = this.content;
        if (content == null || content.getChildCount() == 0 || content.getWidth() <= 0) {
            cancelPageAnimation();
            swapPage(index);
            return;
        }

        cancelPageAnimation();
        final int token = ++this.pageToken;
        final float offset = UiKit.dp(requireContext(), 14);

        swapPage(index);
        View page = content.getChildAt(0);
        if (page == null) return;
        page.setTranslationY(offset);

        ValueAnimator in = ValueAnimator.ofFloat(0.0F, 1.0F);
        in.setDuration(200L);
        in.setInterpolator(MotionEasingUtils.MOTION_EASING_EMPHASIZED_DECELERATE);
        in.addUpdateListener(animation -> {
            if (token != pageToken) return;
            float fraction = (Float) animation.getAnimatedValue();
            page.setTranslationY(offset * (1.0F - fraction));
        });
        in.addListener(new AnimatorListener() {
            @Override
            public void onAnimationEnd(@NotNull Animator animation) {
                page.setTranslationY(0.0F);
            }
        });
        this.pageAnimator = in;
        in.start();
    }

    private void cancelPageAnimation() {
        this.pageToken++;
        if (this.pageAnimator != null) {
            this.pageAnimator.cancel();
            this.pageAnimator = null;
        }
        if (this.content != null) {
            View page = this.content.getChildAt(0);
            if (page != null) page.setTranslationY(0.0F);
        }
    }

    private void styleNav(@NotNull TextView item, boolean active) {
        this.styleNav(item, active, UiKit.primary(item.getContext()));
    }

    private void styleNav(@NotNull TextView item, boolean active, int accent) {
        Context context = item.getContext();

        ShapeDrawable background = new ShapeDrawable();
        background.setCornerRadius(UiKit.dp(context, 22));
        background.setColor(active
                ? ColorStateList.modulateColor(accent, 0.18F)
                : 0x00000000);
        item.setBackground(background);
        item.setTextColor(active ? accent : UiKit.onSurfaceVariant(context));
    }

    @Override
    public void onDestroyView() {
        MuiModApi.removeOnWindowResizeListener(this);
        this.themeToken++;
        if (this.themeAnimator != null) {
            this.themeAnimator.cancel();
            this.themeAnimator = null;
        }
        super.onDestroyView();
        if (active == this) active = null;
        this.rootView = null;
        this.content = null;
        this.rail = null;
        this.logo = null;
        this.hintView = null;
        this.navItems.clear();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        ConfigSaver.flush();
    }
}
