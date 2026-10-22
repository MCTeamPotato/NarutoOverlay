package me.kall.narutooverlay.config.ui;

import icyllis.modernui.R;
import icyllis.modernui.core.Context;
import icyllis.modernui.graphics.drawable.ColorDrawable;
import icyllis.modernui.graphics.drawable.RippleDrawable;
import icyllis.modernui.graphics.drawable.ShapeDrawable;
import icyllis.modernui.resources.TypedValue;
import icyllis.modernui.text.*;
import icyllis.modernui.util.ColorStateList;
import icyllis.modernui.util.StateSet;
import icyllis.modernui.view.Gravity;
import icyllis.modernui.view.View;
import icyllis.modernui.view.ViewGroup;
import icyllis.modernui.widget.*;
import net.minecraft.client.resources.language.I18n;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;
import java.util.function.IntConsumer;

public final class UiKit {
    public static final int MATCH = ViewGroup.LayoutParams.MATCH_PARENT;
    public static final int WRAP = ViewGroup.LayoutParams.WRAP_CONTENT;

    private UiKit() {}

    public static @NotNull String str(@NotNull String key) {
        return I18n.get(key);
    }

    public static int dp(@NotNull Context context, float value) {
        return Math.round(value * context.getResources().getDisplayMetrics().density);
    }

    private static int attr(@NotNull Context context, @NotNull String name, int fallback) {
        TypedValue value = new TypedValue();
        if (context.getTheme().resolveAttribute(R.ns, name, value, true)) {
            return value.data;
        }
        return fallback;
    }

    public static int primary(@NotNull Context context) {
        return Accent.primary();
    }

    public static int secondaryContainer(@NotNull Context context) {
        return Accent.container();
    }

    public static int surfaceContainer(@NotNull Context context) {
        return attr(context, R.attr.colorSurfaceContainer, 0xFF16171C);
    }

    public static int surfaceContainerLow(@NotNull Context context) {
        return attr(context, R.attr.colorSurfaceContainerLow, 0xFF1F2027);
    }

    public static int surfaceContainerHighest(@NotNull Context context) {
        return attr(context, R.attr.colorSurfaceContainerHighest, 0xFF2A2B34);
    }

    public static int onSurface(@NotNull Context context) {
        return attr(context, R.attr.colorOnSurface, 0xFFEDEFF3);
    }

    public static int onPrimary(@NotNull Context context) {
        return Accent.onPrimary();
    }

    public static int onSurfaceVariant(@NotNull Context context) {
        return attr(context, R.attr.colorOnSurfaceVariant, 0xFFA6AAB4);
    }

    public static int outlineVariant(@NotNull Context context) {
        return attr(context, R.attr.colorOutlineVariant, 0xFF3A3C48);
    }

    private static int outline(@NotNull Context context) {
        return attr(context, R.attr.colorOutline, 0xFF968F8E);
    }

    public static int error(@NotNull Context context) {
        return Accent.danger();
    }

    public static int controlHighlight(@NotNull Context context) {
        return attr(context, R.attr.colorControlHighlight, 0x1FFFFFFF);
    }

    public static @NotNull TextView text(@NotNull Context context, @NotNull CharSequence text, float size, int color) {
        TextView view = new TextView(context);
        view.setText(text);
        view.setTextSize(size);
        view.setTextColor(color);
        return view;
    }

    public static @NotNull TextView heading(@NotNull Context context, @NotNull CharSequence text, float size, int color) {
        TextView view = text(context, text, size, color);
        view.setTextStyle(TextPaint.BOLD);
        return view;
    }

    public static @NotNull LinearLayout card(@NotNull Context context) {
        LinearLayout layout = column(context);

        ShapeDrawable background = new ShapeDrawable();
        background.setCornerRadius(dp(context, 12));
        background.setColor(ColorStateList.modulateColor(surfaceContainerLow(context), 0.72F));
        background.setStroke(Math.max(1, dp(context, 1)),
                ColorStateList.modulateColor(outlineVariant(context), 0.55F));
        layout.setBackground(background);

        int padding = dp(context, 12);
        layout.setPadding(padding, padding, padding, padding);
        return layout;
    }

    public static @NotNull LinearLayout column(@NotNull Context context) {
        LinearLayout layout = new LinearLayout(context);
        layout.setOrientation(LinearLayout.VERTICAL);
        return layout;
    }

    public static @NotNull LinearLayout row(@NotNull Context context) {
        LinearLayout layout = new LinearLayout(context);
        layout.setOrientation(LinearLayout.HORIZONTAL);
        layout.setGravity(Gravity.CENTER_VERTICAL);
        layout.setMinimumHeight(dp(context, 44));
        layout.setPadding(dp(context, 2), dp(context, 6), dp(context, 2), dp(context, 6));
        return layout;
    }

