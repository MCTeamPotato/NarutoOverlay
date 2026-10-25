package me.kall.narutooverlay.config.ui.page;

import me.kall.narutooverlay.config.ui.BackgroundHost;
import me.kall.narutooverlay.config.ui.background.ConfigBackground;
import me.kall.narutooverlay.config.ui.widget.UiKit;
import icyllis.modernui.core.Context;
import icyllis.modernui.mc.ui.ThemeControl;
import icyllis.modernui.view.Gravity;
import icyllis.modernui.widget.Button;
import icyllis.modernui.widget.LinearLayout;
import icyllis.modernui.widget.SeekBar;
import icyllis.modernui.widget.TextView;
import me.kall.narutooverlay.NarutoOverlay;
import me.kall.narutooverlay.config.RenderArgs;
import net.minecraft.SharedConstants;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.loading.FMLPaths;
import org.jetbrains.annotations.NotNull;

import java.nio.file.Path;
import java.util.Locale;

public final class AboutPage extends Page {
    private static final String GROUP = "narutooverlay.config.";

    private final BackgroundHost host;

    public AboutPage(@NotNull Context context, @NotNull BackgroundHost host) {
        super(context);
        this.host = host;
        build();
    }

    @Override
    protected void populate() {
        header("narutooverlay.config.about.title", "narutooverlay.config.about.subtitle");

        LinearLayout info = section("narutooverlay.config.about.info");
        info.addView(UiKit.valueRow(this.context, UiKit.str(GROUP + "about.version"), version()), UiKit.fill());
        info.addView(UiKit.divider(this.context), dividerParams());
        info.addView(UiKit.valueRow(this.context, UiKit.str(GROUP + "about.environment"), NarutoOverlay.ENV + " · " + SharedConstants.getCurrentVersion().getName() + " · " + UiKit.str(GROUP + "about.clientOnly")), UiKit.fill());

        addBackgroundSection();

        LinearLayout config = section("narutooverlay.config.about.config");
        Path path = FMLPaths.CONFIGDIR.get().resolve("narutooverlay-client.toml");

        TextView pathView = UiKit.text(this.context, path.toString(), 11, UiKit.onSurfaceVariant(this.context));
        LinearLayout.LayoutParams pathParams = UiKit.fill();
        pathParams.topMargin = UiKit.dp(this.context, 2);
        config.addView(pathView, pathParams);

        TextView copy = UiKit.text(this.context, UiKit.str(GROUP + "about.copyPath"), 12, UiKit.primary(this.context));
        copy.setOnClickListener(view -> {
            Minecraft.getInstance().keyboardHandler.setClipboard(path.toString());
            copy.setText(UiKit.str(GROUP + "about.copied"));
        });

        TextView open = UiKit.text(this.context, UiKit.str(GROUP + "about.openFolder"), 12, UiKit.primary(this.context));
        open.setGravity(Gravity.END);
        open.setOnClickListener(view -> reveal(path));

        LinearLayout actions = UiKit.row(this.context);
        LinearLayout.LayoutParams actionParams = UiKit.fill();
        actionParams.topMargin = UiKit.dp(this.context, 4);
        config.addView(actions, actionParams);

        actions.addView(copy, UiKit.weight());
        LinearLayout.LayoutParams openParams = UiKit.wrap();
        openParams.leftMargin = UiKit.dp(this.context, 12);
        actions.addView(open, openParams);

        LinearLayout hints = section("narutooverlay.config.about.usage");
        hints.addView(hint("narutooverlay.config.about.hint.drag"), UiKit.fill());
        hints.addView(UiKit.divider(this.context), dividerParams());
        hints.addView(hint("narutooverlay.config.about.hint.merge"), UiKit.fill());
        hints.addView(UiKit.divider(this.context), dividerParams());
        hints.addView(hint("narutooverlay.config.about.hint.split"), UiKit.fill());
        hints.addView(UiKit.divider(this.context), dividerParams());
        hints.addView(hint("narutooverlay.config.about.hint.open"), UiKit.fill());
        hints.addView(UiKit.divider(this.context), dividerParams());
        hints.addView(hint("narutooverlay.config.about.hint.reset"), UiKit.fill());

        TextView footer = UiKit.text(this.context, UiKit.str(GROUP + "about.footer"), 11, UiKit.onSurfaceVariant(this.context));
        LinearLayout.LayoutParams footerParams = UiKit.wrap();
        footerParams.gravity = Gravity.CENTER_HORIZONTAL;
        footerParams.topMargin = UiKit.dp(this.context, 28);
        this.content.addView(footer, footerParams);
    }

