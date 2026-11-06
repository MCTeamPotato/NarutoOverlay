package me.kall.narutooverlay.config.ui;

import icyllis.modernui.core.Context;
import icyllis.modernui.graphics.Image;
import icyllis.modernui.view.Gravity;
import icyllis.modernui.view.View;
import icyllis.modernui.view.ViewGroup;
import icyllis.modernui.widget.*;
import me.kall.narutooverlay.NarutoOverlay;
import me.kall.narutooverlay.card.Card;
import me.kall.narutooverlay.card.CardRegistry;
import me.kall.narutooverlay.card.Field;
import me.kall.narutooverlay.card.FieldConfig;
import me.kall.narutooverlay.config.ConfigSaver;
import me.kall.narutooverlay.config.RenderArgs;
import me.kall.narutooverlay.config.ui.background.NarutoJarResources;
import me.kall.narutooverlay.event.TextRelocation;
import me.kall.narutooverlay.theme.Palette;
import me.kall.narutooverlay.theme.Theme;
import me.kall.narutooverlay.theme.ThemeColors;
import me.kall.narutoui.mui.background.*;
import me.kall.narutoui.mui.choice.MuiChoiceGrid;
import me.kall.narutoui.mui.choice.MuiDropdown;
import me.kall.narutoui.mui.choice.MuiOption;
import me.kall.narutoui.mui.factory.MuiWidgetFactory;
import me.kall.narutoui.mui.field.MuiListEditor;
import me.kall.narutoui.mui.field.MuiNumberField;
import me.kall.narutoui.mui.field.MuiTextField;
import me.kall.narutoui.mui.layout.MuiLayout;
import me.kall.narutoui.mui.overlay.MuiColorPickerOverlay;
import me.kall.narutoui.mui.overlay.MuiCropOverlay;
import me.kall.narutoui.mui.overlay.MuiCropOverlayFactory;
import me.kall.narutoui.mui.screen.*;
import me.kall.narutoui.mui.shimmer.MuiShimmer;
import me.kall.narutoui.mui.shimmer.MuiShimmerConfig;
import me.kall.narutoui.mui.shimmer.MuiShimmerProfile;
import me.kall.narutoui.mui.shimmer.MuiShimmerable;
import me.kall.narutoui.mui.theme.MuiTheme;
import me.kall.narutoui.mui.widget.*;
import net.minecraft.SharedConstants;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.ChatScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.resources.language.I18n;
import net.minecraftforge.client.ConfigScreenHandler;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.loading.FMLPaths;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.glfw.GLFW;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.function.IntConsumer;
import java.util.function.Supplier;

import static me.kall.narutooverlay.config.RenderArgs.*;
import static me.kall.narutoui.mui.layout.MuiControls.*;

public final class NarutoConfigScreen {
    private static final MuiConfigurableBackground BACKGROUND = MuiConfigurableBackground
            .builder(
                    new NarutoBackgroundSettings(),
                    new MuiBackgroundLabels(
                            "narutooverlay.config.background.picker",
                            "narutooverlay.config.background.filter",
                            "narutooverlay.config.background.pickerFailed",
                            "narutooverlay.config.background.folder.picker",
                            "narutooverlay.config.background.folder.failed",
                            "narutooverlay.config.background.failed",
                            "narutooverlay.config.background.saved",
                            new MuiCropOverlay.Labels(
                                    "narutooverlay.config.background.crop.title",
                                    "narutooverlay.config.background.crop.hint",
                                    "narutooverlay.config.background.confirm",
                                    "narutooverlay.config.background.cancel"
                            )
                    ),
                    new MuiBackgroundFiles(FMLPaths.GAMEDIR.get(), "narutooverlay-config-bg")
            )
            .cropFactory(new NarutoCropFactory())
            .build();

    private static final NarutoScreenHost HOST = new NarutoScreenHost();

    public static void register(@NotNull FMLJavaModLoadingContext context) {
        NarutoJarResources.install();
        context.registerExtensionPoint(ConfigScreenHandler.ConfigScreenFactory.class, () -> new ConfigScreenHandler.ConfigScreenFactory((minecraft, screen) -> build(screen)));
        MinecraftForge.EVENT_BUS.addListener(NarutoConfigScreen::onMouseButton);
    }

