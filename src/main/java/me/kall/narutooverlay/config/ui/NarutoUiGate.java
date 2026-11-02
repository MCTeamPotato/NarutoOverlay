package me.kall.narutooverlay.config.ui;

import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

public class NarutoUiGate {
    public static void integrate(FMLJavaModLoadingContext context) {
        NarutoConfigScreen.register(context);
    }
}
