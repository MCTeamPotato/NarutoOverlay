package me.kall.narutooverlay.config.ui;

import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;

/**
 * Reflective gate for the optional NarutoUI-backed configuration screen.
 *
 * <p>This class must not reference NarutoUI or Modern UI types directly. It is safe to load when
 * either optional mod is missing, while {@link ConfigUi} is only loaded after both are present.</p>
 */
public final class NarutoUiCompat {
    private static final Logger LOGGER = LogManager.getLogger("NarutoOverlay");
    private static final String CONFIG_UI = "me.kall.narutooverlay.config.ui.ConfigUi";

    private NarutoUiCompat() {}

    /**
     * Installs the optional configuration screen.
     *
     * @param context mod loading context
     */
    public static void integrate(@NotNull FMLJavaModLoadingContext context) {
        invoke("install", new Class<?>[]{FMLJavaModLoadingContext.class}, context);
    }

    /**
     * Migrates bundled background defaults when the optional configuration screen is available.
     */
    public static void migrateBundledDefaults() {
        invoke("migrateBundledDefaults", new Class<?>[0]);
    }

    private static void invoke(@NotNull String method, @NotNull Class<?>[] parameterTypes, @NotNull Object... arguments) {
        try {
            Class.forName(CONFIG_UI).getMethod(method, parameterTypes).invoke(null, arguments);
        } catch (ReflectiveOperationException | LinkageError error) {
            LOGGER.warn("NarutoUI configuration screen is unavailable", error);
        }
    }
}