    private static @NotNull Screen build(@Nullable Screen previous) {
        return MuiSettingsScreen.builder("NarutoOverlay")
                .widgetFactory(NarutoShimmerFactory.INSTANCE)
                .shimmerProfile(NarutoShimmerConfig.PROFILE)
                .background(BACKGROUND)
                .initialPage(0)
                .shimmerNavigation()
                .shimmerTitles()
                .themeProvider(context -> {
                    MuiTheme base = MuiTheme.modern(context);
                    return base.withPrimary(NarutoAccent.primary()).withOnPrimary(NarutoAccent.onPrimary());
                })
                .railHeader(context -> MuiLogo.create(context, MuiTheme.modern(context), "N").color(NarutoAccent.primary()).textColor(NarutoAccent.onPrimary()))
                .railFooter(context -> MuiHintBadge.create(context, MuiTheme.modern(context), "narutooverlay.config.footer.escHint").color(NarutoAccent.primary()))
                .onCreated(HOST::attach)
                .onDestroyed(fragment -> HOST.attach(null))
                .page("narutooverlay.config.nav.cards", "narutooverlay.config.cards.title", "narutooverlay.config.cards.subtitle", NarutoConfigScreen::buildCards)
                .page("narutooverlay.config.nav.appearance", "narutooverlay.config.appearance.title", "narutooverlay.config.appearance.subtitle", NarutoConfigScreen::buildAppearance)
                .page("narutooverlay.config.nav.about", "narutooverlay.config.about.title", "narutooverlay.config.about.subtitle", NarutoConfigScreen::buildAbout)
                .create(previous);
    }

    private static void buildCards(@NotNull MuiSettingsPage page) {
        page.shimmerAll().custom(ui -> {
            Context context = ui.context();
            MuiTheme theme = ui.theme();
            MuiLayout content = ui.contentLayout();
            boolean firstCard = true;

            for (Card<?> card : CardRegistry.all()) {
                String cardID = card.id().toLowerCase(Locale.ROOT);

                MuiCollapsibleCard box = MuiCollapsibleCard.card(context, theme, "narutooverlay.config.card." + cardID, "narutooverlay.config.card." + cardID + ".summary", false);
                MuiLayout body = box.body();

                MuiNumberField x = MuiNumberField.create(context, theme, card.left()).bounds(0, Integer.MAX_VALUE).gravity(Gravity.CENTER).onChangeInt(card::setLeft);
                MuiNumberField y = MuiNumberField.create(context, theme, card.top()).bounds(0, Integer.MAX_VALUE).gravity(Gravity.CENTER).onChangeInt(card::setTop);

                Switch cardToggle = switchControl(context, theme);
                cardToggle.setChecked(card.enabledValue().get());
                cardToggle.setContentDescription("narutooverlay.config.cards.enabled");
                cardToggle.setOnCheckedChangeListener((buttonView, isChecked) -> card.setEnabled(isChecked));

                box.trailing(cardToggle);

                Runnable sync = () -> {
                    x.value(card.left());
                    y.value(card.top());
                };

                LinearLayout.LayoutParams yLabelParams = wrap();
                yLabelParams.setMargins(dp(context, 14), 0, 0, 0);

                LinearLayout.LayoutParams fieldParams = new LinearLayout.LayoutParams(dp(context, 64), WRAP);
                fieldParams.setMargins(dp(context, 6), 0, 0, 0);

                MuiLayout cardRow = MuiLayout.row(context, theme).padding(0)
                        .add(text(context, theme, "X", 12, theme.onSurfaceVariant()))
                        .add(x, fieldParams)
                        .add(text(context, theme, "Y", 12, theme.onSurfaceVariant()), yLabelParams)
                        .add(y, fieldParams)
                        .flexibleSpace()
                        .add(MuiTextAction.create(context, theme, "narutooverlay.config.cards.resetPosition", () -> card.resetPosition(sync)));
                body.add(cardRow);

                int screenWidth = Minecraft.getInstance().getWindow().getGuiScaledWidth();
                int screenHeight = Minecraft.getInstance().getWindow().getGuiScaledHeight();
                int right = Math.max(8, screenWidth - card.lineWidth() - 8);
                int bottom = Math.max(8, screenHeight - Minecraft.getInstance().font.lineHeight - 8);

                MuiLayout presets = MuiLayout.row(context, theme)
                        .padding(0)
                        .spacing(6)
                        .addWeighted(button(context, theme, "narutooverlay.config.cards.topLeft", true, view -> card.moveTo(8, 8, sync)), 1.0F)
                        .addWeighted(button(context, theme, "narutooverlay.config.cards.topRight", true, view -> card.moveTo(right, 8, sync)), 1.0F)
                        .addWeighted(button(context, theme, "narutooverlay.config.cards.bottomLeft", true, view -> card.moveTo(8, bottom, sync)), 1.0F)
                        .addWeighted(button(context, theme, "narutooverlay.config.cards.bottomRight", true, view -> card.moveTo(right, bottom, sync)), 1.0F);

                LinearLayout.LayoutParams presetParams = new LinearLayout.LayoutParams(MATCH, WRAP);
                presetParams.topMargin = dp(context, 6);

                body.add(presets, presetParams).divider();

                boolean firstField = true;
                for (Field<?> field : card.allFields()) {
                    if (!firstField) body.divider();
                    firstField = false;

                    String fieldID = field.id();

                    FieldConfig config = card.fieldConfig(fieldID);
                    MuiLayout fieldRow = MuiLayout.row(context, theme).padding(0).add(text(context, theme, "narutooverlay.config.field." + fieldID.toLowerCase(Locale.ROOT), 13, theme.onSurface()), weight());

                    LinearLayout.LayoutParams chipParams = wrap();
                    chipParams.setMargins(dp(context, 8), 0, 0, 0);

                    if (field.hasLabel()) fieldRow.add(MuiChip.create(context, theme, "narutooverlay.config.chip.label", config.showLabel()).onChanged(config::setShowLabel), chipParams);
                    if (field.hasUnit()) fieldRow.add(MuiChip.create(context, theme, "narutooverlay.config.chip.unit", config.showUnit()).onChanged(config::setShowUnit), chipParams);

                    Switch fieldToggle = switchControl(context, theme);
                    fieldToggle.setChecked(config.enabled());
                    fieldToggle.setOnCheckedChangeListener((buttonView, isChecked) -> config.setEnabled(isChecked));

                    LinearLayout.LayoutParams toggleParams = wrap();
                    toggleParams.setMargins(dp(context, 10), 0, 0, 0);
                    fieldRow.add(fieldToggle, toggleParams);

                    body.add(fieldRow);
                }

                MuiTextAction resetCard = MuiTextAction.create(context, theme, "narutooverlay.config.cards.resetCard", () -> {
                    card.resetAll();
                    HOST.reloadCurrentPage();
                });

                LinearLayout.LayoutParams resetParams = new LinearLayout.LayoutParams(WRAP, WRAP);
                resetParams.topMargin = dp(context, 10);
                body.add(resetCard, resetParams);

                LinearLayout.LayoutParams boxParams = new LinearLayout.LayoutParams(MATCH, WRAP);
                boxParams.setMargins(0, firstCard ? dp(context, 18) : 0, 0, dp(context, 12));
                firstCard = false;

                content.add(box, boxParams);
            }

            MuiTextAction resetAll = MuiTextAction.create(context, theme, "narutooverlay.config.cards.resetAll", () -> {
                CardRegistry.resetAll();
                ConfigSaver.changed();
                HOST.reloadCurrentPage();
            });

            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(WRAP, WRAP);
            params.topMargin = dp(context, 20);

            content.add(resetAll, params);
        });
    }