    public static @NotNull LinearLayout.LayoutParams wrap() {
        return new LinearLayout.LayoutParams(WRAP, WRAP);
    }

    public static @NotNull LinearLayout.LayoutParams fill() {
        return new LinearLayout.LayoutParams(MATCH, WRAP);
    }

    public static @NotNull LinearLayout.LayoutParams weight() {
        return new LinearLayout.LayoutParams(0, WRAP, 1.0F);
    }

    /**
     * Creates a switch whose track and thumb follow the selected overlay theme instead of
     * the Modern UI fallback accent.
     */
    public static @NotNull Switch switchControl(@NotNull Context context) {
        Switch toggle = new Switch(context);

        int accent = Accent.primary();

        ShapeDrawable track = new ShapeDrawable();
        track.setShape(ShapeDrawable.RECTANGLE);
        track.setSize(dp(context, 52), dp(context, 32));
        track.setCornerRadius(dp(context, 16));
        track.setColor(new ColorStateList(
                new int[][]{
                        new int[]{-R.attr.state_enabled, -R.attr.state_checked},
                        new int[]{-R.attr.state_enabled, R.attr.state_checked},
                        new int[]{R.attr.state_checked},
                        StateSet.WILD_CARD
                },
                new int[]{
                        ColorStateList.modulateColor(surfaceContainerHighest(context), 0.12F),
                        ColorStateList.modulateColor(accent, 0.38F),
                        accent,
                        surfaceContainerHighest(context)
                }));
        track.setStroke(Math.max(1, dp(context, 2)), new ColorStateList(
                new int[][]{
                        new int[]{R.attr.state_checked},
                        new int[]{-R.attr.state_enabled},
                        StateSet.WILD_CARD
                },
                new int[]{
                        0x00000000,
                        ColorStateList.modulateColor(outline(context), 0.12F),
                        outline(context)
                }));
        toggle.setTrackDrawable(track);

        toggle.setThumbTintList(new ColorStateList(
                new int[][]{
                        new int[]{-R.attr.state_enabled, -R.attr.state_checked},
                        new int[]{-R.attr.state_enabled, R.attr.state_checked},
                        new int[]{R.attr.state_checked},
                        StateSet.WILD_CARD
                },
                new int[]{
                        ColorStateList.modulateColor(onSurface(context), 0.38F),
                        surfaceContainer(context),
                        Accent.onPrimary(),
                        onSurfaceVariant(context)
                }));
        return toggle;
    }

    public static @NotNull LinearLayout switchRow(@NotNull Context context, @NotNull CharSequence title,
                                                  @Nullable CharSequence description, boolean value,
                                                  @NotNull Consumer<Boolean> onChange) {
        LinearLayout row = row(context);

        LinearLayout texts = column(context);
        texts.addView(text(context, title, 14, onSurface(context)), fill());
        if (description != null) {
            TextView hint = text(context, description, 11, onSurfaceVariant(context));
            LinearLayout.LayoutParams params = fill();
            params.topMargin = dp(context, 2);
            texts.addView(hint, params);
        }
        row.addView(texts, weight());

        Switch toggle = switchControl(context);
        toggle.setChecked(value);
        toggle.setOnCheckedChangeListener((view, checked) -> onChange.accept(checked));

        LinearLayout.LayoutParams params = wrap();
        params.setMargins(dp(context, 16), 0, 0, 0);
        params.gravity = Gravity.CENTER_VERTICAL;
        row.addView(toggle, params);
        return row;
    }

    public static @NotNull LinearLayout valueRow(@NotNull Context context, @NotNull CharSequence title,
                                                 @NotNull CharSequence value) {
        LinearLayout row = row(context);
        row.addView(text(context, title, 14, onSurface(context)), weight());
        row.addView(text(context, value, 12, onSurfaceVariant(context)), wrap());
        return row;
    }

    public static @NotNull EditText numberField(@NotNull Context context, int value) {
        EditText field = new EditText(context);
        field.setSingleLine();
        field.setTextSize(14);
        field.setTextColor(onSurface(context));
        field.setGravity(Gravity.END);
        field.setText(String.valueOf(value));

        ShapeDrawable background = new ShapeDrawable();
        background.setCornerRadius(dp(context, 8));
        background.setColor(surfaceContainerHighest(context));
        field.setBackground(background);

        int horizontal = dp(context, 10);
        field.setPadding(horizontal, dp(context, 6), horizontal, dp(context, 6));
        field.setFilters((InputFilter) (source, start, end, dest, dstart, dend) -> {
            for (int i = start; i < end; i++) {
                if (!Character.isDigit(source.charAt(i))) return "";
            }
            return null;
        });
        return field;
    }

