package me.kall.narutooverlay.config.ui.page;

import icyllis.modernui.core.Context;
import icyllis.modernui.mc.MuiModApi;
import icyllis.modernui.view.Gravity;
import icyllis.modernui.widget.Button;
import icyllis.modernui.widget.LinearLayout;
import icyllis.modernui.widget.TextView;
import me.kall.narutooverlay.NarutoOverlay;
import me.kall.narutoui.mui.background.*;
import me.kall.narutoui.mui.overlay.*;
import me.kall.narutoui.mui.screen.*;
import me.kall.narutoui.mui.theme.*;
import me.kall.narutoui.mui.widget.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.SharedConstants;
import net.minecraft.Util;
import net.minecraftforge.fml.loading.FMLPaths;
import net.minecraftforge.fml.ModList;
import org.jetbrains.annotations.NotNull;

import java.nio.file.Path;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

/**
 * About page expressed through the high-level settings DSL.
 */
public final class ConfigAboutPage {
    private static final String GROUP = "narutooverlay.config.";

    private ConfigAboutPage() {}

    @SuppressWarnings("UnnecessaryUnicodeEscape")
    public static void populate(@NotNull MuiSettingsPage page, @NotNull MuiBackgroundActions background) {
        MuiBackgroundState state = background.state();
        Path path = FMLPaths.CONFIGDIR.get().resolve("narutooverlay-client.toml");

        page.section(GROUP + "about.info", section -> section.value(GROUP + "about.version", version())
                .value(GROUP + "about.environment", NarutoOverlay.ENV + " \u00B7 " + SharedConstants.getCurrentVersion().getName() + " \u00B7 " + I18n.get(GROUP + "about.clientOnly")))
                .section(GROUP + "about.background", section -> {
                    String folder = state.folder();
                    int count = background.images().size();

                    section.value(GROUP + "background.folder", folder.isEmpty() ? I18n.get(GROUP + "background.folder.none") : folder)
                            .text(GROUP + "background.folder.hint")
                            .customLayout(row -> addFolderButtons(row, background, folder))
                            .value(GROUP + "background.images", count == 0 ? I18n.get(GROUP + "background.images.empty") : String.valueOf(count))
                            .slider(GROUP + "background.interval", 2, 300, () -> (int) Math.round(background.state().interval()), background::setBackgroundInterval, value -> I18n.get(GROUP + "background.interval.value", value))
                            .slider(GROUP + "background.fade", 0, 300, () -> (int) Math.round(background.state().fade() * 100.0D), value -> background.setBackgroundFade(value / 100.0D), value -> I18n.get(GROUP + "background.fade.value", String.format(Locale.ROOT, "%.1f", value / 100.0D)))
                            .slider(GROUP + "background.dim", 0, 95, () -> (int) Math.round(background.state().dim() * 100.0D), value -> background.setBackgroundDim(value / 100.0D), value -> value + "%");
                })
                .section(GROUP + "about.config", section -> section.value(GROUP + "about.path", path.toString()).customLayout(row -> addPathActions(row, path)))
                .section(GROUP + "about.usage", section -> {
                    Context context = section.page().context();
                    MuiTheme theme = section.page().theme();
                    section.custom(MuiDescriptionRow.create(context, theme, GROUP + "about.hint.drag", GROUP + "about.hint.drag.desc"))
                            .custom(MuiDescriptionRow.create(context, theme, GROUP + "about.hint.merge", GROUP + "about.hint.merge.desc"))
                            .custom(MuiDescriptionRow.create(context, theme, GROUP + "about.hint.split", GROUP + "about.hint.split.desc"))
                            .custom(MuiDescriptionRow.create(context, theme, GROUP + "about.hint.open", GROUP + "about.hint.open.desc"))
                            .custom(MuiDescriptionRow.create(context, theme, GROUP + "about.hint.reset", GROUP + "about.hint.reset.desc"));
                }).custom(context -> {
                    TextView footer = MuiControls.text(context.context(), context.theme(), GROUP + "about.footer", 11, context.theme().onSurfaceVariant());
                    footer.setGravity(Gravity.CENTER_HORIZONTAL);
                    LinearLayout.LayoutParams params = MuiControls.fill();
                    params.topMargin = MuiControls.dp(context.context(), 28);
                    context.content().addView(footer, params);
                });
    }

    private static void addFolderButtons(@NotNull MuiLayout row, @NotNull MuiBackgroundActions background, @NotNull String folder) {
        Context context = row.getContext();
        MuiTheme theme = MuiTheme.modern(context);

        Button choose = MuiControls.button(context, theme, GROUP + "background.folder.choose", true, view -> background.chooseBackgroundFolder());
        Button clear = MuiControls.button(context, theme, GROUP + "background.folder.clear", true, view -> background.clearBackgroundFolder());
        clear.setEnabled(!folder.isEmpty());

        MuiLayout buttons = MuiLayout.row(context, theme).padding(0).spacing(8).addWeighted(choose, 1.0F).addWeighted(clear, 1.0F);
        row.add(buttons, MuiControls.fill());
    }

    private static void addPathActions(@NotNull MuiLayout row, @NotNull Path path) {
        Context context = row.getContext();
        MuiTheme theme = MuiTheme.modern(context);
        MuiLayout actions = MuiLayout.row(context, theme).padding(0);

        MuiTextAction copy = MuiTextAction.create(context, theme, GROUP + "about.copyPath");
        copy.onClick(() -> {
            Minecraft.getInstance().keyboardHandler.setClipboard(path.toString());
            copy.text(GROUP + "about.copied");

            Thread thread = new Thread(() -> {
                long start = System.nanoTime();
                while (true) {
                    if (TimeUnit.NANOSECONDS.toSeconds(System.nanoTime() - start) >= 1) {
                        MuiModApi.postToUiThread(() -> {
                            try {
                                copy.text(GROUP + "about.copyPath");
                            } catch (Throwable ignored) {}
                        });
                        break;
                    }
                }
            });
            thread.setDaemon(true);
            thread.start();
        });

        MuiTextAction open = MuiTextAction.create(context, theme, GROUP + "about.openFolder", () -> reveal(path));

        actions.add(copy, MuiControls.weight());
        LinearLayout.LayoutParams openParams = MuiControls.wrap();
        openParams.leftMargin = MuiControls.dp(context, 12);
        actions.add(open, openParams);
        row.add(actions, MuiControls.fill());
    }

    private static void reveal(@NotNull Path path) {
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
        } catch (Throwable ignored) {
            // Fall through to the parent folder.
        }

        try {
            Util.getPlatform().openFile(absolute.getParent().toFile());
        } catch (Throwable ignored) {
            // Platform does not expose a file manager.
        }
    }

    private static @NotNull String version() {
        return ModList.get().getModContainerById("narutooverlay").map(container -> container.getModInfo().getVersion().toString()).orElse("?");
    }
}