    private static void buildAppearance(@NotNull MuiSettingsPage page) {
        page.shimmerAll()
                .section("narutooverlay.config.appearance.theme", section -> section
                        .choiceGridBound("narutooverlay.config.appearance.theme", null,
                                Arrays.stream(Theme.values())
                                        .map(theme -> MuiOption.of(theme, themeLabel(theme))
                                                .preview((context, activeTheme) ->
                                                        new ThemePreview(context, RenderArgs.paletteFor(theme))))
                                        .toList(),
                                THEME, theme -> {
                                    set(THEME, theme);
                                    HOST.onThemeChanged();
                                })
                )
                .section("narutooverlay.config.appearance.custom", section -> {
                    section.note(localized("narutooverlay.config.appearance.custom.note"));
                    section.customLayout(row -> {
                        Context context = row.getContext();
                        row.add(MuiColorField.create(context, MuiTheme.modern(context),
                                        localized("narutooverlay.config.appearance.customAccent")
                                                + "  " + ThemeColors.format(customAccent()),
                                        customAccent())
                                .buttonLabel(localized("narutooverlay.config.appearance.pickColor"))
                                .onPick(() -> pickCustomColor(true)), fill());
                    });
                    section.enabledWhen(() -> THEME.get() == Theme.CUSTOM);
                    section.customLayout(row -> {
                        Context context = row.getContext();
                        row.add(MuiColorField.create(context, MuiTheme.modern(context),
                                        localized("narutooverlay.config.appearance.customBackplane")
                                                + "  " + ThemeColors.format(customBackplane()),
                                        customBackplane())
                                .buttonLabel(localized("narutooverlay.config.appearance.pickColor"))
                                .onPick(() -> pickCustomColor(false)), fill());
                    });
                    section.enabledWhen(() -> THEME.get() == Theme.CUSTOM);
                })
                .section("narutooverlay.config.appearance.display", section -> {
                    section.toggle("narutooverlay.config.appearance.dropShadow", "narutooverlay.config.appearance.dropShadow.desc", DROP_SHADOW::get, value -> set(DROP_SHADOW, value))
                            .toggle("narutooverlay.config.appearance.equalWidth", "narutooverlay.config.appearance.equalWidth.desc", EQUAL_WIDTH::get, value -> set(EQUAL_WIDTH, value))
                            .toggle("narutooverlay.config.appearance.drawBackplane", "narutooverlay.config.appearance.drawBackplane.desc", DRAW_BACKPLANE::get, value -> set(DRAW_BACKPLANE, value))
                            .toggle("narutooverlay.config.appearance.shimmer", "narutooverlay.config.appearance.shimmer.desc", SHIMMER::get, value -> {
                                set(SHIMMER, value);
                                MuiShimmer.refresh();
                            });
                    if (!RenderArgs.palette().hasAccent()) disableLastRow(section);
                })
                .section("narutooverlay.config.appearance.shimmer", section -> {
                    section.slider("narutooverlay.config.appearance.shimmerSpeed", "narutooverlay.config.appearance.shimmerSpeed.desc", 0.2D, 3.0D, 0.1D, SHIMMER_SPEED::get, value -> set(SHIMMER_SPEED, value), value -> String.format(Locale.ROOT, "%.1f", value))
                            .slider("narutooverlay.config.appearance.shimmerDensity", "narutooverlay.config.appearance.shimmerDensity.desc", 0.2D, 3.0D, 0.1D, SHIMMER_SCALE::get, value -> set(SHIMMER_SCALE, value), value -> String.format(Locale.ROOT, "%.1f", value))
                            .slider("narutooverlay.config.appearance.shimmerStrength", "narutooverlay.config.appearance.shimmerStrength.desc", 0.0D, 1.0D, 0.1D, SHIMMER_STRENGTH::get, value -> set(SHIMMER_STRENGTH, value), value -> String.format(Locale.ROOT, "%.1f", value))
                            .slider("narutooverlay.config.appearance.shimmerFps", "narutooverlay.config.appearance.shimmerFps.desc", 5, 240, SHIMMER_FPS::get, value -> set(SHIMMER_FPS, value), Integer::toString)
                            .slider("narutooverlay.config.appearance.configShimmerFps", "narutooverlay.config.appearance.configShimmerFps.desc", 5, 120, CONFIG_SHIMMER_FPS::get, value -> set(CONFIG_SHIMMER_FPS, value), Integer::toString)
                            .toggle("narutooverlay.config.appearance.cacheShimmer", "narutooverlay.config.appearance.cacheShimmer.desc", CACHE_SHIMMER::get, value -> set(CACHE_SHIMMER, value));

                    if (!RenderArgs.palette().hasAccent()) {
                        disableAllRows(section);
                        section.note("narutooverlay.config.appearance.shimmer.unavailable");
                    }
                })
                .section("narutooverlay.config.appearance.backplane", section -> section
                        .slider("narutooverlay.config.appearance.borderWidth", "narutooverlay.config.appearance.borderWidth.desc", 0.0D, 4.0D, 0.1D, BACKPLANE_BORDER_WIDTH::get, value -> set(BACKPLANE_BORDER_WIDTH, value), value -> String.format(Locale.ROOT, "%.1f", value))
                        .toggle("narutooverlay.config.appearance.borderOverride", "narutooverlay.config.appearance.borderOverride.desc", BACKPLANE_BORDER_OVERRIDE::get, value -> set(BACKPLANE_BORDER_OVERRIDE, value))
                        .customLayout(row -> {
                            Context context = row.getContext();
                            row.add(MuiColorField.create(context, MuiTheme.modern(context), "narutooverlay.config.appearance.borderColor", borderColor())
                                    .buttonLabel("narutooverlay.config.appearance.pickColor")
                                    .onPick(() -> {
                                        Context pickerContext = HOST.requireContext();
                                        MuiShimmerProfile.withCurrent(
                                                NarutoShimmerConfig.PROFILE,
                                                () -> withFactory(
                                                        NarutoShimmerFactory.INSTANCE,
                                                        () -> HOST.showOverlay(
                                                                new MuiColorPickerOverlay(
                                                                        pickerContext,
                                                                        MuiTheme.modern(pickerContext).withPrimary(NarutoAccent.primary()),
                                                                        borderColor(),
                                                                        new MuiColorPickerOverlay.Labels(
                                                                                "narutooverlay.config.color.title",
                                                                                "narutooverlay.config.color.brightness",
                                                                                "narutooverlay.config.color.alpha",
                                                                                "narutooverlay.config.color.apply",
                                                                                "narutooverlay.config.color.cancel"
                                                                        ),
                                                                        argb -> {
                                                                            BACKPLANE_BORDER_OVERRIDE.set(true);
                                                                            BACKPLANE_BORDER_RED.set((argb >> 16) & 0xFF);
                                                                            BACKPLANE_BORDER_GREEN.set((argb >> 8) & 0xFF);
                                                                            BACKPLANE_BORDER_BLUE.set(argb & 0xFF);
                                                                            BACKPLANE_BORDER_ALPHA.set((argb >>> 24) & 0xFF);
                                                                            ConfigSaver.changed();
                                                                            HOST.reloadCurrentPage();
                                                                        })
                                                        )
                                                )
                                        );
                                    }), fill());
                        })
                )
                .section("narutooverlay.config.appearance.performance", section -> section
                        .number("narutooverlay.config.appearance.fpsInterval", "narutooverlay.config.appearance.fpsInterval.desc", 1, 200, FPS_COOLDOWN::get, value -> set(FPS_COOLDOWN, value))
                        .number("narutooverlay.config.appearance.gcInterval", "narutooverlay.config.appearance.gcInterval.desc", 1, 200, GC_COOLDOWN::get, value -> set(GC_COOLDOWN, value))
                        .number("narutooverlay.config.appearance.ramInterval", "narutooverlay.config.appearance.ramInterval.desc", 1, 200, RAM_COOLDOWN::get, value -> set(RAM_COOLDOWN, value))
                        .number("narutooverlay.config.appearance.fpsWindow", "narutooverlay.config.appearance.fpsWindow.desc", 10, 1000, FPS_WINDOW::get, value -> set(FPS_WINDOW, value))
                )
        ;

    }

