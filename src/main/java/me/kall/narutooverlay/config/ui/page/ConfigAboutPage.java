package me.kall.narutooverlay.config.ui.page;

import icyllis.modernui.core.Context;
import icyllis.modernui.view.Gravity;
import icyllis.modernui.widget.Button;
import icyllis.modernui.widget.LinearLayout;
import icyllis.modernui.widget.TextView;
import me.kall.narutooverlay.NarutoOverlay;
import me.kall.narutoui.mui.background.*;
import me.kall.narutoui.mui.overlay.*;
import me.kall.narutoui.mui.screen.*;
import me.kall.narutoui.mui.theme.*;
import me.kall.narutoui.mui.util.*;
import me.kall.narutoui.mui.widget.*;
import net.minecraft.client.Minecraft;
import net.minecraft.SharedConstants;
import net.minecraft.Util;
import net.minecraftforge.fml.loading.FMLPaths;
import net.minecraftforge.fml.ModList;
import org.jetbrains.annotations.NotNull;

import java.nio.file.Path;
import java.util.Locale;

/**
 * About page expressed through the high-level settings DSL.
 */
public final class ConfigAboutPage {
    private static final String GROUP = "narutooverlay.config.";

    private ConfigAboutPage() {}

    @SuppressWarnings("UnnecessaryUnicodeEscape")
    public static void populate(@NotNull MuiSettingsPage page, @NotNull MuiBackgroundActions background) {
        MuiBackgroundState state = background.state();

        page.section(MuiLocalization.get(GROUP + "about.info"), section -> section.value(MuiLocalization.get(GROUP + "about.version"), version()).value(MuiLocalization.get(GROUP + "about.environment"), NarutoOverlay.ENV + " \u00B7 " + SharedConstants.getCurrentVersion().getName() + " \u00B7 " + MuiLocalization.get(GROUP + "about.clientOnly")));

        page.section(MuiLocalization.get(GROUP + "about.background"), section -> {
            String folder = state.folder();
            int count = background.images().size();

            section.value(MuiLocalization.get(GROUP + "background.folder"), folder.isEmpty() ? MuiLocalization.get(GROUP + "background.folder.none") : folder);
            section.text(MuiLocalization.get(GROUP + "background.folder.hint"));
            section.custom(row -> addFolderButtons(row, background, folder));
            section.value(MuiLocalization.get(GROUP + "background.images"), count == 0 ? MuiLocalization.get(GROUP + "background.images.empty") : String.valueOf(count));

            section.slider(MuiLocalization.get(GROUP + "background.interval"), 2, 300, () -> (int) Math.round(background.state().interval()), background::setBackgroundInterval, value -> MuiLocalization.get(GROUP + "background.interval.value", value));
            section.slider(MuiLocalization.get(GROUP + "background.fade"), 0, 300, () -> (int) Math.round(background.state().fade() * 100.0D), value -> background.setBackgroundFade(value / 100.0D), value -> MuiLocalization.get(GROUP + "background.fade.value", String.format(Locale.ROOT, "%.1f", value / 100.0D)));
            section.slider(MuiLocalization.get(GROUP + "background.dim"), 0, 95, () -> (int) Math.round(background.state().dim() * 100.0D), value -> background.setBackgroundDim(value / 100.0D), value -> value + "%");
        });

        Path path = FMLPaths.CONFIGDIR.get().resolve("narutooverlay-client.toml");
        page.section(MuiLocalization.get(GROUP + "about.config"), section -> section.value(MuiLocalization.get(GROUP + "about.path"), path.toString()).custom(row -> addPathActions(row, path)));

        page.section(MuiLocalization.get(GROUP + "about.usage"), section -> {
            Context context = section.page().context();
            MuiTheme theme = section.page().theme();
            section.custom(MuiDescriptionRow.create(context, theme,
                            MuiLocalization.get(GROUP + "about.hint.drag"),
                            MuiLocalization.get(GROUP + "about.hint.drag.desc")))
                    .custom(MuiDescriptionRow.create(context, theme,
                            MuiLocalization.get(GROUP + "about.hint.merge"),
                            MuiLocalization.get(GROUP + "about.hint.merge.desc")))
                    .custom(MuiDescriptionRow.create(context, theme,
                            MuiLocalization.get(GROUP + "about.hint.split"),
                            MuiLocalization.get(GROUP + "about.hint.split.desc")))
                    .custom(MuiDescriptionRow.create(context, theme,
                            MuiLocalization.get(GROUP + "about.hint.open"),
                            MuiLocalization.get(GROUP + "about.hint.open.desc")))
                    .custom(MuiDescriptionRow.create(context, theme,
                            MuiLocalization.get(GROUP + "about.hint.reset"),
                            MuiLocalization.get(GROUP + "about.hint.reset.desc")));
        });

        page.custom(context -> {
            TextView footer = MuiControls.text(context.context(), context.theme(), MuiLocalization.get(GROUP + "about.footer"), 11, context.theme().onSurfaceVariant());
            footer.setGravity(Gravity.CENTER_HORIZONTAL);
            LinearLayout.LayoutParams params = MuiControls.fill();
            params.topMargin = MuiControls.dp(context.context(), 28);
            context.content().addView(footer, params);
        });
    }

    private static void addFolderButtons(@NotNull LinearLayout row, @NotNull MuiBackgroundActions background, @NotNull String folder) {
        Context context = row.getContext();
        MuiTheme theme = MuiTheme.modern(context);

        Button choose = MuiControls.button(context, theme, MuiLocalization.get(GROUP + "background.folder.choose"), true, view -> background.chooseBackgroundFolder());
        Button clear = MuiControls.button(context, theme, MuiLocalization.get(GROUP + "background.folder.clear"), true, view -> background.clearBackgroundFolder());
        clear.setEnabled(!folder.isEmpty());

        MuiLayout buttons = MuiLayout.row(context, theme)
                .padding(0)
                .spacing(8)
                .addWeighted(choose, 1.0F)
                .addWeighted(clear, 1.0F);
        row.addView(buttons, MuiControls.fill());
    }

    private static void addPathActions(@NotNull LinearLayout row, @NotNull Path path) {
        Context context = row.getContext();
        MuiTheme theme = MuiTheme.modern(context);
        LinearLayout actions = MuiControls.row(context);
        actions.setPadding(0, 0, 0, 0);

        MuiTextAction copy = MuiTextAction.create(context, theme, MuiLocalization.get(GROUP + "about.copyPath"));
        copy.onClick(() -> {
            Minecraft.getInstance().keyboardHandler.setClipboard(path.toString());
            copy.text(MuiLocalization.get(GROUP + "about.copied"));
        });

        MuiTextAction open = MuiTextAction.create(context, theme, MuiLocalization.get(GROUP + "about.openFolder"), () -> reveal(path));

        actions.addView(copy, MuiControls.weight());
        LinearLayout.LayoutParams openParams = MuiControls.wrap();
        openParams.leftMargin = MuiControls.dp(context, 12);
        actions.addView(open, openParams);
        row.addView(actions, MuiControls.fill());
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