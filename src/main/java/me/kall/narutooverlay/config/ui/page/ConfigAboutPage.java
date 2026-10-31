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

public final class ConfigAboutPage {
    @SuppressWarnings("UnnecessaryUnicodeEscape")
    public static void populate(@NotNull MuiSettingsPage page, @NotNull MuiBackgroundActions background) {
        Path path = FMLPaths.CONFIGDIR.get().resolve("narutooverlay-client.toml");

        page.section("narutooverlay.config.about.info", section -> section.value("narutooverlay.config.about.version", ModList.get().getModContainerById(NarutoOverlay.MOD_ID).map(container -> container.getModInfo().getVersion().toString()).orElse("?"))
                .value("narutooverlay.config.about.environment", NarutoOverlay.ENV + " \u00B7 " + SharedConstants.getCurrentVersion().getName() + " \u00B7 " + I18n.get("narutooverlay.config.about.clientOnly")))
                .section("narutooverlay.config.about.background", section -> {
                    String folder = background.state().folder();
                    int count = background.images().size();

                    section.value("narutooverlay.config.background.folder", folder.isEmpty() ? I18n.get("narutooverlay.config.background.folder.none") : folder)
                            .text("narutooverlay.config.background.folder.hint")
                            .customLayout(row -> {
                                Context context = row.getContext();
                                MuiTheme theme = row.theme();

                                Button choose = MuiControls.button(context, theme, "narutooverlay.config.background.folder.choose", true, view -> background.chooseBackgroundFolder());
                                Button clear = MuiControls.button(context, theme, "narutooverlay.config.background.folder.clear", true, view -> background.clearBackgroundFolder());
                                clear.setEnabled(!folder.isEmpty());

                                row.add(MuiLayout.row(context, theme).padding(0).spacing(8).addWeighted(choose, 1.0F).addWeighted(clear, 1.0F), MuiControls.fill());
                            })
                            .value("narutooverlay.config.background.images", count == 0 ? I18n.get("narutooverlay.config.background.images.empty") : String.valueOf(count))
                            .slider("narutooverlay.config.background.interval", 2, 300, () -> (int) Math.round(background.state().interval()), background::setBackgroundInterval, value -> I18n.get("narutooverlay.config.background.interval.value", value))
                            .slider("narutooverlay.config.background.fade", 0, 300, () -> (int) Math.round(background.state().fade() * 100.0D), value -> background.setBackgroundFade(value / 100.0D), value -> I18n.get("narutooverlay.config.background.fade.value", String.format(Locale.ROOT, "%.1f", value / 100.0D)))
                            .slider("narutooverlay.config.background.dim", 0, 95, () -> (int) Math.round(background.state().dim() * 100.0D), value -> background.setBackgroundDim(value / 100.0D), value -> value + "%");
                })
                .section("narutooverlay.config.about.config", section -> section
                        .value("narutooverlay.config.about.path", path.toString())
                        .customLayout(row -> {
                            Context context = row.getContext();
                            MuiTheme theme = row.theme();
                            MuiLayout actions = MuiLayout.row(context, theme).padding(0);

                            MuiTextAction copy = MuiTextAction.create(context, theme, "narutooverlay.config.about.copyPath");
                            copy.onClick(() -> {
                                Minecraft.getInstance().keyboardHandler.setClipboard(path.toString());
                                copy.flash("narutooverlay.config.about.copied", 1000);
                            });

                            MuiTextAction open = MuiTextAction.create(context, theme, "narutooverlay.config.about.openFolder", () -> {
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
                            });

                            actions.add(copy, MuiControls.weight());
                            LinearLayout.LayoutParams openParams = MuiControls.wrap();
                            openParams.leftMargin = MuiControls.dp(context, 12);
                            actions.add(open, openParams);
                            row.add(actions, MuiControls.fill());
                        }))
                .section("narutooverlay.config.about.usage", section -> section
                        .note("narutooverlay.config.about.hint.drag", "narutooverlay.config.about.hint.drag.desc")
                        .note("narutooverlay.config.about.hint.merge", "narutooverlay.config.about.hint.merge.desc")
                        .note("narutooverlay.config.about.hint.split", "narutooverlay.config.about.hint.split.desc")
                        .note("narutooverlay.config.about.hint.open", "narutooverlay.config.about.hint.open.desc")
                        .note("narutooverlay.config.about.hint.reset", "narutooverlay.config.about.hint.reset.desc"))
                .custom(context -> {
                    TextView footer = MuiControls.text(context.context(), context.theme(), "narutooverlay.config.about.footer", 11, context.theme().onSurfaceVariant());
                    footer.setGravity(Gravity.CENTER_HORIZONTAL);
                    LinearLayout.LayoutParams params = MuiControls.fill();
                    params.topMargin = MuiControls.dp(context.context(), 28);
                    context.content().addView(footer, params);
                });
    }
}