    private static @NotNull String themeLabel(@NotNull Theme theme) {
        String key = "narutooverlay.theme." + theme.name().toLowerCase(Locale.ROOT);
        String translated = I18n.get(key);
        return translated.equals(key) ? theme.name() : translated;
    }

    private static @NotNull String localized(@NotNull String key) {
        return I18n.get(key);
    }

    private static void pickCustomColor(boolean accent) {
        String title = localized(accent
                ? "narutooverlay.config.appearance.customAccent"
                : "narutooverlay.config.appearance.customBackplane");
        openColorPicker(title, accent ? customAccent() : customBackplane(), argb -> {
            if (accent) {
                CUSTOM_ACCENT.set(ThemeColors.toColorToken(ThemeColors.opaque(argb)));
            } else {
                CUSTOM_BACKPLANE.set(ThemeColors.toColorToken(argb));
            }
        });
    }

    private static void openColorPicker(@NotNull String title, int initial, @NotNull IntConsumer onPicked) {
        Context context = HOST.requireContext();
        MuiShimmerProfile.withCurrent(NarutoShimmerConfig.PROFILE, () ->
                withFactory(NarutoShimmerFactory.INSTANCE, () ->
                        HOST.showOverlay(new MuiColorPickerOverlay(
                                context,
                                MuiTheme.modern(context).withPrimary(NarutoAccent.primary()),
                                initial,
                                new MuiColorPickerOverlay.Labels(
                                        title,
                                        localized("narutooverlay.config.color.brightness"),
                                        localized("narutooverlay.config.color.alpha"),
                                        localized("narutooverlay.config.color.apply"),
                                        localized("narutooverlay.config.color.cancel")),
                                argb -> {
                                    onPicked.accept(argb);
                                    ConfigSaver.changed();
                                    HOST.reloadCurrentPage();
                                }))));
    }

