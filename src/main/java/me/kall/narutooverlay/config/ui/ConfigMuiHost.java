package me.kall.narutooverlay.config.ui;

import icyllis.modernui.core.Context;
import icyllis.modernui.view.View;
import me.kall.narutoui.mui.MuiConfigFragment;
import me.kall.narutoui.mui.shimmer.MuiShimmer;
import me.kall.narutoui.mui.shimmer.ShimmerTarget;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

/**
 * Adapts the {@link MuiConfigFragment} shell to the config pages' host contract.
 */
public final class ConfigMuiHost implements ConfigUiHost {
    private final ConfigMuiBackground background;
    private final List<ShimmerTarget> chromeShimmer = new ArrayList<>();
    private @Nullable MuiConfigFragment fragment;

    public ConfigMuiHost() {
        this.background = new ConfigMuiBackground(this);
    }

    public @NotNull ConfigMuiBackground background() {
        return this.background;
    }

    public void addChromeShimmer(@NotNull ShimmerTarget target) {
        this.chromeShimmer.add(target);
        MuiShimmer.attach(target);
    }

    public void attach(@Nullable MuiConfigFragment fragment) {
        if (fragment == null) {
            for (ShimmerTarget target : this.chromeShimmer) MuiShimmer.detach(target);
            this.chromeShimmer.clear();
        }
        this.fragment = fragment;
    }

    @Override
    public @NotNull Context requireContext() {
        if (this.fragment == null) throw new IllegalStateException("Config screen is not attached");
        return this.fragment.requireContext();
    }

    @Override
    public void showOverlay(@NotNull View overlay) {
        if (this.fragment != null) this.fragment.showOverlay(overlay);
    }

    @Override
    public void removeOverlay(@NotNull View overlay) {
        if (this.fragment != null) this.fragment.removeOverlay(overlay);
    }

    @Override
    public void onThemeChanged() {
        this.reloadCurrentPage();
    }

    @Override
    public void reloadCurrentPage() {
        if (this.fragment != null) this.fragment.reloadCurrentPage();
    }

    @Override
    public void chooseBackgroundImage() {
        this.background.chooseBackgroundImage();
    }

    @Override
    public void clearBackgroundImage() {
        this.background.clearBackgroundImage();
    }

    @Override
    public void chooseBackgroundFolder() {
        this.background.chooseBackgroundFolder();
    }

    @Override
    public void clearBackgroundFolder() {
        this.background.clearBackgroundFolder();
    }

    @Override
    public void setBackgroundInterval(double seconds) {
        this.background.setBackgroundInterval(seconds);
    }

    @Override
    public void setBackgroundFade(double seconds) {
        this.background.setBackgroundFade(seconds);
    }

    @Override
    public void setBackgroundDim(double dim) {
        this.background.setBackgroundDim(dim);
    }
}