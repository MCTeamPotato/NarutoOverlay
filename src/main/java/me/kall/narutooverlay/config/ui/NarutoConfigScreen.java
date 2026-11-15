package me.kall.narutooverlay.config.ui;

import icyllis.modernui.core.Context;
import icyllis.modernui.view.Gravity;
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
import me.kall.narutooverlay.theme.HudScheme;
import me.kall.narutooverlay.theme.HudSchemes;
import me.kall.narutooverlay.theme.Palette;
import me.kall.narutooverlay.theme.ThemeColors;
import me.kall.narutoui.mui.background.*;
import me.kall.narutoui.mui.choice.MuiOption;
import me.kall.narutoui.mui.factory.MuiWidgetFactory;
import me.kall.narutoui.mui.field.MuiNumberField;
import me.kall.narutoui.mui.layout.MuiLayout;
import me.kall.narutoui.mui.overlay.MuiColorPickerOverlay;
import me.kall.narutoui.mui.overlay.MuiCropOverlay;
import me.kall.narutoui.mui.screen.*;
import me.kall.narutoui.mui.shimmer.MuiShimmer;
import me.kall.narutoui.mui.shimmer.MuiShimmerConfig;
import me.kall.narutoui.mui.shimmer.MuiShimmerProfile;
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
import java.util.List;
import java.util.Locale;

import static me.kall.narutooverlay.config.RenderArgs.*;
import static me.kall.narutoui.mui.layout.MuiControls.*;

/**
 * NarutoOverlay's configuration screen.
 * <p>
 * One instance owns one screen: the background controller, the shimmer profile and the page
 * bodies are all per-instance, so opening the config from the chat screen and from the mod list no
 * longer share mutable UI state. The static {@link #register(FMLJavaModLoadingContext)} entry point
 * only installs the Forge hooks.
 */
public final class NarutoConfigScreen {
    private final MuiConfigurableBackground background;
    private final MuiShimmerProfile shimmerProfile;

    private NarutoConfigScreen() {
        this.background = MuiConfigurableBackground
                .builder(new NarutoBackgroundSettings(), backgroundLabels(), new MuiBackgroundFiles(FMLPaths.GAMEDIR.get(), "narutooverlay-config-bg"))
                .build();
        this.shimmerProfile = MuiShimmerProfile.create(new NarutoShimmerConfig());
    }

    public static void register(@NotNull FMLJavaModLoadingContext context) {
        NarutoJarResources.install();
        context.registerExtensionPoint(ConfigScreenHandler.ConfigScreenFactory.class, () -> new ConfigScreenHandler.ConfigScreenFactory((minecraft, screen) -> build(screen)));
        MinecraftForge.EVENT_BUS.addListener(NarutoConfigScreen::onMouseButton);
    }

    private static @NotNull Screen build(@Nullable Screen previous) {
        return new NarutoConfigScreen().create(previous);
    }

    private @NotNull Screen create(@Nullable Screen previous) {
        return MuiSettingsScreen.builder("NarutoOverlay")
                .widgetFactory(MuiWidgetFactory.shimmer())
                .shimmerProfile(this.shimmerProfile)
                .background(this.background)
                .initialPage(0)
                .shimmerNavigation()
                .shimmerTitles()
                .themeProvider(context -> MuiTheme.modern(context)
                        .withPrimary(NarutoAccent.primary())
                        .withOnPrimary(NarutoAccent.onPrimary()))
                .railHeader((context, theme) -> MuiLogo.create(context, theme, "N")
                        .color(NarutoAccent.primary())
                        .textColor(NarutoAccent.onPrimary()))
                .railFooter((context, theme) -> MuiHintBadge.create(context, theme, "narutooverlay.config.footer.escHint")
                        .color(NarutoAccent.primary()))
                .page("narutooverlay.config.nav.cards", "narutooverlay.config.cards.title", "narutooverlay.config.cards.subtitle", this::buildCards)
                .page("narutooverlay.config.nav.appearance", "narutooverlay.config.appearance.title", "narutooverlay.config.appearance.subtitle", this::buildAppearance)
                .page("narutooverlay.config.nav.about", "narutooverlay.config.about.title", "narutooverlay.config.about.subtitle", this::buildAbout)
                .create(previous);
    }

