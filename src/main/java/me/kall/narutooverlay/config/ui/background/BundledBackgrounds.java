package me.kall.narutooverlay.config.ui.background;

import me.kall.narutooverlay.config.RenderArgs;
import net.minecraftforge.fml.loading.FMLPaths;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * Extracts the pictures bundled in the mod jar into {@code <game dir>/narutooverlay}.
 * Existing files are never overwritten, so players can replace or edit them.
 */
public final class BundledBackgrounds {
    public static final String FOLDER = RenderArgs.DEFAULT_BACKGROUND_FOLDER;
    public static final List<String> NAMES = RenderArgs.DEFAULT_BACKGROUND_ORDER;

    private static final Logger LOGGER = LogManager.getLogger("NarutoOverlay");
    private static final List<Asset> ASSETS = List.of(
            new Asset("bg1.jpg", "/assets/narutooverlay/textures/bg1.jpg"),
            new Asset("bg2.jpg", "/assets/narutooverlay/textures/bg2.jpg"),
            new Asset("logo.jpg", "/logo.jpg")
    );

    private record Asset(@NotNull String name, @NotNull String resource) {}

    private BundledBackgrounds() {}

    public static void install() {
        Path folder = folder();
        try {
            Files.createDirectories(folder);
        } catch (IOException error) {
            LOGGER.error("Could not create the bundled background folder {}", folder, error);
            return;
        }

        for (Asset asset : ASSETS) {
            copy(asset, folder.resolve(asset.name()));
        }
    }

    public static @NotNull Path folder() {
        return FMLPaths.GAMEDIR.get().resolve(FOLDER);
    }

    public static @NotNull List<Path> files() {
        List<Path> paths = new ArrayList<>(NAMES.size());
        for (String name : NAMES) {
            Path path = folder().resolve(name);
            if (Files.isRegularFile(path)) paths.add(path);
        }
        return paths;
    }

    public static boolean available() {
        return !files().isEmpty();
    }

    private static void copy(@NotNull Asset asset, @NotNull Path target) {
        if (Files.exists(target)) return;

        try (InputStream input = BundledBackgrounds.class.getResourceAsStream(asset.resource())) {
            if (input == null) {
                LOGGER.error("Missing bundled background resource {}", asset.resource());
                return;
            }
            Files.copy(input, target);
        } catch (IOException error) {
            LOGGER.error("Could not extract the bundled background {} to {}", asset.resource(), target, error);
        }
    }
}