    public static void bindNumber(@NotNull EditText field, int min, int max, @NotNull IntConsumer onChange) {
        boolean[] guard = new boolean[1];
        field.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                if (guard[0]) return;

                String raw = s.toString();
                if (raw.isEmpty()) return;

                int parsed;
                try {
                    parsed = Integer.parseInt(raw);
                } catch (NumberFormatException exception) {
                    return;
                }

                int value = Math.max(min, Math.min(max, parsed));
                onChange.accept(value);

                if (value != parsed) {
                    guard[0] = true;
                    field.post(() -> {
                        field.setText(String.valueOf(value));
                        guard[0] = false;
                    });
                }
            }
        });
    }

    public static @NotNull SeekBar slider(@NotNull Context context, int max, int progress) {
        SeekBar bar = new SeekBar(context);
        bar.setMin(0);
        bar.setMax(max);
        bar.setProgress(Math.max(0, Math.min(max, progress)));

        int accent = Accent.primary();
        bar.setProgressTintList(ColorStateList.valueOf(accent));
        bar.setProgressBackgroundTintList(ColorStateList.valueOf(Accent.muted()));
        bar.setThumbTintList(ColorStateList.valueOf(accent));
        return bar;
    }

    public static @NotNull TextView chip(@NotNull Context context, @NotNull CharSequence label, boolean checked,
                                         @NotNull Consumer<Boolean> onChange) {
        TextView chip = new TextView(context);
        chip.setText(label);
        chip.setTextSize(11);
        chip.setGravity(Gravity.CENTER);
        int horizontal = dp(context, 10);
        chip.setPadding(horizontal, dp(context, 4), horizontal, dp(context, 4));

        boolean[] state = {checked};
        Runnable apply = () -> {
            ShapeDrawable background = new ShapeDrawable();
            background.setCornerRadius(dp(context, 14));
            background.setColor(state[0]
                    ? ColorStateList.modulateColor(primary(context), 0.18F)
                    : surfaceContainerHighest(context));
            chip.setBackground(background);
            chip.setTextColor(state[0] ? primary(context) : onSurfaceVariant(context));
        };
        apply.run();

        chip.setOnClickListener(view -> {
            state[0] = !state[0];
            apply.run();
            onChange.accept(state[0]);
        });
        return chip;
    }

                                                           public static @NotNull View divider(@NotNull Context context) {
        View view = new View(context);
        view.setBackground(new ColorDrawable(ColorStateList.modulateColor(outlineVariant(context), 0.6F)));
        return view;
    }

    public static @NotNull ShapeDrawable outlinedBackground(@NotNull Context context) {
        ShapeDrawable background = new ShapeDrawable();
        background.setCornerRadius(dp(context, 10));
        background.setColor(0x00000000);
        background.setStroke(Math.max(1, dp(context, 1)), outlineVariant(context));
        return background;
    }

    public static @NotNull Button button(@NotNull Context context, @NotNull CharSequence label, boolean outlined,
                                         @NotNull View.OnClickListener onClick) {
        Button button = outlined
                ? new Button(context, null, R.attr.buttonOutlinedStyle)
                : new Button(context, null, R.attr.buttonTonalStyle);
        button.setText(label);
        button.setTextSize(12);
        button.setMinimumHeight(dp(context, 36));
        button.setOnClickListener(onClick);

        int[][] states = {
                new int[]{-R.attr.state_enabled},
                StateSet.WILD_CARD
        };
        int accent = Accent.primary();

        ShapeDrawable shape = new ShapeDrawable();
        shape.setCornerRadius(dp(context, 18));
        if (outlined) {
            shape.setColor(0x00000000);
            shape.setStroke(Math.max(1, dp(context, 1)), new ColorStateList(states,
                    new int[]{
                            ColorStateList.modulateColor(accent, 0.16F),
                            Accent.outline()
                    }));
            button.setTextColor(new ColorStateList(states,
                    new int[]{
                            ColorStateList.modulateColor(accent, 0.38F),
                            accent
                    }));
        } else {
            shape.setColor(new ColorStateList(states,
                    new int[]{
                            ColorStateList.modulateColor(accent, 0.16F),
                            accent
                    }));
            button.setTextColor(new ColorStateList(states,
                    new int[]{
                            ColorStateList.modulateColor(Accent.onPrimary(), 0.38F),
                            Accent.onPrimary()
                    }));
        }

        button.setBackground(new RippleDrawable(
                ColorStateList.valueOf(outlined
                        ? ColorStateList.modulateColor(accent, 0.18F)
                        : 0x33FFFFFF),
                shape, null));
        return button;
    }
}