    /**
     * Builds one collapsible section per registered card. Each section is rebuilt on its own after a
     * reset, so the page keeps its scroll position and the other cards stay expanded.
     *
     * @param page the cards page
     */
    private void buildCards(@NotNull MuiSettingsPage page) {
        page.shimmerAll();

        Context context = page.context().context();
        MuiTheme theme = page.theme();

        List<Runnable> refreshers = new ArrayList<>();
        for (Card<?> card : CardRegistry.all()) {
            String cardID = "narutooverlay.config.card." + card.id().toLowerCase(Locale.ROOT);
            Switch toggle = this.cardToggle(context, theme, card);

            MuiSettingsSection section = page.collapsibleCard(
                    cardID,
                    cardID,
                    cardID + ".summary",
                    false,
                    box -> box.trailing(toggle),
                    body -> this.buildCardBody(card, body));

            refreshers.add(() -> {
                toggle.setChecked(card.enabledValue().get());
                section.rebuild(body -> this.buildCardBody(card, body));
            });
        }

        page.customLayout(content -> {
            MuiTextAction resetAll = MuiTextAction.create(context, theme, "narutooverlay.config.cards.resetAll", () -> {
                CardRegistry.resetAll();
                ConfigSaver.changed();
                refreshers.forEach(Runnable::run);
            });

            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(WRAP, WRAP);
            params.topMargin = dp(context, 20);
            content.add(resetAll, params);
        });
    }

    private @NotNull Switch cardToggle(@NotNull Context context, @NotNull MuiTheme theme, @NotNull Card<?> card) {
        Switch toggle = switchControl(context, theme);
        toggle.setChecked(card.enabledValue().get());
        toggle.setContentDescription("narutooverlay.config.cards.enabled");
        toggle.setOnCheckedChangeListener((buttonView, isChecked) -> card.setEnabled(isChecked));
        return toggle;
    }

    private void buildCardBody(@NotNull Card<?> card, @NotNull MuiSettingsSection section) {
        Context context = section.page().context();
        MuiTheme theme = section.page().theme();

        MuiNumberField x = MuiNumberField.create(context, theme, card.left()).bounds(0, Integer.MAX_VALUE).gravity(Gravity.CENTER).onChangeInt(card::setLeft);
        MuiNumberField y = MuiNumberField.create(context, theme, card.top()).bounds(0, Integer.MAX_VALUE).gravity(Gravity.CENTER).onChangeInt(card::setTop);

        Runnable sync = () -> {
            x.value(card.left());
            y.value(card.top());
        };

        LinearLayout.LayoutParams yLabelParams = wrap();
        yLabelParams.setMargins(dp(context, 14), 0, 0, 0);

        LinearLayout.LayoutParams fieldParams = new LinearLayout.LayoutParams(dp(context, 64), WRAP);
        fieldParams.setMargins(dp(context, 6), 0, 0, 0);

        section.addRow(MuiLayout.row(context, theme).padding(0)
                .add(text(context, theme, "X", 12, theme.onSurfaceVariant()))
                .add(x, fieldParams)
                .add(text(context, theme, "Y", 12, theme.onSurfaceVariant()), yLabelParams)
                .add(y, fieldParams)
                .flexibleSpace()
                .add(MuiTextAction.create(context, theme, "narutooverlay.config.cards.resetPosition", () -> card.resetPosition(sync))));

        int screenWidth = Minecraft.getInstance().getWindow().getGuiScaledWidth();
        int screenHeight = Minecraft.getInstance().getWindow().getGuiScaledHeight();
        int right = Math.max(8, screenWidth - card.lineWidth() - 8);
        int bottom = Math.max(8, screenHeight - Minecraft.getInstance().font.lineHeight - 8);

        section.addRow(MuiLayout.row(context, theme)
                .padding(0)
                .spacing(6)
                .addWeighted(button(context, theme, "narutooverlay.config.cards.topLeft", true, view -> card.moveTo(8, 8, sync)), 1.0F)
                .addWeighted(button(context, theme, "narutooverlay.config.cards.topRight", true, view -> card.moveTo(right, 8, sync)), 1.0F)
                .addWeighted(button(context, theme, "narutooverlay.config.cards.bottomLeft", true, view -> card.moveTo(8, bottom, sync)), 1.0F)
                .addWeighted(button(context, theme, "narutooverlay.config.cards.bottomRight", true, view -> card.moveTo(right, bottom, sync)), 1.0F));

        for (Field<?> field : card.allFields()) {
            String fieldID = field.id();
            FieldConfig config = card.fieldConfig(fieldID);

            MuiLayout fieldRow = MuiLayout.row(context, theme)
                    .padding(0)
                    .add(text(context, theme, "narutooverlay.config.field." + fieldID.toLowerCase(Locale.ROOT), 13, theme.onSurface()), weight());

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

            section.addRow(fieldRow);
        }

        section.addRow(MuiTextAction.create(context, theme, "narutooverlay.config.cards.resetCard", () ->
                section.card().post(() -> section.rebuild(body -> this.buildCardBody(card, body)))));
    }