    private static void disableLastRow(@NotNull MuiSettingsSection section) {
        LinearLayout card = section.card();
        if (card.getChildCount() == 0) return;
        setEnabledRecursive(card.getChildAt(card.getChildCount() - 1), false);
    }

    private static void disableAllRows(@NotNull MuiSettingsSection section) {
        LinearLayout card = section.card();
        for (int i = 0; i < card.getChildCount(); i++) {
            setEnabledRecursive(card.getChildAt(i), false);
        }
    }

    private static void setEnabledRecursive(@NotNull View view, boolean enabled) {
        view.setEnabled(enabled);
        if (view instanceof ViewGroup group) {
            for (int i = 0; i < group.getChildCount(); i++) {
                setEnabledRecursive(group.getChildAt(i), enabled);
            }
        }
    }

    private static void buildAbout(@NotNull MuiSettingsPage page) {
        Path path = FMLPaths.CONFIGDIR.get().resolve("narutooverlay-client.toml");

        page.shimmerAll()
                .section("narutooverlay.config.about.info", section -> section
                        .value("narutooverlay.config.about.version", ModList.get().getModContainerById(NarutoOverlay.MOD_ID)
                                .map(modContainer -> modContainer.getModInfo().getVersion().toString())
                                .orElse("?")
                        )
                        .value("narutooverlay.config.about.environment", NarutoOverlay.ENV + " · " + SharedConstants.getCurrentVersion().getName() + " · " + I18n.get("narutooverlay.config.about.clientOnly"))
                )
                .section("narutooverlay.config.about.background", section -> {
                    String folder = BACKGROUND.state().folder();
                    int count = BACKGROUND.images().size();

                    section
                            .value("narutooverlay.config.background.folder", folder.isBlank() ? I18n.get("narutooverlay.config.background.folder.none") : folder)
                            .text("narutooverlay.config.background.folder.hint")
                            .customLayout(row -> {
                                Context context = row.getContext();
                                MuiTheme theme = row.theme();

                                Button choose = button(context, theme, "narutooverlay.config.background.folder.choose", true, view -> BACKGROUND.chooseBackgroundFolder());
                                Button clear = button(context, theme, "narutooverlay.config.background.folder.clear", true, view -> BACKGROUND.clearBackgroundFolder());
                                clear.setEnabled(!folder.isBlank());

                                row.add(
                                        MuiLayout
                                                .row(context, theme)
                                                .padding(0)
                                                .space(8)
                                                .addWeighted(choose, 1.0F)
                                                .addWeighted(clear, 1.0F)
                                        , fill()
                                );
                            })
                            .value("narutooverlay.config.background.images", count == 0 ? I18n.get("narutooverlay.config.background.images.empty") : String.valueOf(count))
                            .slider("narutooverlay.config.background.interval", "narutooverlay.config.background.interval.desc", 2.0D, 300.0D, 0.1D, BACKGROUND.state()::interval, BACKGROUND::setBackgroundInterval, value -> String.format(Locale.ROOT, "%.1f", value))
                            .slider("narutooverlay.config.background.fade", "narutooverlay.config.background.fade.desc", 0.0D, 3.0D, 0.1D, () -> BACKGROUND.state().fade(), BACKGROUND::setBackgroundFade, value -> String.format(Locale.ROOT, "%.1f", value))
                            .slider("narutooverlay.config.background.dim", "narutooverlay.config.background.dim.desc", 0.0D, 1.0D, 0.1D, () -> BACKGROUND.state().dim(), BACKGROUND::setBackgroundDim, value -> String.format(Locale.ROOT, "%.1f", value));
                })
                .section("narutooverlay.config.about.config", section -> section
                        .value("narutooverlay.config.about.path", path.toString())
                        .customLayout(row -> {
                            Context context = row.getContext();
                            MuiTheme theme = row.theme();
                            MuiLayout actions = MuiLayout.row(context, theme).padding(0);

                            MuiTextAction copy = MuiTextAction.create(context, theme, "narutooverlay.config.about.copyPath");
                            copy.onClick(() -> runCopy(path, copy));

                            MuiTextAction open = MuiTextAction.create(context, theme, "narutooverlay.config.about.openFolder", () -> runOpen(path));

                            actions.add(copy, weight());

                            LinearLayout.LayoutParams openParams = wrap();
                            openParams.leftMargin = dp(context, 12);

                            actions.add(open, openParams);

                            row.add(actions, fill());
                        }))
                .section("narutooverlay.config.about.usage", section -> section
                        .note("narutooverlay.config.about.hint.drag", "narutooverlay.config.about.hint.drag.desc")
                        .note("narutooverlay.config.about.hint.merge", "narutooverlay.config.about.hint.merge.desc")
                        .note("narutooverlay.config.about.hint.split", "narutooverlay.config.about.hint.split.desc")
                        .note("narutooverlay.config.about.hint.open", "narutooverlay.config.about.hint.open.desc")
                        .note("narutooverlay.config.about.hint.reset", "narutooverlay.config.about.hint.reset.desc")
                )
                .custom(context -> {
                    TextView footer = text(context.context(), context.theme(), "narutooverlay.config.about.footer", 11, context.theme().onSurfaceVariant());
                    footer.setGravity(Gravity.CENTER_HORIZONTAL);

                    LinearLayout.LayoutParams params = fill();
                    params.topMargin = dp(context.context(), 28);

                    context.content().addView(footer, params);
                });
    }

