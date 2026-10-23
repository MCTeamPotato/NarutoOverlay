package me.kall.narutooverlay.config.ui.page;

import me.kall.narutooverlay.config.ui.widget.UiKit;
import icyllis.modernui.core.Context;
import icyllis.modernui.view.Gravity;
import icyllis.modernui.view.MeasureSpec;
import icyllis.modernui.view.View;
import icyllis.modernui.widget.FrameLayout;
import icyllis.modernui.widget.LinearLayout;
import icyllis.modernui.widget.ScrollView;
import icyllis.modernui.widget.TextView;
import org.jetbrains.annotations.NotNull;

public abstract class Page {

    private static final float MAX_CONTENT_WIDTH = 880.0F;

    protected final Context context;
    protected final LinearLayout content;

    private final ScrollView scroll;

    protected Page(@NotNull Context context) {
        this.context = context;

        this.scroll = new CenteredScrollView(context, UiKit.dp(context, MAX_CONTENT_WIDTH));
        this.scroll.setClipToPadding(false);

        this.content = new LinearLayout(context);
        this.content.setOrientation(LinearLayout.VERTICAL);
        this.content.setClipToPadding(false);
        int padding = UiKit.dp(context, 20);
        this.content.setPadding(padding, padding, padding, UiKit.dp(context, 48));

        this.scroll.addView(this.content, new FrameLayout.LayoutParams(UiKit.MATCH, UiKit.WRAP));
    }

    public final @NotNull View root() {
        return this.scroll;
    }

    protected final void build() {
        this.content.removeAllViews();
        this.populate();
    }

    public final void rebuild() {
        this.build();
    }

    protected abstract void populate();

    private static final class CenteredScrollView extends ScrollView {
        private final int maxWidth;

        CenteredScrollView(@NotNull Context context, int maxWidth) {
            super(context);
            this.maxWidth = maxWidth;
        }

        @Override
        protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
            if (this.maxWidth > 0 && MeasureSpec.getMode(widthMeasureSpec) != MeasureSpec.UNSPECIFIED
                    && MeasureSpec.getSize(widthMeasureSpec) > this.maxWidth) {
                widthMeasureSpec = MeasureSpec.makeMeasureSpec(this.maxWidth, MeasureSpec.EXACTLY);
            }
            super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        }
    }

    protected final void header(@NotNull String titleKey, @NotNull String subtitleKey) {
        TextView title = UiKit.heading(this.context, UiKit.str(titleKey), 30, UiKit.onSurface(this.context));
        this.content.addView(title, UiKit.wrap());

        TextView subtitle = UiKit.text(this.context, UiKit.str(subtitleKey), 12, UiKit.onSurfaceVariant(this.context));
        LinearLayout.LayoutParams params = UiKit.fill();
        params.topMargin = UiKit.dp(this.context, 6);
        this.content.addView(subtitle, params);
    }

    protected final @NotNull LinearLayout section(@NotNull String titleKey) {
        TextView title = UiKit.heading(this.context, UiKit.str(titleKey), 16, UiKit.primary(this.context));
        LinearLayout.LayoutParams headerParams = UiKit.wrap();
        headerParams.setMargins(0, UiKit.dp(this.context, 28), 0, UiKit.dp(this.context, 8));
        this.content.addView(title, headerParams);

        LinearLayout card = UiKit.card(this.context);
        this.content.addView(card, UiKit.fill());
        return card;
    }
}
