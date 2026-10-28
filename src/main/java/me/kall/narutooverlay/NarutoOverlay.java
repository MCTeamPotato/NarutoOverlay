package me.kall.narutooverlay;

import me.kall.narutooverlay.card.CardRegistry;
import me.kall.narutooverlay.config.ConfigVersion;
import me.kall.narutooverlay.config.RenderArgs;
import me.kall.narutooverlay.config.ui.NarutoUiCompat;
import me.kall.narutooverlay.config.ui.background.BundledBackgrounds;
import me.kall.narutooverlay.event.TextRelocation;
import me.kall.narutooverlay.event.api.TaskRegistryEvent;
import me.kall.narutooverlay.event.fps.FpsTracker;
import me.kall.narutooverlay.event.gc.GCTracker;
import me.kall.narutooverlay.event.ram.RAMTracker;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.ModLoader;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.loading.FMLLoader;
import org.jetbrains.annotations.NotNull;

@Mod(NarutoOverlay.MOD_ID)
public final class NarutoOverlay {
    public static final String MOD_ID = "narutooverlay";

    public static final String ENV;

    static {
        boolean isNeoEnv;

        try {
            Class.forName("net.minecraftforge.fml.ModLoader");
            isNeoEnv = false;
        } catch (ClassNotFoundException exception) {
            isNeoEnv = true;
        }

        ENV = isNeoEnv ? "NeoForge" : "Forge";
    }

    public NarutoOverlay(@NotNull FMLJavaModLoadingContext context) {
        if (FMLLoader.getDist().isClient()) {
            context.registerConfig(ModConfig.Type.CLIENT, RenderArgs.CONFIG);

            IEventBus modBus = context.getModEventBus();

            modBus.addListener(ConfigVersion::onLoading);
            modBus.addListener(ConfigVersion::onReloading);

            modBus.addListener(FpsTracker::listen);
            modBus.addListener(RAMTracker::listen);
            modBus.addListener(RenderArgs.GC::listen);

            if (ModList.get().isLoaded("narutoui")) {
                NarutoUiCompat.integrate(context);
            }
        }
    }

    @Mod.EventBusSubscriber(modid = MOD_ID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD)
    public static class ModEventListeners {
        @SubscribeEvent(priority = EventPriority.LOWEST)
        public static void setup(@NotNull FMLClientSetupEvent event) {
            event.enqueueWork(() -> {
                BundledBackgrounds.install();

                if (ModList.get().isLoaded("modernui") && ModList.get().isLoaded("narutoui")) {
                    NarutoUiCompat.migrateBundledDefaults();
                }

                IEventBus bus = MinecraftForge.EVENT_BUS;

                bus.addListener(CardRegistry::render);

                bus.addListener(RenderArgs.FPS::update);
                bus.addListener(EventPriority.LOWEST, FpsTracker::tick);

                GCTracker.setup();

                bus.addListener(RenderArgs.RAM::update);

                bus.addListener(RenderArgs::tick);

                bus.addListener(TextRelocation::click);
                bus.addListener(TextRelocation::drag);
                bus.addListener(TextRelocation::release);

                bus.addListener(TaskRegistryEvent::update);

                ModLoader.get().postEvent(TaskRegistryEvent.INSTANCE);
            });
        }

        @SubscribeEvent
        public static void registerKey(@NotNull RegisterKeyMappingsEvent event) {
            event.register(RenderArgs.RESET_KEY.get());
        }
    }
}
