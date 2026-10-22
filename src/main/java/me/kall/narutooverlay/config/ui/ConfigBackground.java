package me.kall.narutooverlay.config.ui;

import icyllis.modernui.graphics.BitmapFactory;
import icyllis.modernui.graphics.Image;
import icyllis.modernui.mc.MuiModApi;
import me.kall.narutooverlay.config.RenderArgs;
import net.minecraftforge.fml.loading.FMLPaths;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Locale;
import java.util.concurrent.CompletableFuture;

public final class ConfigBackground {
    public static final String BASE_NAME = "narutooverlay-config-bg";

    private static final Logger LOGGER = LogManager.getLogger("NarutoOverlay");

    private static @Nullable Image image;
    private static String loaded = "";

    private ConfigBackground() {}

    public static void installDefault() {
        Path target = file(RenderArgs.DEFAULT_BACKGROUND_IMAGE);
        if (Files.exists(target)) return;

        try (InputStream logo = ConfigBackground.class.getResourceAsStream("/logo.jpg")) {
            if (logo == null) {
                LOGGER.error("Could not find the bundled logo for the configuration screen background");
                return;
            }
            Files.createDirectories(target.getParent());
            Files.copy(logo, target);
        } catch (IOException error) {
            LOGGER.error("Could not install the default configuration screen background at {}", target, error);
        }
    }

    public static @NotNull Path file(@NotNull String name) {
        return FMLPaths.GAMEDIR.get().resolve(name);
    }

    public static @NotNull Path file() {
        return file(RenderArgs.BACKGROUND_IMAGE.get());
    }

    public static @Nullable Image image() {
        return image;
    }

    public static @NotNull String copyInto(@NotNull Path source) throws IOException {
        String name = BASE_NAME + extensionOf(source.getFileName().toString());
        Path target = file(name).toAbsolutePath().normalize();
        if (!source.toAbsolutePath().normalize().equals(target)) {
            Files.createDirectories(target.getParent());
            Files.copy(source, target, StandardCopyOption.REPLACE_EXISTING);
        }
        return name;
    }

    private static @NotNull String extensionOf(@NotNull String fileName) {
        int dot = fileName.lastIndexOf('.');
        if (dot < 0 || dot == fileName.length() - 1) return ".png";
        String extension = fileName.substring(dot).toLowerCase(Locale.ROOT);
        return extension.matches("\\.[a-z0-9]{1,5}") ? extension : ".png";
    }

    public static void loadAsync(@NotNull Runnable onReady) {
        String name = RenderArgs.BACKGROUND_IMAGE.get();
        if (name.isEmpty()) {
            close();
            loaded = "";
            return;
        }
        if (image != null && name.equals(loaded)) return;

        Path path = file(name);
        if (!Files.isReadable(path)) {
            close();
            loaded = "";
            return;
        }

        CompletableFuture.supplyAsync(() -> {
            try {
                return BitmapFactory.decodePath(path);
            } catch (Throwable ignored) {
                return null;
            }
        }).whenComplete((bitmap, error) -> {
            if (bitmap == null) return;
            MuiModApi.postToUiThread(() -> {
                close();
                Image created = Image.createTextureFromBitmap(bitmap);
                bitmap.close();
                if (created != null) {
                    image = created;
                    loaded = name;
                }
                onReady.run();
            });
        });
    }

    public static void invalidate() {
        close();
        loaded = "";
    }

    private static void close() {
        if (image != null) {
            image.close();
            image = null;
        }
    }
}
