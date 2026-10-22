package me.kall.narutooverlay.config.ui;

import icyllis.modernui.animation.Animator;
import icyllis.modernui.animation.AnimatorListener;
import icyllis.modernui.animation.MotionEasingUtils;
import icyllis.modernui.animation.ValueAnimator;
import icyllis.modernui.core.Context;
import icyllis.modernui.view.Gravity;
import icyllis.modernui.view.MeasureSpec;
import icyllis.modernui.view.View;
import icyllis.modernui.view.ViewGroup;
import icyllis.modernui.widget.EditText;
import icyllis.modernui.widget.FrameLayout;
import icyllis.modernui.widget.LinearLayout;
import icyllis.modernui.widget.Switch;
import icyllis.modernui.widget.TextView;
import me.kall.narutooverlay.card.Card;
import me.kall.narutooverlay.card.CardRegistry;
import me.kall.narutooverlay.card.Field;
import me.kall.narutooverlay.card.FieldConfig;
import me.kall.narutooverlay.config.ConfigSaver;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.language.I18n;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Locale;

public final class CardsPage extends Page {
    private static final String GROUP = "narutooverlay.config.";

    private final @Nullable String initialCard;

    public CardsPage(@NotNull Context context, @Nullable String initialCard) {
        super(context);
        this.initialCard = initialCard;
        build();
    }

    @Override
    protected void populate() {
        header("narutooverlay.config.cards.title", "narutooverlay.config.cards.subtitle");

        View gap = new View(this.context);
        this.content.addView(gap, new LinearLayout.LayoutParams(UiKit.MATCH, UiKit.dp(this.context, 18)));

        String expand = this.initialCard != null ? this.initialCard
                : CardRegistry.all().isEmpty() ? null : CardRegistry.all().get(0).id();

        for (Card<?> card : CardRegistry.all()) {
            addCard(card, card.id().equals(expand));
        }

        TextView reset = UiKit.text(this.context, UiKit.str("narutooverlay.config.resetAll"), 12,
                UiKit.error(this.context));
        reset.setOnClickListener(view -> {
            CardRegistry.resetAll();
            ConfigSaver.changed();
            rebuild();
        });
        LinearLayout.LayoutParams params = UiKit.wrap();
        params.setMargins(0, UiKit.dp(this.context, 20), 0, 0);
        this.content.addView(reset, params);
    }

    private void addCard(@NotNull Card<?> card, boolean expanded) {
        LinearLayout box = UiKit.card(this.context);

        LinearLayout body = UiKit.column(this.context);

        RevealLayout reveal = new RevealLayout(this.context);
        reveal.setClipChildren(true);
        reveal.addView(body, new FrameLayout.LayoutParams(UiKit.MATCH, UiKit.WRAP));

        LinearLayout.LayoutParams revealParams = new LinearLayout.LayoutParams(UiKit.MATCH,
                expanded ? UiKit.WRAP : 0);
        reveal.setVisibility(expanded ? View.VISIBLE : View.GONE);

        TextView chevron = UiKit.text(this.context, expanded ? "-" : "+", 16, UiKit.onSurfaceVariant(this.context));
        chevron.setGravity(Gravity.CENTER);
        chevron.setMinimumWidth(UiKit.dp(this.context, 20));

        TextView title = UiKit.heading(this.context, cardName(card), 15, UiKit.onSurface(this.context));
        TextView summary = UiKit.text(this.context, summary(card), 11, UiKit.onSurfaceVariant(this.context));

        LinearLayout texts = UiKit.column(this.context);
        texts.addView(title, UiKit.fill());
        texts.addView(summary, UiKit.fill());

        Switch toggle = UiKit.switchControl(this.context);
        toggle.setChecked(card.enabledValue().get());
        toggle.setOnCheckedChangeListener((view, checked) -> {
            card.enabledValue().set(checked);
            ConfigSaver.changed();
        });

        LinearLayout head = UiKit.row(this.context);
        head.setPadding(UiKit.dp(this.context, 2), UiKit.dp(this.context, 8),
                UiKit.dp(this.context, 2), UiKit.dp(this.context, 8));
        head.addView(chevron, UiKit.wrap());

        LinearLayout.LayoutParams textParams = UiKit.weight();
        textParams.setMargins(UiKit.dp(this.context, 10), 0, 0, 0);
        head.addView(texts, textParams);

        LinearLayout.LayoutParams toggleParams = UiKit.wrap();
        toggleParams.setMargins(UiKit.dp(this.context, 12), 0, 0, 0);
        head.addView(toggle, toggleParams);

        head.setOnClickListener(view -> toggleReveal(box, reveal, chevron, reveal.getVisibility() != View.VISIBLE));

        box.addView(head, UiKit.fill());
        box.addView(reveal, revealParams);

        addPositionSection(card, body, summary);
        addFieldsSection(card, body, summary);
        addCardFooter(card, body);

        LinearLayout.LayoutParams params = UiKit.fill();
        params.bottomMargin = UiKit.dp(this.context, 12);
        this.content.addView(box, params);
    }

