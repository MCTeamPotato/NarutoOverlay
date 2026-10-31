package me.kall.narutooverlay.config.ui;

import icyllis.modernui.core.Context;
import icyllis.modernui.view.View;
import me.kall.narutooverlay.config.ConfigSaver;
import me.kall.narutooverlay.config.ui.background.NarutoBackgroundSettings;
import me.kall.narutoui.mui.background.MuiConfigurableBackground;
import me.kall.narutoui.mui.screen.MuiConfigFragment;
import me.kall.narutoui.mui.screen.MuiScreenHost;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Wires the configuration pages to the Modern UI shell.
 * <p>
 * This class is only a composition root: it tracks the attached fragment and owns the configurable
 * background. Screen services are implemented against the currently attached
 * {@link MuiConfigFragment}.
 */
final class ConfigScreenHost implements MuiScreenHost {
    private @Nullable MuiConfigFragment fragment;

    ConfigScreenHost() {}

    public @NotNull MuiConfigurableBackground background() {
        return NarutoBackgroundSettings.BACKGROUND;
    }

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
        this.reloadCurrentPage();
        ConfigSaver.changed();
    }
}