    private static void runCopy(@NotNull Path path, @NotNull MuiTextAction copy) {
        Minecraft.getInstance().keyboardHandler.setClipboard(path.toString());
        copy.flash("narutooverlay.config.about.copied", 1000);
    }

    private static void runOpen(@NotNull Path path) {
        Path absolute = path.toAbsolutePath();
        String os = System.getProperty("os.name", "").toLowerCase(Locale.ROOT);

        try {
            if (absolute.toFile().isFile() && os.contains("win")) {
                new ProcessBuilder("explorer.exe", "/select," + absolute).start();
                return;
            }
            if (absolute.toFile().isFile() && os.contains("mac")) {
                new ProcessBuilder("open", "-R", absolute.toString()).start();
                return;
            }
        } catch (Throwable ignored) {}

        try {
            Util.getPlatform().openFile(absolute.getParent().toFile());
        } catch (Throwable ignored) {}
    }

    private static void onMouseButton(InputEvent.MouseButton.@NotNull Pre event) {
        if (event.getButton() != GLFW.GLFW_MOUSE_BUTTON_RIGHT) return;
        if (event.getAction() != GLFW.GLFW_PRESS) return;

        Minecraft minecraft = Minecraft.getInstance();
        if (!(minecraft.screen instanceof ChatScreen)) return;

        Card<?> card = TextRelocation.pick(TextRelocation.mouseX(), TextRelocation.mouseY());
        if (card == null) return;

        event.setCanceled(true);
        minecraft.setScreen(build(minecraft.screen));
    }

