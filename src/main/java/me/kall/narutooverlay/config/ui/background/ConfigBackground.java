package me.kall.narutooverlay.config.ui.background;

import icyllis.modernui.graphics.Bitmap;
import icyllis.modernui.graphics.BitmapFactory;
import icyllis.modernui.graphics.Image;
import icyllis.modernui.mc.MuiModApi;
import me.kall.narutooverlay.config.ConfigSaver;
import me.kall.narutooverlay.config.RenderArgs;
import net.minecraftforge.fml.loading.FMLPaths;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Stream;

public final class ConfigBackground {
    public static final String BASE_NAME = "narutooverlay-config-bg";

    private static final Logger LOGGER = LogManager.getLogger("NarutoOverlay");

    private static @Nullable Image image;
    private static String loaded = "";

    private ConfigBackground() {}

    public static void installDefault() {
        BundledBackgrounds.install();
    }

    /**
     * Moves an old default single-image configuration over to the bundled slideshow folder.
     * Only touches the exact legacy default, so custom images and folders are left alone.
     */
    public static void migrateBundledDefaults() {
        if (!BundledBackgrounds.available()) return;
        if (!RenderArgs.BACKGROUND_FOLDER.get().isBlank()) return;
        if (!"narutooverlay-config-bg.png".equals(RenderArgs.BACKGROUND_IMAGE.get())) return;

        RenderArgs.BACKGROUND_FOLDER.set(BundledBackgrounds.FOLDER);
        RenderArgs.BACKGROUND_ORDER.set(new ArrayList<>(BundledBackgrounds.NAMES));
        RenderArgs.BACKGROUND_IMAGE.set(RenderArgs.DEFAULT_BACKGROUND_IMAGE);
        ConfigSaver.changed();
    }

    public static @NotNull Path file(@NotNull String name) {
        return FMLPaths.GAMEDIR.get().resolve(name);
    }

    public static @NotNull Path file() {
        return file(RenderArgs.BACKGROUND_IMAGE.get());
    }

    /**
     * Resolves the configured slideshow folder. Relative paths are resolved against the game directory.
     */
    public static @NotNull Path folder() {
        String configured = RenderArgs.BACKGROUND_FOLDER.get();
        try {
            Path path = Path.of(configured);
            return path.isAbsolute() ? path.normalize() : FMLPaths.GAMEDIR.get().resolve(path).normalize();
        } catch (RuntimeException ignored) {
            return FMLPaths.GAMEDIR.get().resolve("__invalid-background-folder__");
        }
    }

    /**
     * Stores the selected folder in a portable form: relative to the game directory when possible.
     */
    public static @NotNull String store(@NotNull Path selected) {
        Path gameDir = FMLPaths.GAMEDIR.get().toAbsolutePath().normalize();
        Path path = selected.toAbsolutePath().normalize();
        if (path.equals(gameDir)) return ".";
        if (path.startsWith(gameDir)) {
            return gameDir.relativize(path).toString().replace('\\', '/');
        }
        return path.toString();
    }

    /**
     * Lists the pictures of the slideshow folder in playback order.
     * Pictures named in {@code backgroundOrder} come first in that order, the rest follow alphabetically.
     */
    public static @NotNull List<Path> images() {
        if (RenderArgs.BACKGROUND_FOLDER.get().isBlank()) return List.of();

        Path folder = folder();
        if (!Files.isDirectory(folder)) return List.of();

        List<Path> found = new ArrayList<>();
        try (Stream<Path> stream = Files.list(folder)) {
            stream.filter(Files::isRegularFile)
                    .filter(path -> isImage(path.getFileName().toString()))
                    .forEach(found::add);
        } catch (IOException error) {
            LOGGER.warn("Could not list the background folder {}", folder, error);
            return List.of();
        }
        found.sort(Comparator.comparing(path -> path.getFileName().toString().toLowerCase(Locale.ROOT)));

        List<? extends String> order = RenderArgs.BACKGROUND_ORDER.get();
        if (order.isEmpty()) return found;

        List<Path> ordered = new ArrayList<>(found.size());
        for (String name : order) {
            String wanted = name == null ? "" : name.trim();
            if (wanted.isEmpty()) continue;

            for (Iterator<Path> iterator = found.iterator(); iterator.hasNext(); ) {
                Path path = iterator.next();
                if (path.getFileName().toString().equalsIgnoreCase(wanted)) {
                    ordered.add(path);
                    iterator.remove();
                    break;
                }
            }
        }
        ordered.addAll(found);
        return ordered;
    }

    private static boolean isImage(@NotNull String name) {
        String lower = name.toLowerCase(Locale.ROOT);
        return lower.endsWith(".png") || lower.endsWith(".jpg") || lower.endsWith(".jpeg")
                || lower.endsWith(".bmp") || lower.endsWith(".gif");
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

    /**
     * Decodes a picture off the render thread. The future completes with {@code null} when the file
     * cannot be decoded, so callers only need to handle the success path.
     */
    public static @NotNull CompletableFuture<Bitmap> decode(@NotNull Path path) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                return BitmapFactory.decodePath(path);
            } catch (Throwable ignored) {
                return null;
            }
        });
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

        decode(path).whenComplete((bitmap, error) -> {
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
