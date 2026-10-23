package me.kall.narutooverlay.config.ui.widget;

import icyllis.modernui.mc.MuiModApi;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.language.I18n;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.PointerBuffer;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.util.tinyfd.TinyFileDialogs;

import java.awt.FileDialog;
import java.awt.Frame;
import java.awt.GraphicsEnvironment;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Locale;
import javax.swing.JFileChooser;
import javax.swing.SwingUtilities;

public final class ImagePicker {
    private static final Logger LOGGER = LogManager.getLogger("NarutoOverlay");

    public interface Callback {
        void onPicked(@NotNull Path path);

        default void onFailed() {}
    }

    private ImagePicker() {}

    public static void open(@NotNull Callback callback) {
        LOGGER.info("Opening the background image picker");
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.isSameThread()) {
            run(callback);
        } else {
            minecraft.execute(() -> run(callback));
        }
    }

    private static void run(@NotNull Callback callback) {
        String picked = null;
        boolean failed = false;
        try {
            picked = openNativeDialog();
        } catch (Throwable error) {
            failed = true;
            LOGGER.warn("Could not open the native file dialog, falling back to AWT", error);
        }

        if (picked != null && !picked.isEmpty()) {
            LOGGER.info("Picked background image: {}", picked);
            Path path = Path.of(picked);
            MuiModApi.postToUiThread(() -> callback.onPicked(path));
            return;
        }
        if (failed) {
            openAwtDialog(callback);
        }

    }

    public static void openFolder(@NotNull Callback callback) {
        openFolder(null, callback);
    }

    public static void openFolder(@Nullable Path initial, @NotNull Callback callback) {
        LOGGER.info("Opening the background folder picker");
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.isSameThread()) {
            runFolder(initial, callback);
        } else {
            minecraft.execute(() -> runFolder(initial, callback));
        }
    }

    private static void runFolder(@Nullable Path initial, @NotNull Callback callback) {
        String picked;
        try {
            picked = openNativeFolderDialog(initial);
        } catch (Throwable error) {
            LOGGER.warn("Could not open the native folder dialog, falling back to Swing", error);
            openSwingFolderDialog(callback);
            return;
        }

        if (picked == null || picked.isEmpty()) return;
        try {
            Path path = Path.of(picked);
            MuiModApi.postToUiThread(() -> callback.onPicked(path));
        } catch (RuntimeException error) {
            LOGGER.error("Invalid folder selected: {}", picked, error);
            MuiModApi.postToUiThread(callback::onFailed);
        }
    }

    private static @Nullable String openNativeFolderDialog(@Nullable Path initial) {
        // LWJGL uses the null-unsafe nUTF8() for this parameter, so it must never be null.
        String defaultPath = initial != null && Files.isDirectory(initial)
                ? initial.toString()
                : Minecraft.getInstance().gameDirectory.getAbsolutePath();
        return TinyFileDialogs.tinyfd_selectFolderDialog(
                I18n.get("narutooverlay.config.background.folder.picker"), defaultPath);
    }

    private static void openSwingFolderDialog(@NotNull Callback callback) {
        if (GraphicsEnvironment.isHeadless()) {
            LOGGER.warn("Cannot open the folder picker: AWT is headless");
            MuiModApi.postToUiThread(callback::onFailed);
            return;
        }

        SwingUtilities.invokeLater(() -> {
            try {
                JFileChooser chooser = new JFileChooser();
                chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
                chooser.setDialogTitle(I18n.get("narutooverlay.config.background.folder.picker"));
                if (chooser.showOpenDialog(null) != JFileChooser.APPROVE_OPTION) return;

                Path path = chooser.getSelectedFile().toPath();
                MuiModApi.postToUiThread(() -> callback.onPicked(path));
            } catch (Throwable error) {
                LOGGER.error("Could not open the folder picker", error);
                MuiModApi.postToUiThread(callback::onFailed);
            }
        });
    }

    private static @Nullable String openNativeDialog() {
        try (MemoryStack stack = MemoryStack.stackPush()) {
            PointerBuffer filters = stack.mallocPointer(4);
            filters.put(stack.UTF8("*.png"));
            filters.put(stack.UTF8("*.jpg"));
            filters.put(stack.UTF8("*.jpeg"));
            filters.put(stack.UTF8("*.bmp"));
            filters.flip();
            return TinyFileDialogs.tinyfd_openFileDialog(null, null, filters,
                    I18n.get("narutooverlay.config.background.filter"), false);
        }
    }

    private static void openAwtDialog(@NotNull Callback callback) {
        Thread thread = new Thread(() -> {
            try {
                if (GraphicsEnvironment.isHeadless()) {
                    LOGGER.warn("Cannot open the image picker: AWT is headless");
                    MuiModApi.postToUiThread(callback::onFailed);
                    return;
                }

                FileDialog dialog = new FileDialog((Frame) null,
                        I18n.get("narutooverlay.config.background.picker"), FileDialog.LOAD);
                dialog.setAlwaysOnTop(true);
                dialog.setMultipleMode(false);
                dialog.setFilenameFilter((dir, name) -> isImage(name));
                dialog.setFile("*.png;*.jpg;*.jpeg;*.bmp;*.gif");
                dialog.setVisible(true);

                String file = dialog.getFile();
                String directory = dialog.getDirectory();
                dialog.dispose();

                if (file == null || directory == null) return;
                Path path = Path.of(directory, file);
                MuiModApi.postToUiThread(() -> callback.onPicked(path));
            } catch (Throwable error) {
                LOGGER.error("Could not open the image picker", error);
                MuiModApi.postToUiThread(callback::onFailed);
            }
        }, "NarutoOverlay-ImagePicker");
        thread.setDaemon(true);
        thread.start();
    }

    private static boolean isImage(@NotNull String name) {
        String lower = name.toLowerCase(Locale.ROOT);
        return lower.endsWith(".png") || lower.endsWith(".jpg") || lower.endsWith(".jpeg")
                || lower.endsWith(".bmp") || lower.endsWith(".gif") || lower.endsWith(".tga");
    }
}