    static final class NarutoBackgroundSettings implements MuiBackgroundSettings {
        @Override
        public @NotNull MuiBackgroundState load() {
            List<String> order = new ArrayList<>();
            for (String name : BACKGROUND_ORDER.get()) {
                if (name != null) order.add(name);
            }

            return new MuiBackgroundState(
                    BACKGROUND_IMAGE.get(),
                    BACKGROUND_FOLDER.get(),
                    order,
                    BACKGROUND_INTERVAL.get(),
                    BACKGROUND_FADE.get(),
                    BACKGROUND_DIM.get(),
                    new MuiCrop(
                            BACKGROUND_CROP_X.get(),
                            BACKGROUND_CROP_Y.get(),
                            BACKGROUND_CROP_WIDTH.get(),
                            BACKGROUND_CROP_HEIGHT.get()
                    )
            );
        }

        @Override
        public void save(@NotNull MuiBackgroundState state) {
            BACKGROUND_IMAGE.set(state.image());
            BACKGROUND_FOLDER.set(state.folder());
            BACKGROUND_ORDER.set(new ArrayList<>(state.order()));
            BACKGROUND_INTERVAL.set(state.interval());
            BACKGROUND_FADE.set(state.fade());
            BACKGROUND_DIM.set(state.dim());

            MuiCrop crop = state.crop();
            BACKGROUND_CROP_X.set(crop.x());
            BACKGROUND_CROP_Y.set(crop.y());
            BACKGROUND_CROP_WIDTH.set(crop.width());
            BACKGROUND_CROP_HEIGHT.set(crop.height());
            ConfigSaver.changed();
        }
    }

    static final class NarutoCropFactory implements MuiCropOverlayFactory {
        @Override
        public @NotNull MuiCropOverlay create(@NotNull Context context, @NotNull Image image, int imageWidth, int imageHeight, float targetAspect, MuiCropOverlay.@NotNull Labels labels, MuiCropOverlay.@NotNull Listener listener) {
            Supplier<MuiCropOverlay> overlay = () -> new MuiCropOverlay(context, MuiTheme.modern(context).withPrimary(NarutoAccent.primary()), image, imageWidth, imageHeight, targetAspect, labels, listener);
            return MuiShimmerProfile.withCurrent(NarutoShimmerConfig.PROFILE, () -> withFactory(NarutoShimmerFactory.INSTANCE, overlay));
        }
    }

