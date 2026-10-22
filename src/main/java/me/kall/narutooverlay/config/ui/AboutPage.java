package me.kall.narutooverlay.config.ui;

import icyllis.modernui.core.Context;
import icyllis.modernui.view.Gravity;
import icyllis.modernui.widget.Button;
import icyllis.modernui.widget.LinearLayout;
import icyllis.modernui.widget.SeekBar;
import icyllis.modernui.widget.TextView;
import me.kall.narutooverlay.NarutoOverlay;
import me.kall.narutooverlay.config.RenderArgs;
import net.minecraft.SharedConstants;
import net.minecraft.client.Minecraft;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.loading.FMLPaths;
import org.jetbrains.annotations.NotNull;

import java.nio.file.Path;

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
        copy.setPadding(0, UiKit.dp(this.context, 8), 0, 0);
        copy.setOnClickListener(view -> {
            Minecraft.getInstance().keyboardHandler.setClipboard(path.toString());
            copy.setText(UiKit.str(GROUP + "about.copied"));
        });
        config.addView(copy, UiKit.wrap());

        LinearLayout hints = section("narutooverlay.config.about.usage");
        hints.addView(hint("narutooverlay.config.about.hint.drag"), UiKit.fill());
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

        String name = RenderArgs.BACKGROUND_IMAGE.get();
        background.addView(UiKit.valueRow(this.context, UiKit.str(GROUP + "background.current"),
                name.isEmpty() ? UiKit.str(GROUP + "background.none") : name), UiKit.fill());
        background.addView(UiKit.divider(this.context), dividerParams());

        LinearLayout buttons = UiKit.row(this.context);
        buttons.setPadding(UiKit.dp(this.context, 2), UiKit.dp(this.context, 6),
                UiKit.dp(this.context, 2), UiKit.dp(this.context, 6));

        Button choose = UiKit.button(this.context, UiKit.str(GROUP + "background.choose"), true,
                view -> this.host.chooseBackgroundImage());
        Button clear = UiKit.button(this.context, UiKit.str(GROUP + "background.clear"), true,
                view -> this.host.clearBackgroundImage());
        clear.setEnabled(!name.isEmpty());

        LinearLayout.LayoutParams chooseParams = new LinearLayout.LayoutParams(0, UiKit.WRAP, 1.0F);
        chooseParams.setMargins(0, 0, UiKit.dp(this.context, 8), 0);
        buttons.addView(choose, chooseParams);
        buttons.addView(clear, new LinearLayout.LayoutParams(0, UiKit.WRAP, 1.0F));
        background.addView(buttons, UiKit.fill());

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
