package me.kall.narutooverlay.config;

import net.minecraftforge.fml.event.config.ModConfigEvent;

import java.util.concurrent.atomic.AtomicInteger;

public final class ConfigVersion {
    private static final AtomicInteger VERSION = new AtomicInteger();

    private ConfigVersion() {}

    public static int get() {
        return VERSION.get();
    }

    public static void bump() {
        VERSION.incrementAndGet();
    }

    public static void onLoading(ModConfigEvent.Loading event) {
        bump();
    }

    public static void onReloading(ModConfigEvent.Reloading event) {
        bump();
    }
}