    static class NarutoScreenHost implements MuiScreenHost {
        private @Nullable MuiConfigFragment fragment;

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
            MuiConfigFragment current = this.fragment;
            if (current != null) {
                current.reloadTheme();
            }
            ConfigSaver.changed();
        }
    }

    static class NarutoShimmerConfig implements MuiShimmerConfig {
        public static final MuiShimmerProfile PROFILE = MuiShimmerProfile.create(new NarutoShimmerConfig());

        @Override
        public boolean enabled() {
            return SHIMMER.get();
        }

        @Override
        public boolean hasAccent() {
            return palette().hasAccent();
        }

        @Override
        public int fps() {
            return CONFIG_SHIMMER_FPS.get();
        }

        @Override
        public double speed() {
            return SHIMMER_SPEED.get();
        }

        @Override
        public double scale() {
            return SHIMMER_SCALE.get();
        }

        @Override
        public double strength() {
            return SHIMMER_STRENGTH.get();
        }

        @Override
        public int @NotNull [] ramp() {
            return ThemeColors.shimmerRamp(THEME.get(), palette());
        }
    }

    static class NarutoShimmerFactory implements MuiWidgetFactory {
        public static final MuiWidgetFactory SHIMMER = MuiWidgetFactory.shimmer();
        public static final NarutoShimmerFactory INSTANCE = new NarutoShimmerFactory();

        @Override
        public @NotNull TextView text(@NotNull Context context, @NotNull CharSequence text, float size, int color) {
            return SHIMMER.text(context, text, size, color);
        }

        @Override
        public @NotNull TextView text(@NotNull Context context, @NotNull CharSequence text, float size, int color, @NotNull MuiShimmerable marker) {
            return SHIMMER.text(context, text, size, color, marker);
        }

        @Override
        public @NotNull Switch switchControl(@NotNull Context context, @NotNull MuiTheme theme) {
            return SHIMMER.switchControl(context, naruto(theme));
        }

        @Override
        public @NotNull Switch switchControl(@NotNull Context context, @NotNull MuiTheme theme, @NotNull MuiShimmerable marker) {
            return SHIMMER.switchControl(context, naruto(theme), marker);
        }

        @Override
        public @NotNull SeekBar slider(@NotNull Context context, @NotNull MuiTheme theme, int max, int progress) {
            return SHIMMER.slider(context, naruto(theme), max, progress);
        }

        @Override
        public @NotNull SeekBar slider(@NotNull Context context, @NotNull MuiTheme theme, int max, int progress, @NotNull MuiShimmerable marker) {
            return SHIMMER.slider(context, naruto(theme), max, progress, marker);
        }

        @Override
        public @NotNull Button button(@NotNull Context context, @NotNull MuiTheme theme, @NotNull CharSequence label, boolean outlined, View.@NotNull OnClickListener onClick) {
            return SHIMMER.button(context, naruto(theme), label, outlined, onClick);
        }

        @Override
        public @NotNull Button button(@NotNull Context context, @NotNull MuiTheme theme, @NotNull CharSequence label, boolean outlined, View.@NotNull OnClickListener onClick, @NotNull MuiShimmerable marker) {
            return SHIMMER.button(context, naruto(theme), label, outlined, onClick, marker);
        }

        @Override
        public @NotNull CheckBox checkBox(@NotNull Context context, @NotNull MuiTheme theme, @NotNull MuiShimmerable marker) {
            return SHIMMER.checkBox(context, naruto(theme), marker);
        }

        @Override
        public @NotNull RadioButton radioButton(@NotNull Context context, @NotNull MuiTheme theme, @NotNull MuiShimmerable marker) {
            return SHIMMER.radioButton(context, naruto(theme), marker);
        }

        @Override
        public @NotNull ProgressBar progressBar(@NotNull Context context, @NotNull MuiTheme theme, @NotNull MuiShimmerable marker) {
            return SHIMMER.progressBar(context, naruto(theme), marker);
        }

        @Override
        public @NotNull MuiTextField textField(@NotNull Context context, @NotNull MuiTheme theme, @NotNull CharSequence text) {
            return SHIMMER.textField(context, naruto(theme), text);
        }

        @Override
        public @NotNull MuiNumberField numberField(@NotNull Context context, @NotNull MuiTheme theme, long value) {
            return SHIMMER.numberField(context, naruto(theme), value);
        }

        @Override
        public @NotNull <T> MuiDropdown<T> dropdown(@NotNull Context context, @NotNull MuiTheme theme, @NotNull List<MuiOption<T>> muiOptions) {
            return SHIMMER.dropdown(context, naruto(theme), muiOptions);
        }

        @Override
        public @NotNull <T> MuiChoiceGrid<T> choiceGrid(@NotNull Context context, @NotNull MuiTheme theme, @NotNull List<MuiOption<T>> muiOptions) {
            return SHIMMER.choiceGrid(context, naruto(theme), muiOptions);
        }

        @Override
        public @NotNull MuiListEditor listEditor(@NotNull Context context, @NotNull MuiTheme theme, @NotNull List<String> values) {
            return SHIMMER.listEditor(context, naruto(theme), values);
        }

        private static @NotNull MuiTheme naruto(@NotNull MuiTheme theme) {
            return theme.withPrimary(NarutoAccent.primary()).withOnPrimary(NarutoAccent.onPrimary());
        }
    }

    static final class NarutoAccent {
        public static int primary() {
            Palette palette = palette();
            return palette.hasAccent() ? palette.border() | 0xFF000000 : 0xFFFFFFFF;
        }

        public static int onPrimary() {
            return ThemeColors.readableOn(primary());
        }
    }
}
