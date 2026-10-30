package me.kall.narutooverlay.config.ui.page;

import icyllis.modernui.core.Context;
import icyllis.modernui.view.Gravity;
import icyllis.modernui.widget.Button;
import icyllis.modernui.widget.LinearLayout;
import icyllis.modernui.widget.Switch;
import me.kall.narutooverlay.card.Card;
import me.kall.narutooverlay.card.CardRegistry;
import me.kall.narutooverlay.card.Field;
import me.kall.narutooverlay.card.FieldConfig;
import me.kall.narutooverlay.config.ConfigSaver;
import me.kall.narutoui.mui.screen.MuiPageContext;
import me.kall.narutoui.mui.screen.MuiScreenHost;
import me.kall.narutoui.mui.screen.MuiSettingsPage;
import me.kall.narutoui.mui.theme.MuiTheme;
import me.kall.narutoui.mui.util.MuiLocalization;
import me.kall.narutoui.mui.widget.MuiChip;
import me.kall.narutoui.mui.widget.MuiCollapsibleCard;
import me.kall.narutoui.mui.widget.MuiControls;
import me.kall.narutoui.mui.widget.MuiLayout;
import me.kall.narutoui.mui.widget.MuiNumberField;
import me.kall.narutoui.mui.widget.MuiTextAction;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.language.I18n;
import org.jetbrains.annotations.NotNull;

import java.util.Locale;

/**
 * Cards page composed from NarutoUI's generic components.
 * <p>
 * The page is split into two small view models instead of static helper methods with many captured
 * locals: {@link CardsView} owns the page-level list, and each {@link CardView} owns one card's
 * header, position editor, fields and reset action. Callbacks are method references where possible,
 * so a card view only captures {@code this}.
 */
public final class ConfigCardsPage {
    private static final String GROUP = "narutooverlay.config.";
    private static final int CARD_TOP_MARGIN = 18;
    private static final int CARD_BOTTOM_MARGIN = 12;
    private static final int PRESET_TOP_MARGIN = 6;
    private static final int RESET_TOP_MARGIN = 10;

    private ConfigCardsPage() {}

    public static void populate(@NotNull MuiSettingsPage page, @NotNull MuiScreenHost host) {
        page.custom(ui -> new CardsView(ui, host).render());
    }

    /**
     * Page-level view model: card order, spacing and the "reset all" action.
     */
    private static final class CardsView {
        private final Context context;
        private final MuiTheme theme;
        private final MuiLayout content;
        private final MuiScreenHost host;
        private boolean first = true;

        CardsView(@NotNull MuiPageContext page, @NotNull MuiScreenHost host) {
            this.context = page.context();
            this.theme = page.theme();
            this.content = page.contentLayout();
            this.host = host;
        }

        void render() {
            for (Card<?> card : CardRegistry.all()) {
                CardView view = new CardView(this, card);
                this.content.add(view.view(), cardParams());
            }

            MuiTextAction resetAll = MuiTextAction.create(this.context, this.theme,
                    loc("resetAll"), this::resetAll);
            this.content.add(resetAll, resetAllParams());
        }

        void changed() {
            ConfigSaver.changed();
        }

        void rebuild() {
            this.host.reloadCurrentPage();
        }

        private void resetAll() {
            CardRegistry.resetAll();
            this.changed();
            this.rebuild();
        }

        private @NotNull LinearLayout.LayoutParams cardParams() {
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    MuiControls.MATCH, MuiControls.WRAP);
            params.setMargins(0, this.first ? dp(this.context, CARD_TOP_MARGIN) : 0, 0,
                    dp(this.context, CARD_BOTTOM_MARGIN));
            this.first = false;
            return params;
        }

