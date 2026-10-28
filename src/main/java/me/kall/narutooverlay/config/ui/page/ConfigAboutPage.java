package me.kall.narutooverlay.config.ui.page;

import me.kall.narutoui.mui.MuiLocalization;

import icyllis.modernui.core.Context;
import icyllis.modernui.view.Gravity;
import icyllis.modernui.widget.Button;
import icyllis.modernui.widget.LinearLayout;
import icyllis.modernui.widget.TextView;
import me.kall.narutoui.mui.MuiControls;
import me.kall.narutoui.mui.MuiSettingsPage;
import me.kall.narutoui.mui.MuiTheme;
import me.kall.narutooverlay.NarutoOverlay;
import me.kall.narutooverlay.config.RenderArgs;
import me.kall.narutooverlay.config.ui.ConfigUiHost;
import me.kall.narutooverlay.config.ui.background.ConfigBackground;
import me.kall.narutooverlay.config.ui.widget.Accent;
import net.minecraft.SharedConstants;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.loading.FMLPaths;
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
    public static void populate(@NotNull MuiSettingsPage page, @NotNull ConfigUiHost host) {
        page.section(MuiLocalization.get(GROUP + "about.info"), section -> section
                .value(MuiLocalization.get(GROUP + "about.version"), version())
                .value(MuiLocalization.get(GROUP + "about.environment"), NarutoOverlay.ENV + " \u00B7 " + SharedConstants.getCurrentVersion().getName() + " \u00B7 " + MuiLocalization.get(GROUP + "about.clientOnly")));

        page.section(MuiLocalization.get(GROUP + "about.background"), section -> {
            String folder = RenderArgs.BACKGROUND_FOLDER.get();
            int count = ConfigBackground.images().size();

            section.value(MuiLocalization.get(GROUP + "background.folder"), folder.isEmpty() ? MuiLocalization.get(GROUP + "background.folder.none") : folder);
            section.text(MuiLocalization.get(GROUP + "background.folder.hint"));
            section.custom(row -> addFolderButtons(row, host, folder));
            section.value(MuiLocalization.get(GROUP + "background.images"),
                    count == 0 ? MuiLocalization.get(GROUP + "background.images.empty") : String.valueOf(count));

            section.slider(MuiLocalization.get(GROUP + "background.interval"), 2, 300,
                    () -> (int) Math.round(RenderArgs.BACKGROUND_INTERVAL.get()),
                    host::setBackgroundInterval,
                    value -> MuiLocalization.get(GROUP + "background.interval.value", value));
            section.slider(MuiLocalization.get(GROUP + "background.fade"), 0, 300,
                    () -> (int) Math.round(RenderArgs.BACKGROUND_FADE.get() * 100.0D),
                    value -> host.setBackgroundFade(value / 100.0D),
                    value -> MuiLocalization.get(GROUP + "background.fade.value", String.format(Locale.ROOT, "%.1f", value / 100.0D)));
            section.slider(MuiLocalization.get(GROUP + "background.dim"), 0, 95,
                    () -> (int) Math.round(RenderArgs.BACKGROUND_DIM.get() * 100.0D),
                    value -> host.setBackgroundDim(value / 100.0D),
                    value -> value + "%");
        });

        Path path = FMLPaths.CONFIGDIR.get().resolve("narutooverlay-client.toml");
        page.section(MuiLocalization.get(GROUP + "about.config"), section -> section
                .value(MuiLocalization.get(GROUP + "about.path"), path.toString())
                .custom(row -> addPathActions(row, path)));

        page.section(MuiLocalization.get(GROUP + "about.usage"), section -> section
                .custom(row -> addHint(row, "narutooverlay.config.about.hint.drag"))
                .custom(row -> addHint(row, "narutooverlay.config.about.hint.merge"))
                .custom(row -> addHint(row, "narutooverlay.config.about.hint.split"))
                .custom(row -> addHint(row, "narutooverlay.config.about.hint.open"))
                .custom(row -> addHint(row, "narutooverlay.config.about.hint.reset")));

        page.custom(context -> {
            TextView footer = MuiControls.text(context.context(), context.theme(), MuiLocalization.get(GROUP + "about.footer"), 11, context.theme().onSurfaceVariant());
            footer.setGravity(Gravity.CENTER_HORIZONTAL);
            LinearLayout.LayoutParams params = MuiControls.fill();
            params.topMargin = MuiControls.dp(context.context(), 28);
            context.content().addView(footer, params);
        });
    }

    private static void addFolderButtons(@NotNull LinearLayout row, @NotNull ConfigUiHost host, @NotNull String folder) {
        Context context = row.getContext();
        MuiTheme theme = MuiTheme.modern(context);
        LinearLayout buttons = MuiControls.row(context);
        buttons.setPadding(0, 0, 0, 0);

        Button choose = MuiControls.button(context, theme, MuiLocalization.get(GROUP + "background.folder.choose"), true, view -> host.chooseBackgroundFolder());
        Button clear = MuiControls.button(context, theme, MuiLocalization.get(GROUP + "background.folder.clear"), true, view -> host.clearBackgroundFolder());
        clear.setEnabled(!folder.isEmpty());

        LinearLayout.LayoutParams chooseParams = new LinearLayout.LayoutParams(0, MuiControls.WRAP, 1.0F);
        chooseParams.setMargins(0, 0, MuiControls.dp(context, 8), 0);
        buttons.addView(choose, chooseParams);
        buttons.addView(clear, new LinearLayout.LayoutParams(0, MuiControls.WRAP, 1.0F));
        row.addView(buttons, MuiControls.fill());
    }

    private static void addPathActions(@NotNull LinearLayout row, @NotNull Path path) {
        Context context = row.getContext();
        MuiTheme theme = MuiTheme.modern(context);
        LinearLayout actions = MuiControls.row(context);
        actions.setPadding(0, 0, 0, 0);

        TextView copy = MuiControls.text(context, theme, MuiLocalization.get(GROUP + "about.copyPath"), 12, Accent.primary());
        copy.setOnClickListener(view -> {
            Minecraft.getInstance().keyboardHandler.setClipboard(path.toString());
            copy.setText(MuiLocalization.get(GROUP + "about.copied"));
        });

        TextView open = MuiControls.text(context, theme, MuiLocalization.get(GROUP + "about.openFolder"), 12, Accent.primary());
        open.setGravity(Gravity.END);
        open.setOnClickListener(view -> reveal(path));

        actions.addView(copy, MuiControls.weight());
        LinearLayout.LayoutParams openParams = MuiControls.wrap();
        openParams.leftMargin = MuiControls.dp(context, 12);
        actions.addView(open, openParams);
        row.addView(actions, MuiControls.fill());
    }

    private static void addHint(@NotNull LinearLayout row, @NotNull String key) {
        Context context = row.getContext();
        MuiTheme theme = MuiTheme.modern(context);
        LinearLayout hint = MuiControls.column(context);
        hint.setPadding(MuiControls.dp(context, 2), MuiControls.dp(context, 8), MuiControls.dp(context, 2), MuiControls.dp(context, 8));
        hint.addView(MuiControls.text(context, theme, MuiLocalization.get(key), 13, theme.onSurface()), MuiControls.fill());
        TextView description = MuiControls.text(context, theme, MuiLocalization.get(key + ".desc"), 11, theme.onSurfaceVariant());
        LinearLayout.LayoutParams descriptionParams = MuiControls.fill();
        descriptionParams.topMargin = MuiControls.dp(context, 3);
        hint.addView(description, descriptionParams);
        row.addView(hint, MuiControls.fill());
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
        return ModList.get().getModContainerById("narutooverlay")
                .map(container -> container.getModInfo().getVersion().toString())
                .orElse("?");
    }
}