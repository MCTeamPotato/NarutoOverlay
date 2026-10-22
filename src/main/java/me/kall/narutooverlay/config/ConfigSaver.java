package me.kall.narutooverlay.config;

import net.minecraft.Util;

public final class ConfigSaver {
    private static final long SAVE_DELAY_MS = 400L;

    private static boolean dirty;
    private static long lastChange;

    private ConfigSaver() {}

    public static void changed() {
        ConfigVersion.bump();
        dirty = true;
        lastChange = Util.getMillis();
    }

    public static void tick() {
        if (dirty && Util.getMillis() - lastChange >= SAVE_DELAY_MS) {
            flush();
        }
    }

    public static void flush() {
        if (!dirty) return;
        dirty = false;
        RenderArgs.CONFIG.save();
    }
}