    private void addBackgroundSection() {
        LinearLayout background = section("narutooverlay.config.about.background");

        String folder = RenderArgs.BACKGROUND_FOLDER.get();
        int count = ConfigBackground.images().size();

        background.addView(UiKit.valueRow(this.context, UiKit.str(GROUP + "background.folder"),
                folder.isEmpty() ? UiKit.str(GROUP + "background.folder.none") : folder), UiKit.fill());

        TextView folderHint = UiKit.text(this.context, UiKit.str(GROUP + "background.folder.hint"), 11,
                UiKit.onSurfaceVariant(this.context));
        LinearLayout.LayoutParams folderHintParams = UiKit.fill();
        folderHintParams.topMargin = UiKit.dp(this.context, 2);
        background.addView(folderHint, folderHintParams);
        background.addView(UiKit.divider(this.context), dividerParams());

        LinearLayout buttons = UiKit.row(this.context);
        buttons.setPadding(UiKit.dp(this.context, 2), UiKit.dp(this.context, 6),
                UiKit.dp(this.context, 2), UiKit.dp(this.context, 6));

        Button chooseFolder = UiKit.button(this.context, UiKit.str(GROUP + "background.folder.choose"), true,
                view -> this.host.chooseBackgroundFolder());
        Button clearFolder = UiKit.button(this.context, UiKit.str(GROUP + "background.folder.clear"), true,
                view -> this.host.clearBackgroundFolder());
        clearFolder.setEnabled(!folder.isEmpty());

        LinearLayout.LayoutParams chooseParams = new LinearLayout.LayoutParams(0, UiKit.WRAP, 1.0F);
        chooseParams.setMargins(0, 0, UiKit.dp(this.context, 8), 0);
        buttons.addView(chooseFolder, chooseParams);
        buttons.addView(clearFolder, new LinearLayout.LayoutParams(0, UiKit.WRAP, 1.0F));
        background.addView(buttons, UiKit.fill());

        background.addView(UiKit.divider(this.context), dividerParams());

        background.addView(UiKit.valueRow(this.context, UiKit.str(GROUP + "background.images"),
                count == 0 ? UiKit.str(GROUP + "background.images.empty") : String.valueOf(count)), UiKit.fill());
        background.addView(UiKit.divider(this.context), dividerParams());

        LinearLayout intervalRow = UiKit.row(this.context);
        intervalRow.addView(UiKit.text(this.context, UiKit.str(GROUP + "background.interval"), 14,
                UiKit.onSurface(this.context)), UiKit.weight());

        TextView intervalValue = UiKit.text(this.context,
                UiKit.str(GROUP + "background.interval.value", Math.round(RenderArgs.BACKGROUND_INTERVAL.get())),
                12, UiKit.primary(this.context));
        intervalRow.addView(intervalValue, UiKit.wrap());
        background.addView(intervalRow, UiKit.fill());

        SeekBar interval = UiKit.slider(this.context, 300, (int) Math.round(RenderArgs.BACKGROUND_INTERVAL.get()));
        interval.setMin(2);
        LinearLayout.LayoutParams intervalParams = UiKit.fill();
        intervalParams.setMargins(UiKit.dp(this.context, 2), 0, UiKit.dp(this.context, 2), 0);
        background.addView(interval, intervalParams);
        ThemeControl.setViewAndChildrenEnabled(interval, count > 1);

        interval.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (!fromUser) return;
                intervalValue.setText(UiKit.str(GROUP + "background.interval.value", progress));
                AboutPage.this.host.setBackgroundInterval(progress);
            }
        });

        background.addView(UiKit.divider(this.context), dividerParams());

        LinearLayout fadeRow = UiKit.row(this.context);
        fadeRow.addView(UiKit.text(this.context, UiKit.str(GROUP + "background.fade"), 14,
                UiKit.onSurface(this.context)), UiKit.weight());

        TextView fadeValue = UiKit.text(this.context,
                UiKit.str(GROUP + "background.fade.value",
                        String.format(Locale.ROOT, "%.1f", RenderArgs.BACKGROUND_FADE.get())),
                12, UiKit.primary(this.context));
        fadeRow.addView(fadeValue, UiKit.wrap());
        background.addView(fadeRow, UiKit.fill());

        SeekBar fade = UiKit.slider(this.context, 300, (int) Math.round(RenderArgs.BACKGROUND_FADE.get() * 100.0D));
        LinearLayout.LayoutParams fadeParams = UiKit.fill();
        fadeParams.setMargins(UiKit.dp(this.context, 2), 0, UiKit.dp(this.context, 2), 0);
        background.addView(fade, fadeParams);
        ThemeControl.setViewAndChildrenEnabled(fade, count > 1);

        fade.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (!fromUser) return;
                fadeValue.setText(UiKit.str(GROUP + "background.fade.value",
                        String.format(Locale.ROOT, "%.1f", progress / 100.0D)));
                AboutPage.this.host.setBackgroundFade(progress / 100.0D);
            }
        });

        background.addView(UiKit.divider(this.context), dividerParams());

        LinearLayout dimRow = UiKit.row(this.context);
        dimRow.addView(UiKit.text(this.context, UiKit.str(GROUP + "background.dim"), 14,
                UiKit.onSurface(this.context)), UiKit.weight());

        TextView dimValue = UiKit.text(this.context,
                Math.round(RenderArgs.BACKGROUND_DIM.get() * 100.0D) + "%", 12,
                UiKit.primary(this.context));
        dimRow.addView(dimValue, UiKit.wrap());
        background.addView(dimRow, UiKit.fill());

        SeekBar dim = UiKit.slider(this.context, 95, (int) Math.round(RenderArgs.BACKGROUND_DIM.get() * 100.0D));
        LinearLayout.LayoutParams dimParams = UiKit.fill();
        dimParams.setMargins(UiKit.dp(this.context, 2), 0, UiKit.dp(this.context, 2), 0);
        background.addView(dim, dimParams);

        dim.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (!fromUser) return;
                dimValue.setText(progress + "%");
                AboutPage.this.host.setBackgroundDim(progress / 100.0D);
            }
        });
    }

    private @NotNull LinearLayout hint(@NotNull String key) {
        LinearLayout row = UiKit.column(this.context);
        row.setPadding(UiKit.dp(this.context, 2), UiKit.dp(this.context, 8),
                UiKit.dp(this.context, 2), UiKit.dp(this.context, 8));
        row.addView(UiKit.text(this.context, UiKit.str(key), 13, UiKit.onSurface(this.context)), UiKit.fill());
        row.addView(UiKit.text(this.context, UiKit.str(key + ".desc"), 11, UiKit.onSurfaceVariant(this.context)),
                UiKit.fill());
        return row;
    }

    private void reveal(@NotNull Path path) {
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
            // Fall through to opening the parent folder.
        }

        try {
            Util.getPlatform().openFile(absolute.getParent().toFile());
        } catch (Throwable ignored) {
            // The platform may not expose a file manager; keep the row usable.
        }
    }

    private @NotNull String version() {
        return ModList.get().getModContainerById("narutooverlay")
                .map(container -> container.getModInfo().getVersion().toString())
                .orElse("?");
    }

    private @NotNull LinearLayout.LayoutParams dividerParams() {
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(UiKit.MATCH, UiKit.dp(this.context, 1));
        params.setMargins(0, UiKit.dp(this.context, 2), 0, UiKit.dp(this.context, 2));
        return params;
    }
}
