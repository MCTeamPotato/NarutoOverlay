package me.kall.narutooverlay.config.ui.page;

import me.kall.narutoui.mui.MuiLocalization;

import icyllis.modernui.widget.TextView;
import me.kall.narutoui.mui.MuiCardEditor;
import me.kall.narutoui.mui.MuiControls;
import me.kall.narutoui.mui.MuiSettingsPage;
import me.kall.narutooverlay.card.Card;
import me.kall.narutooverlay.card.CardRegistry;
import me.kall.narutooverlay.card.Field;
import me.kall.narutooverlay.card.FieldConfig;
import me.kall.narutooverlay.config.ConfigSaver;
import me.kall.narutooverlay.config.ui.ConfigUiHost;
import me.kall.narutooverlay.config.ui.widget.Accent;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.language.I18n;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Cards page expressed through {@link MuiCardEditor}: the page only adapts card config to the
 * editor model; section/header/row/divider/preset layout lives in the API.
 */
public final class ConfigCardsPage {
    private static final String GROUP = "narutooverlay.config.";

    private ConfigCardsPage() {}

    public static void populate(@NotNull MuiSettingsPage page) {
        List<MuiCardEditor.Model> models = new ArrayList<>(CardRegistry.all().size());
        for (Card<?> card : CardRegistry.all()) models.add(new CardModel(card));

        MuiCardEditor.Labels labels = new MuiCardEditor.Labels(
                MuiLocalization.get(GROUP + "cards.enabled"),
                MuiLocalization.get(GROUP + "cards.position"),
                MuiLocalization.get(GROUP + "cards.resetPosition"),
                MuiLocalization.get(GROUP + "cards.resetCard"),
                MuiLocalization.get(GROUP + "chip.label"),
                MuiLocalization.get(GROUP + "chip.unit"),
                MuiLocalization.get(GROUP + "cards.topLeft"),
                MuiLocalization.get(GROUP + "cards.topRight"),
                MuiLocalization.get(GROUP + "cards.bottomLeft"),
                MuiLocalization.get(GROUP + "cards.bottomRight")
        );

        MuiCardEditor.add(page, models, labels, ConfigSaver::changed);

        page.custom(context -> {
            TextView reset = MuiControls.text(context.context(), context.theme(), MuiLocalization.get(GROUP + "resetAll"), 12, Accent.danger());
            reset.setPadding(0, MuiControls.dp(context.context(), 20), 0, 0);
            reset.setOnClickListener(view -> {
                CardRegistry.resetAll();
                ConfigSaver.changed();
                ConfigUiHost host = ConfigUiHost.active();
                if (host != null) host.reloadCurrentPage();
            });
            context.content().addView(reset, MuiControls.fill());
        });
    }

    private record CardModel(Card<?> card) implements MuiCardEditor.Model {
        @Override
        public @NotNull String title() {
            String key = GROUP + "card." + this.card.id().toLowerCase(Locale.ROOT);
            return I18n.exists(key) ? I18n.get(key) : this.card.id();
        }

        @Override
        public @NotNull String summary() {
            int visible = 0;
            for (Field<?> field : this.card.allFields()) {
                if (this.card.fieldConfig(field.id()).enabled()) visible++;
            }
            return I18n.get(GROUP + "cards.summary", visible, this.card.allFields().size(),
                    this.card.leftValue().get(), this.card.topValue().get());
        }

        @Override
        public boolean enabled() {
            return this.card.enabledValue().get();
        }

        @Override
        public void enabled(boolean value) {
            this.card.enabledValue().set(value);
        }

        @Override
        public int x() {
            return this.card.leftValue().get();
        }

        @Override
        public int y() {
            return this.card.topValue().get();
        }

        @Override
        public void position(int x, int y) {
            this.card.leftValue().set(x);
            this.card.topValue().set(y);
        }

        @Override
        public void resetPosition() {
            this.card.resetPosition();
        }

        @Override
        public void preset(int horizontal, int vertical) {
            Minecraft minecraft = Minecraft.getInstance();
            int width = this.card.lineWidth();
            int height = minecraft.font.lineHeight;
            int left = horizontal == 0 ? 0 : Math.max(0, minecraft.getWindow().getGuiScaledWidth() - width - 4);
            int top = vertical == 0 ? 0 : Math.max(0, minecraft.getWindow().getGuiScaledHeight() - height - 4);
            this.card.moveTo(left, top);
        }

        @Override
        public @NotNull List<MuiCardEditor.Field> fields() {
            List<MuiCardEditor.Field> fields = new ArrayList<>(this.card.allFields().size());
            for (Field<?> field : this.card.allFields()) fields.add(new FieldModel(this.card, field));
            return fields;
        }

        @Override
        public void reset() {
            this.card.resetAll();
        }
    }

    private record FieldModel(Card<?> card, Field<?> field) implements MuiCardEditor.Field {
        @Override
        public @NotNull String title() {
            String key = GROUP + "field." + this.field.id().toLowerCase(Locale.ROOT);
            return I18n.exists(key) ? I18n.get(key) : this.field.id();
        }

        @Override
        public boolean enabled() {
            return this.config().enabled();
        }

        @Override
        public void enabled(boolean value) {
            this.config().enabledValue().set(value);
        }

        @Override
        public boolean hasLabel() {
            return this.field.hasLabel();
        }

        @Override
        public boolean label() {
            return this.config().showLabel();
        }

        @Override
        public void label(boolean value) {
            if (this.config().showLabelValue() != null) this.config().showLabelValue().set(value);
        }

        @Override
        public boolean hasUnit() {
            return this.field.hasUnit();
        }

        @Override
        public boolean unit() {
            return this.config().showUnit();
        }

        @Override
        public void unit(boolean value) {
            if (this.config().showUnitValue() != null) this.config().showUnitValue().set(value);
        }

        private @NotNull FieldConfig config() {
            return this.card.fieldConfig(this.field.id());
        }
    }
}