    private void buildAppearance(@NotNull MuiSettingsPage page) {
        MuiScreenHost host = page.context().host();

        page.shimmerAll()
                .section("narutooverlay.config.appearance.theme", section -> section
                        .choiceGridBound("narutooverlay.config.appearance.theme", null,
                                themeChoices(),
                                NarutoConfigScreen::selectedThemeId, theme -> {
                                    set(THEME, theme);
                                    if (host != null) host.onThemeChanged();
                                })
                )
                .section("narutooverlay.config.appearance.custom", section -> {
                    section.note(localized("narutooverlay.config.appearance.custom.note"));

                    section.customLayout(row -> {
                        MuiColorField field = colorField(row, "narutooverlay.config.appearance.customAccent", customAccent());
                        field.onPick(() -> pickCustomColor(host, field, true));
                        row.add(field, fill());
                    });

                    section.customLayout(row -> {
                        MuiColorField field = colorField(row, "narutooverlay.config.appearance.customBackplane", customBackplane());
                        field.onPick(() -> pickCustomColor(host, field, false));
                        row.add(field, fill());
                    });

                    section.enabledWhenAll(() -> HudSchemes.custom(THEME.get()));
                })
                .section("narutooverlay.config.appearance.display", section -> section
                        .toggle("narutooverlay.config.appearance.dropShadow", "narutooverlay.config.appearance.dropShadow.desc", DROP_SHADOW::get, value -> set(DROP_SHADOW, value))
                        .toggle("narutooverlay.config.appearance.equalWidth", "narutooverlay.config.appearance.equalWidth.desc", EQUAL_WIDTH::get, value -> set(EQUAL_WIDTH, value))
                        .toggle("narutooverlay.config.appearance.drawBackplane", "narutooverlay.config.appearance.drawBackplane.desc", DRAW_BACKPLANE::get, value -> set(DRAW_BACKPLANE, value))
                        .toggle("narutooverlay.config.appearance.shimmer", "narutooverlay.config.appearance.shimmer.desc", SHIMMER::get, value -> {
                            set(SHIMMER, value);
                            MuiShimmer.refresh();
                        })
                        .enabledWhen(() -> palette().hasAccent())
                )
                .section("narutooverlay.config.appearance.shimmer", section -> {
                    section.slider("narutooverlay.config.appearance.shimmerSpeed", "narutooverlay.config.appearance.shimmerSpeed.desc", 0.2D, 3.0D, 0.1D, SHIMMER_SPEED::get, value -> set(SHIMMER_SPEED, value), value -> String.format(Locale.ROOT, "%.1f", value))
                            .slider("narutooverlay.config.appearance.shimmerDensity", "narutooverlay.config.appearance.shimmerDensity.desc", 0.2D, 3.0D, 0.1D, SHIMMER_SCALE::get, value -> set(SHIMMER_SCALE, value), value -> String.format(Locale.ROOT, "%.1f", value))
                            .slider("narutooverlay.config.appearance.shimmerStrength", "narutooverlay.config.appearance.shimmerStrength.desc", 0.0D, 1.0D, 0.1D, SHIMMER_STRENGTH::get, value -> set(SHIMMER_STRENGTH, value), value -> String.format(Locale.ROOT, "%.1f", value))
                            .slider("narutooverlay.config.appearance.shimmerFps", "narutooverlay.config.appearance.shimmerFps.desc", 5, 240, SHIMMER_FPS::get, value -> set(SHIMMER_FPS, value), Integer::toString)
                            .slider("narutooverlay.config.appearance.configShimmerFps", "narutooverlay.config.appearance.configShimmerFps.desc", 5, 120, CONFIG_SHIMMER_FPS::get, value -> set(CONFIG_SHIMMER_FPS, value), Integer::toString)
                            .toggle("narutooverlay.config.appearance.cacheShimmer", "narutooverlay.config.appearance.cacheShimmer.desc", CACHE_SHIMMER::get, value -> set(CACHE_SHIMMER, value))
                            .enabledWhenAll(() -> palette().hasAccent());

                    if (!palette().hasAccent()) section.note("narutooverlay.config.appearance.shimmer.unavailable");
                })
                .section("narutooverlay.config.appearance.backplane", section -> section
                        .slider("narutooverlay.config.appearance.borderWidth", "narutooverlay.config.appearance.borderWidth.desc", 0.0D, 4.0D, 0.1D, BACKPLANE_BORDER_WIDTH::get, value -> set(BACKPLANE_BORDER_WIDTH, value), value -> String.format(Locale.ROOT, "%.1f", value))
                        .toggle("narutooverlay.config.appearance.borderOverride", "narutooverlay.config.appearance.borderOverride.desc", BACKPLANE_BORDER_OVERRIDE::get, value -> set(BACKPLANE_BORDER_OVERRIDE, value))
                        .customLayout(row -> {
                            MuiColorField field = colorField(row, "narutooverlay.config.appearance.borderColor", borderColor());
                            field.onPick(() -> pickBorderColor(host, field));
                            row.add(field, fill());
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

    private static @NotNull MuiColorField colorField(@NotNull MuiLayout row, @NotNull String labelKey, int color) {
        return MuiColorField.create(row.getContext(), row.theme(), labelKey, color)
                .value(ThemeColors.format(color))
                .buttonLabel("narutooverlay.config.appearance.pickColor");
    }

    private void pickCustomColor(@Nullable MuiScreenHost host, @NotNull MuiColorField field, boolean accent) {
        if (host == null) return;

        String titleKey = accent ? "narutooverlay.config.appearance.customAccent" : "narutooverlay.config.appearance.customBackplane";
        host.colorPicker(accent ? customAccent() : customBackplane(), colorLabels(titleKey), argb -> {
            int shown = accent ? ThemeColors.opaque(argb) : argb;

            if (accent) {
                CUSTOM_ACCENT.set(ThemeColors.toColorToken(shown));
            } else {
                CUSTOM_BACKPLANE.set(ThemeColors.toColorToken(shown));
            }

            field.color(shown);
            field.value(ThemeColors.format(shown));
            ConfigSaver.changed();

            // The accent is part of the screen theme, so the rail and every control have to be
            // resolved again; the other custom color only affects the HUD.
            if (accent) host.onThemeChanged();
        });
    }

    private void pickBorderColor(@Nullable MuiScreenHost host, @NotNull MuiColorField field) {
        if (host == null) return;

        host.colorPicker(borderColor(), colorLabels("narutooverlay.config.appearance.borderColor"), argb -> {
            BACKPLANE_BORDER_OVERRIDE.set(true);
            BACKPLANE_BORDER_RED.set((argb >> 16) & 0xFF);
            BACKPLANE_BORDER_GREEN.set((argb >> 8) & 0xFF);
            BACKPLANE_BORDER_BLUE.set(argb & 0xFF);
            BACKPLANE_BORDER_ALPHA.set((argb >>> 24) & 0xFF);

            field.color(argb);
            field.value(ThemeColors.format(argb));
            ConfigSaver.changed();
        });
    }

    private static @NotNull MuiColorPickerOverlay.Labels colorLabels(@NotNull CharSequence title) {
        return MuiColorPickerOverlay.Labels.builder()
                .title(title)
                .brightness("narutooverlay.config.color.brightness")
                .alpha("narutooverlay.config.color.alpha")
                .apply("narutooverlay.config.color.apply")
                .cancel("narutooverlay.config.color.cancel")
                .build();
    }

    private static @NotNull List<MuiOption<String>> themeChoices() {
        List<MuiOption<String>> choices = new ArrayList<>();
        for (HudScheme scheme : HudSchemes.all()) {
            choices.add(MuiOption.of(scheme.id(), themeLabel(scheme))
                    .preview((context, activeTheme) -> new ThemePreview(context, scheme.palette())));
        }
        choices.add(MuiOption.of("CUSTOM", localized("narutooverlay.theme.custom"))
                .preview((context, activeTheme) -> new ThemePreview(context, RenderArgs.paletteFor("CUSTOM"))));
        return choices;
    }

    private static @NotNull String selectedThemeId() {
        String id = THEME.get();
        if (HudSchemes.custom(id)) {
            return "CUSTOM";
        }
        HudScheme scheme = HudSchemes.byId(id);
        return scheme != null ? scheme.id() : HudSchemes.all().get(0).id();
    }

    private static @NotNull String themeLabel(@NotNull HudScheme scheme) {
        String key = scheme.nameKey();
        String translated = I18n.get(key);
        return translated.equals(key) ? scheme.id() : translated;
    }

    private static @NotNull String localized(@NotNull String key) {
        return I18n.get(key);
    }

    private void buildAbout(@NotNull MuiSettingsPage page) {
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
                    String folder = this.background.state().folder();
                    int count = this.background.images().size();

                    section
                            .value("narutooverlay.config.background.folder", folder.isBlank() ? I18n.get("narutooverlay.config.background.folder.none") : folder)
                            .text("narutooverlay.config.background.folder.hint")
                            .customLayout(row -> {
                                Context context = row.getContext();
                                MuiTheme theme = row.theme();

                                Button choose = button(context, theme, "narutooverlay.config.background.folder.choose", true, view -> this.background.chooseBackgroundFolder());
                                Button clear = button(context, theme, "narutooverlay.config.background.folder.clear", true, view -> this.background.clearBackgroundFolder());
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
                            .slider("narutooverlay.config.background.interval", "narutooverlay.config.background.interval.desc", 2.0D, 300.0D, 0.1D, this.background.state()::interval, this.background::setBackgroundInterval, value -> String.format(Locale.ROOT, "%.1f", value))
                            .slider("narutooverlay.config.background.fade", "narutooverlay.config.background.fade.desc", 0.0D, 3.0D, 0.1D, () -> this.background.state().fade(), this.background::setBackgroundFade, value -> String.format(Locale.ROOT, "%.1f", value))
                            .slider("narutooverlay.config.background.dim", "narutooverlay.config.background.dim.desc", 0.0D, 1.0D, 0.1D, () -> this.background.state().dim(), this.background::setBackgroundDim, value -> String.format(Locale.ROOT, "%.1f", value));
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

    private static @NotNull MuiBackgroundLabels backgroundLabels() {
        return MuiBackgroundLabels.builder()
                .pickerTitle("narutooverlay.config.background.picker")
                .imageFilter("narutooverlay.config.background.filter")
                .pickerFailed("narutooverlay.config.background.pickerFailed")
                .folderPickerTitle("narutooverlay.config.background.folder.picker")
                .folderPickerFailed("narutooverlay.config.background.folder.failed")
                .failed("narutooverlay.config.background.failed")
                .saved("narutooverlay.config.background.saved")
                .crop(MuiCropOverlay.Labels.builder()
                        .title("narutooverlay.config.background.crop.title")
                        .hint("narutooverlay.config.background.crop.hint")
                        .confirm("narutooverlay.config.background.confirm")
                        .cancel("narutooverlay.config.background.cancel")
                        .build())
                .build();
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

    static final class NarutoShimmerConfig implements MuiShimmerConfig {
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
            return HudSchemes.shimmerRamp(THEME.get(), palette());
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