    private void toggleReveal(@NotNull LinearLayout box, @NotNull RevealLayout reveal,
                              @NotNull TextView chevron, boolean expand) {
        LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) reveal.getLayoutParams();
        int from = reveal.getVisibility() == View.VISIBLE ? reveal.getHeight() : 0;
        int target = 0;

        if (expand) {
            int width = box.getWidth() - box.getPaddingLeft() - box.getPaddingRight();
            if (width <= 0) width = reveal.getWidth();
            if (width <= 0) {

                reveal.setVisibility(View.VISIBLE);
                return;
            }
            params.height = UiKit.WRAP;
            reveal.setLayoutParams(params);
            reveal.measure(MeasureSpec.makeMeasureSpec(width, MeasureSpec.EXACTLY),
                    MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED));
            target = reveal.getMeasuredHeight();

            reveal.setVisibility(View.VISIBLE);
            params.height = 0;
            reveal.setLayoutParams(params);
        }

        chevron.setText(expand ? "-" : "+");

        ValueAnimator animator = ValueAnimator.ofInt(from, target);
        animator.setDuration(190L);
        animator.setInterpolator(MotionEasingUtils.MOTION_EASING_STANDARD);
        animator.addUpdateListener(animation -> {
            params.height = (Integer) animation.getAnimatedValue();
            reveal.requestLayout();
        });
        animator.addListener(new AnimatorListener() {
            @Override
            public void onAnimationEnd(@NotNull Animator animation) {
                if (expand) {
                    params.height = UiKit.WRAP;
                } else {
                    params.height = 0;
                    reveal.setVisibility(View.GONE);
                }
                reveal.requestLayout();
            }
        });
        animator.start();
    }

    private static final class RevealLayout extends FrameLayout {
        RevealLayout(@NotNull Context context) {
            super(context);
        }

        @Override
        protected void measureChildWithMargins(@NotNull View child, int parentWidthMeasureSpec, int widthUsed,
                                               int parentHeightMeasureSpec, int heightUsed) {
            ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) child.getLayoutParams();
            int childWidthSpec = getChildMeasureSpec(parentWidthMeasureSpec,
                    getPaddingLeft() + getPaddingRight() + params.leftMargin + params.rightMargin + widthUsed,
                    params.width);
            child.measure(childWidthSpec, MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED));
        }
    }

    private void addPositionSection(@NotNull Card<?> card, @NotNull LinearLayout body,
                                    @NotNull TextView summaryView) {
        body.addView(caption("narutooverlay.config.cards.position"), captionParams());

        LinearLayout row = UiKit.row(this.context);

        EditText x = UiKit.numberField(this.context, card.leftValue().get());
        EditText y = UiKit.numberField(this.context, card.topValue().get());
        x.setGravity(Gravity.CENTER);
        y.setGravity(Gravity.CENTER);

        Runnable sync = () -> {
            if (!x.getText().toString().equals(String.valueOf(card.leftValue().get()))) {
                x.setText(String.valueOf(card.leftValue().get()));
            }
            if (!y.getText().toString().equals(String.valueOf(card.topValue().get()))) {
                y.setText(String.valueOf(card.topValue().get()));
            }
            summaryView.setText(summary(card));
        };

        row.addView(UiKit.text(this.context, "X", 12, UiKit.onSurfaceVariant(this.context)), UiKit.wrap());
        row.addView(x, fieldParams());
        row.addView(UiKit.text(this.context, "Y", 12, UiKit.onSurfaceVariant(this.context)), gapParams());
        row.addView(y, fieldParams());
        row.addView(new View(this.context), UiKit.weight());
        row.addView(resetButton(() -> {
            card.resetPosition();
            ConfigSaver.changed();
            sync.run();
        }), UiKit.wrap());

        UiKit.bindNumber(x, 0, Integer.MAX_VALUE, value -> {
            card.leftValue().set(value);
            ConfigSaver.changed();
            summaryView.setText(summary(card));
        });
        UiKit.bindNumber(y, 0, Integer.MAX_VALUE, value -> {
            card.topValue().set(value);
            ConfigSaver.changed();
            summaryView.setText(summary(card));
        });

        body.addView(row, UiKit.fill());

        LinearLayout presets = UiKit.row(this.context);

        presets.setPadding(0, 0, 0, 0);
        presets.setMinimumHeight(0);
        presets.addView(presetButton("narutooverlay.config.cards.topLeft", card, 0, 0, sync), presetParams(0));
        presets.addView(presetButton("narutooverlay.config.cards.topRight", card, 1, 0, sync), presetParams(1));
        presets.addView(presetButton("narutooverlay.config.cards.bottomLeft", card, 0, 1, sync), presetParams(2));
        presets.addView(presetButton("narutooverlay.config.cards.bottomRight", card, 1, 1, sync), presetParams(3));

        LinearLayout.LayoutParams presetsParams = UiKit.fill();
        presetsParams.topMargin = UiKit.dp(this.context, 6);
        body.addView(presets, presetsParams);
    }

    private void addFieldsSection(@NotNull Card<?> card, @NotNull LinearLayout body,
                                  @NotNull TextView summaryView) {
        View spacer = new View(this.context);
        body.addView(spacer, new LinearLayout.LayoutParams(UiKit.MATCH, UiKit.dp(this.context, 10)));
        body.addView(caption("narutooverlay.config.cards.fields"), captionParams());

        boolean first = true;
        for (Field<?> field : card.allFields()) {
            if (!first) body.addView(UiKit.divider(this.context), dividerParams());
            first = false;

            FieldConfig config = card.fieldConfig(field.id());

            LinearLayout row = UiKit.row(this.context);
            row.addView(UiKit.text(this.context, fieldName(field), 13, UiKit.onSurface(this.context)), UiKit.weight());

            if (field.hasLabel()) {
                row.addView(UiKit.chip(this.context, UiKit.str(GROUP + "chip.label"), config.showLabel(), checked -> {
                    config.showLabelValue().set(checked);
                    ConfigSaver.changed();
                }), chipParams());
            }
            if (field.hasUnit()) {
                row.addView(UiKit.chip(this.context, UiKit.str(GROUP + "chip.unit"), config.showUnit(), checked -> {
                    config.showUnitValue().set(checked);
                    ConfigSaver.changed();
                }), chipParams());
            }

            Switch toggle = UiKit.switchControl(this.context);
            toggle.setChecked(config.enabled());
            toggle.setOnCheckedChangeListener((view, checked) -> {
                config.enabledValue().set(checked);
                ConfigSaver.changed();
                summaryView.setText(summary(card));
            });
            LinearLayout.LayoutParams toggleParams = UiKit.wrap();
            toggleParams.setMargins(UiKit.dp(this.context, 10), 0, 0, 0);
            row.addView(toggle, toggleParams);

            body.addView(row, UiKit.fill());
        }
    }

    private void addCardFooter(@NotNull Card<?> card, @NotNull LinearLayout body) {
        TextView reset = UiKit.text(this.context, UiKit.str("narutooverlay.config.cards.resetCard"), 12,
                UiKit.primary(this.context));
        reset.setOnClickListener(view -> {
            card.resetAll();
            ConfigSaver.changed();
            rebuild();
        });

        LinearLayout.LayoutParams params = UiKit.wrap();
        params.setMargins(0, UiKit.dp(this.context, 10), 0, 0);
        body.addView(reset, params);
    }

    private @NotNull TextView caption(@NotNull String key) {
        return UiKit.text(this.context, UiKit.str(key), 11, UiKit.onSurfaceVariant(this.context));
    }

    private @NotNull LinearLayout.LayoutParams captionParams() {
        LinearLayout.LayoutParams params = UiKit.wrap();
        params.setMargins(0, UiKit.dp(this.context, 10), 0, UiKit.dp(this.context, 2));
        return params;
    }

    private @NotNull LinearLayout.LayoutParams fieldParams() {
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(UiKit.dp(this.context, 64), UiKit.WRAP);
        params.setMargins(UiKit.dp(this.context, 6), 0, 0, 0);
        return params;
    }

    private @NotNull LinearLayout.LayoutParams gapParams() {
        LinearLayout.LayoutParams params = UiKit.wrap();
        params.setMargins(UiKit.dp(this.context, 14), 0, 0, 0);
        return params;
    }

    private @NotNull LinearLayout.LayoutParams presetParams(int index) {

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(0, UiKit.dp(this.context, 38), 1.0F);
        params.setMargins(index == 0 ? 0 : UiKit.dp(this.context, 6), 0, 0, 0);
        return params;
    }

    private @NotNull LinearLayout.LayoutParams chipParams() {
        LinearLayout.LayoutParams params = UiKit.wrap();
        params.setMargins(UiKit.dp(this.context, 8), 0, 0, 0);
        return params;
    }

    private @NotNull LinearLayout.LayoutParams dividerParams() {
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(UiKit.MATCH, UiKit.dp(this.context, 1));
        params.setMargins(0, UiKit.dp(this.context, 2), 0, UiKit.dp(this.context, 2));
        return params;
    }

    private @NotNull TextView resetButton(@NotNull Runnable onClick) {
        TextView button = UiKit.text(this.context, UiKit.str("narutooverlay.config.cards.resetPosition"), 12,
                UiKit.primary(this.context));
        button.setPadding(UiKit.dp(this.context, 8), UiKit.dp(this.context, 4),
                UiKit.dp(this.context, 8), UiKit.dp(this.context, 4));
        button.setOnClickListener(view -> onClick.run());
        return button;
    }

    private @NotNull TextView presetButton(@NotNull String key, @NotNull Card<?> card, int horizontal, int vertical,
                                           @NotNull Runnable sync) {
        TextView button = UiKit.text(this.context, UiKit.str(key), 11, UiKit.onSurfaceVariant(this.context));
        button.setGravity(Gravity.CENTER);
        button.setBackground(UiKit.outlinedBackground(this.context));
        button.setOnClickListener(view -> {
            Minecraft minecraft = Minecraft.getInstance();
            int width = card.lineWidth();
            if (width <= 0) width = 160;
            int height = minecraft.font.lineHeight;
            int margin = 8;
            int left = horizontal == 0 ? margin
                    : minecraft.getWindow().getGuiScaledWidth() - width - margin;
            int top = vertical == 0 ? margin
                    : minecraft.getWindow().getGuiScaledHeight() - height - margin;
            card.moveTo(left, top);
            ConfigSaver.changed();
            sync.run();
        });
        return button;
    }

    private static @NotNull String cardName(@NotNull Card<?> card) {
        String key = GROUP + "card." + card.id().toLowerCase(Locale.ROOT);
        return I18n.exists(key) ? I18n.get(key) : card.id();
    }

    private static @NotNull String fieldName(@NotNull Field<?> field) {
        String key = GROUP + "field." + field.id().toLowerCase(Locale.ROOT);
        return I18n.exists(key) ? I18n.get(key) : field.id();
    }

    private static @NotNull String summary(@NotNull Card<?> card) {
        int visible = 0;
        for (Field<?> field : card.allFields()) {
            if (card.fieldConfig(field.id()).enabled()) visible++;
        }
        return I18n.get(GROUP + "cards.summary", visible, card.allFields().size(),
                card.leftValue().get(), card.topValue().get());
    }
}
