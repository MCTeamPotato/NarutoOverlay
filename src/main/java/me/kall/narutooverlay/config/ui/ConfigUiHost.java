package me.kall.narutooverlay.config.ui;

import icyllis.modernui.core.Context;
import icyllis.modernui.view.View;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Host contract shared by the legacy {@code ConfigFragment} and the Modern UI shell adapter.
 * Pages use it for overlays, theme refreshes, and background actions.
 */
public interface ConfigUiHost extends BackgroundHost {
    @NotNull Context requireContext();

    void showOverlay(@NotNull View overlay);

    default void removeOverlay(@NotNull View overlay) {}

    void onThemeChanged();

    void reloadCurrentPage();

    static @Nullable ConfigUiHost active() {
        return Holder.ACTIVE;
    }

    static void activate(@Nullable ConfigUiHost host) {
        Holder.ACTIVE = host;
    }

    final class Holder {
        private static @Nullable ConfigUiHost ACTIVE;

        private Holder() {}
    }
}