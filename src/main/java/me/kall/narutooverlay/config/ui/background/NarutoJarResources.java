package me.kall.narutooverlay.config.ui.background;

import me.kall.narutooverlay.config.RenderArgs;
import net.minecraftforge.fml.loading.FMLPaths;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class NarutoJarResources {
    public static final String FOLDER = RenderArgs.DEFAULT_BACKGROUND_FOLDER;
    public static final List<String> NAMES = RenderArgs.DEFAULT_BACKGROUND_ORDER;

    private static final List<Asset> ASSETS = List.of(
            new Asset("bg1.jpg", "/assets/narutooverlay/textures/bg1.jpg"),
            new Asset("bg2.jpg", "/assets/narutooverlay/textures/bg2.jpg"),
            new Asset("logo.jpg", "/logo.jpg")
    );

    private record Asset(@NotNull String name, @NotNull String resource) {}

    public static void install() {
        Path folder = folder();
        try {
            Files.createDirectories(folder);
        } catch (IOException ignored) {
            return;
        }

        for (Asset asset : ASSETS) copy(asset, folder.resolve(asset.name()));
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

    private static void copy(Asset asset, Path target) {
        if (Files.exists(target)) return;

        try (InputStream input = NarutoJarResources.class.getResourceAsStream(asset.resource())) {
            if (input == null) return;
            Files.copy(input, target);
        } catch (IOException ignored) {}
    }
}