        private @NotNull LinearLayout.LayoutParams resetAllParams() {
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    MuiControls.WRAP, MuiControls.WRAP);
            params.topMargin = dp(this.context, 20);
            return params;
        }
    }

    /**
     * One card: collapsible header, position editor, field rows and card reset.
     */
    private static final class CardView {
        private final CardsView parent;
        private final Context context;
        private final MuiTheme theme;
        private final Card<?> card;
        private final MuiCollapsibleCard box;
        private final MuiLayout body;
        private final MuiNumberField x;
        private final MuiNumberField y;

        CardView(@NotNull CardsView parent, @NotNull Card<?> card) {
            this.parent = parent;
            this.context = parent.context;
            this.theme = parent.theme;
            this.card = card;

            this.box = MuiCollapsibleCard.card(this.context, this.theme,
                    cardName(card), summary(card), false);
            this.body = this.box.body();
            this.x = MuiNumberField.create(this.context, this.theme, card.leftValue().get())
                    .bounds(0, Integer.MAX_VALUE)
                    .gravity(Gravity.CENTER);
            this.y = MuiNumberField.create(this.context, this.theme, card.topValue().get())
                    .bounds(0, Integer.MAX_VALUE)
                    .gravity(Gravity.CENTER);

            this.renderHeader();
            this.renderPosition();
            this.body.divider();
            this.renderFields();
            this.renderReset();
        }

        @NotNull MuiCollapsibleCard view() {
            return this.box;
        }

        void changed() {
            this.parent.changed();
        }

        private void renderHeader() {
            Switch toggle = MuiControls.switchControl(this.context, this.theme);
            toggle.setChecked(this.card.enabledValue().get());
            toggle.setContentDescription(loc("cards.enabled"));
            toggle.setOnCheckedChangeListener((view, checked) -> this.setEnabled(checked));
            this.box.trailing(toggle);
        }

        private void renderPosition() {
            this.x.onChange(this::setX);
            this.y.onChange(this::setY);

            MuiLayout row = MuiLayout.row(this.context, this.theme).padding(0)
                    .add(MuiControls.text(this.context, this.theme, "X", 12, this.theme.onSurfaceVariant()))
                    .add(this.x, fieldParams(this.context))
                    .add(MuiControls.text(this.context, this.theme, "Y", 12, this.theme.onSurfaceVariant()), yLabelParams(this.context))
                    .add(this.y, fieldParams(this.context))
                    .flexibleSpace()
                    .add(MuiTextAction.create(this.context, this.theme, loc("cards.resetPosition"), this::resetPosition));
            this.body.add(row);

            MuiLayout presets = MuiLayout.row(this.context, this.theme).padding(0).spacing(6);
            presets.addWeighted(this.presetButton("cards.topLeft", 0, 0), 1.0F)
                    .addWeighted(this.presetButton("cards.topRight", 1, 0), 1.0F)
                    .addWeighted(this.presetButton("cards.bottomLeft", 0, 1), 1.0F)
                    .addWeighted(this.presetButton("cards.bottomRight", 1, 1), 1.0F);

            LinearLayout.LayoutParams presetParams = new LinearLayout.LayoutParams(MuiControls.MATCH, MuiControls.WRAP);
            presetParams.topMargin = dp(this.context, PRESET_TOP_MARGIN);
            this.body.add(presets, presetParams);
        }

        private @NotNull Button presetButton(@NotNull String labelKey, int horizontal, int vertical) {
            return MuiControls.button(this.context, this.theme, loc(labelKey), true, view -> this.preset(horizontal, vertical));
        }

        private void renderFields() {
            boolean first = true;
            for (Field<?> field : this.card.allFields()) {
                if (!first) this.body.divider();
                first = false;
                this.body.add(new FieldView(this, field).row());
            }
        }

        private void renderReset() {
            MuiTextAction reset = MuiTextAction.create(this.context, this.theme, loc("cards.resetCard"), this::resetCard);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(MuiControls.WRAP, MuiControls.WRAP);
            params.topMargin = dp(this.context, RESET_TOP_MARGIN);
            this.body.add(reset, params);
        }

        private void setEnabled(boolean enabled) {
            this.card.enabledValue().set(enabled);
            this.changed();
        }

        private void setX(int value) {
            this.card.leftValue().set(value);
            this.changed();
            this.refreshSummary();
        }

        private void setY(int value) {
            this.card.topValue().set(value);
            this.changed();
            this.refreshSummary();
        }

        private void resetPosition() {
            this.card.resetPosition();
            this.changed();
            this.syncPosition();
        }

        private void syncPosition() {
            this.x.value(this.card.leftValue().get());
            this.y.value(this.card.topValue().get());
            this.refreshSummary();
        }

        private void preset(int horizontal, int vertical) {
            Minecraft minecraft = Minecraft.getInstance();
            int width = this.card.lineWidth();
            if (width <= 0) width = 160;
            int height = minecraft.font.lineHeight;
            int margin = 8;
            int left = horizontal == 0 ? margin : minecraft.getWindow().getGuiScaledWidth() - width - margin;
            int top = vertical == 0 ? margin : minecraft.getWindow().getGuiScaledHeight() - height - margin;
            this.card.moveTo(left, top);
            this.changed();
            this.syncPosition();
        }

        private void resetCard() {
            this.card.resetAll();
            this.changed();
            this.parent.rebuild();
        }

        private void refreshSummary() {
            this.box.summary(summary(this.card));
        }
    }

    /**
     * One field row: title, optional label/unit chips, enabled switch.
     */
    private static final class FieldView {
        private final CardView parent;
        private final Field<?> field;
        private final FieldConfig config;
        private final MuiLayout row;

        FieldView(@NotNull CardView parent, @NotNull Field<?> field) {
            this.parent = parent;
            this.field = field;
            this.config = parent.card.fieldConfig(field.id());
            this.row = this.buildRow();
        }

        @NotNull MuiLayout row() {
            return this.row;
        }

        private @NotNull MuiLayout buildRow() {
            MuiLayout row = MuiLayout.row(this.parent.context, this.parent.theme).padding(0);
            row.add(MuiControls.text(this.parent.context, this.parent.theme, fieldName(this.field), 13, this.parent.theme.onSurface()), MuiControls.weight());

            if (this.field.hasLabel()) {
                row.add(MuiChip.create(this.parent.context, this.parent.theme, loc("chip.label"), this.config.showLabel()).onChanged(this::setLabel), chipParams(this.parent.context));
            }
            if (this.field.hasUnit()) {
                row.add(MuiChip.create(this.parent.context, this.parent.theme, loc("chip.unit"), this.config.showUnit()).onChanged(this::setUnit), chipParams(this.parent.context));
            }

            Switch toggle = MuiControls.switchControl(this.parent.context, this.parent.theme);
            toggle.setChecked(this.config.enabled());
            toggle.setOnCheckedChangeListener((view, checked) -> this.setEnabled(checked));
            LinearLayout.LayoutParams toggleParams = MuiControls.wrap();
            toggleParams.setMargins(dp(this.parent.context, 10), 0, 0, 0);
            row.add(toggle, toggleParams);
            return row;
        }

        private void setLabel(boolean checked) {
            if (this.config.showLabelValue() != null) this.config.showLabelValue().set(checked);
            this.parent.changed();
        }

        private void setUnit(boolean checked) {
            if (this.config.showUnitValue() != null) this.config.showUnitValue().set(checked);
            this.parent.changed();
        }

        private void setEnabled(boolean enabled) {
            this.config.enabledValue().set(enabled);
            this.parent.changed();
            this.parent.refreshSummary();
        }
    }

    private static @NotNull LinearLayout.LayoutParams fieldParams(@NotNull Context context) {
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(dp(context, 64), MuiControls.WRAP);
        params.setMargins(dp(context, 6), 0, 0, 0);
        return params;
    }

    private static @NotNull LinearLayout.LayoutParams yLabelParams(@NotNull Context context) {
        LinearLayout.LayoutParams params = MuiControls.wrap();
        params.setMargins(dp(context, 14), 0, 0, 0);
        return params;
    }

    private static @NotNull LinearLayout.LayoutParams chipParams(@NotNull Context context) {
        LinearLayout.LayoutParams params = MuiControls.wrap();
        params.setMargins(dp(context, 8), 0, 0, 0);
        return params;
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
        return I18n.get(GROUP + "cards.summary", visible, card.allFields().size(), card.leftValue().get(), card.topValue().get());
    }

    private static @NotNull String loc(@NotNull String suffix) {
        return MuiLocalization.get(GROUP + suffix);
    }

    private static int dp(@NotNull Context context, int value) {
        return MuiControls.dp(context, value);
    }
}