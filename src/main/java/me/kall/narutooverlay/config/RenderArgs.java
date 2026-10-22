package me.kall.narutooverlay.config;

import me.kall.narutooverlay.card.CardRegistry;
import me.kall.narutooverlay.event.fps.FpsCard;
import me.kall.narutooverlay.event.gc.GcCard;
import me.kall.narutooverlay.event.ram.RamCard;
import me.kall.narutooverlay.theme.Palette;
import me.kall.narutooverlay.theme.Theme;
import net.minecraft.client.KeyMapping;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.common.util.Lazy;
import net.minecraftforge.event.TickEvent;
import org.jetbrains.annotations.NotNull;
import org.lwjgl.glfw.GLFW;

public final class RenderArgs {
    private static final int DEFAULT_FPS_COOLDOWN = 20;
    private static final int DEFAULT_GC_COOLDOWN = 20;
    private static final int DEFAULT_RAM_COOLDOWN = 20;

    private static final int DEFAULT_FPS_WINDOW = 1000;
    private static final int MAX_FPS_WINDOW = 1_000_000;

    public static final String DEFAULT_BACKGROUND_IMAGE = "narutooverlay-config-bg.png";

    public static final ForgeConfigSpec CONFIG;

    public static final FpsCard FPS;
    public static final GcCard GC;
    public static final RamCard RAM;

    public static final ForgeConfigSpec.IntValue FPS_COOLDOWN;
    public static final ForgeConfigSpec.IntValue GC_COOLDOWN;
    public static final ForgeConfigSpec.IntValue RAM_COOLDOWN;

    public static final ForgeConfigSpec.IntValue FPS_WINDOW;

    public static final ForgeConfigSpec.EnumValue<Theme> THEME;
    public static final ForgeConfigSpec.BooleanValue DROP_SHADOW;
    public static final ForgeConfigSpec.BooleanValue EQUAL_WIDTH;
    public static final ForgeConfigSpec.BooleanValue DRAW_BACKPLANE;
    public static final ForgeConfigSpec.BooleanValue BACKPLANE_BORDER_OVERRIDE;
    public static final ForgeConfigSpec.IntValue BACKPLANE_BORDER_RED;
    public static final ForgeConfigSpec.IntValue BACKPLANE_BORDER_GREEN;
    public static final ForgeConfigSpec.IntValue BACKPLANE_BORDER_BLUE;
    public static final ForgeConfigSpec.IntValue BACKPLANE_BORDER_ALPHA;
    public static final ForgeConfigSpec.DoubleValue BACKPLANE_BORDER_WIDTH;

    public static final ForgeConfigSpec.ConfigValue<String> BACKGROUND_IMAGE;
    public static final ForgeConfigSpec.DoubleValue BACKGROUND_DIM;
    public static final ForgeConfigSpec.DoubleValue BACKGROUND_CROP_X;
    public static final ForgeConfigSpec.DoubleValue BACKGROUND_CROP_Y;
    public static final ForgeConfigSpec.DoubleValue BACKGROUND_CROP_WIDTH;
    public static final ForgeConfigSpec.DoubleValue BACKGROUND_CROP_HEIGHT;

    public static final Lazy<KeyMapping> RESET_KEY = Lazy.of(() -> new KeyMapping("key.narutooverlay.reset_text_args", GLFW.GLFW_KEY_UNKNOWN, "key.categories.narutooverlay"));

    static {
        ForgeConfigSpec.Builder builder = new ForgeConfigSpec.Builder();

        builder.push("Texts");
        FPS = new FpsCard(builder);
        GC = new GcCard(builder);
        RAM = new RamCard(builder);
        builder.pop();

        builder.push("UpdateInterval");

        FPS_COOLDOWN = builder
                .comment("Number of client ticks between FPS information updates.")
                .defineInRange("fps", DEFAULT_FPS_COOLDOWN, 1, Integer.MAX_VALUE);

        GC_COOLDOWN = builder
                .comment("Number of client ticks between garbage collector information updates.")
                .defineInRange("gc", DEFAULT_GC_COOLDOWN, 1, Integer.MAX_VALUE);

        RAM_COOLDOWN = builder
                .comment("Number of client ticks between RAM information updates.")
                .defineInRange("ram", DEFAULT_RAM_COOLDOWN, 1, Integer.MAX_VALUE);

        builder.pop();

        builder.push("Statistics");
        builder.push("FPS");
        FPS_WINDOW = builder
                .comment(
                        "Duration in milliseconds of the recent frame interval window used to calculate FPS statistics.",
                        "A larger window smooths average and low-percentile FPS values, but makes them slower to reflect performance changes.",
                        "A smaller window responds faster to changes, but produces less stable statistics.",
                        "The instantaneous FPS value is always based on the latest frame interval."
                )
                .defineInRange("window", DEFAULT_FPS_WINDOW, 10, MAX_FPS_WINDOW);
        builder.pop();
        builder.pop();

        builder.push("Quality");
        THEME = builder
                .comment(
                        "Color theme of the overlay text and backplane.",
                        "NARUTO: orange, charcoal black and yellow, inspired by the Valkyrie VK99 Gaming Naruto keyboard.",
                        "CLASSIC: white text on a translucent gray backplane."
                )
                .defineEnum("theme", Theme.NARUTO);
        DROP_SHADOW = builder
                .comment("Whether to draw a shadow behind the text.")
                .define("dropShadow", false);
        EQUAL_WIDTH = builder
                .comment("Whether all visible overlay cards use the width of the widest one, so stacked cards line up.")
                .define("equalWidth", true);

        builder.push("Backplane");
        DRAW_BACKPLANE = builder
                .comment("Whether to draw a translucent background behind the text.")
                .define("drawBackplane", true);
        BACKPLANE_BORDER_WIDTH = builder
                .comment("Rounded backplane border width in GUI pixels. Set to 0 to disable the border.")
                .defineInRange("backplaneBorderWidth", 0.6D, 0D, 4D);
        BACKPLANE_BORDER_OVERRIDE = builder
                .comment("Use the border color channels below instead of the border color of the selected theme.")
                .define("backplaneBorderOverride", false);
        BACKPLANE_BORDER_RED = builder
                .comment("Red channel of the backplane border color (0-255). Only used when backplaneBorderOverride is true.")
                .defineInRange("backplaneBorderRed", 0, 0, 255);
        BACKPLANE_BORDER_GREEN = builder
                .comment("Green channel of the backplane border color (0-255). Only used when backplaneBorderOverride is true.")
                .defineInRange("backplaneBorderGreen", 0, 0, 255);
        BACKPLANE_BORDER_BLUE = builder
                .comment("Blue channel of the backplane border color (0-255). Only used when backplaneBorderOverride is true.")
                .defineInRange("backplaneBorderBlue", 255, 0, 255);
        BACKPLANE_BORDER_ALPHA = builder
                .comment("Alpha channel of the backplane border color (0-255). Only used when backplaneBorderOverride is true.")
                .defineInRange("backplaneBorderAlpha", 255, 0, 255);
        builder.pop();

        builder.pop();

        builder.push("ConfigScreen");
        BACKGROUND_IMAGE = builder
                .comment(
                        "File name of the custom configuration screen background, relative to the game directory.",
                        "The configuration screen writes the image there when the player picks one, leave empty for no background."
                )
                .define("backgroundImage", DEFAULT_BACKGROUND_IMAGE);
        BACKGROUND_DIM = builder
                .comment("How strongly the configuration screen background image is darkened, 0 to 0.95.")
                .defineInRange("backgroundDim", 0.55D, 0D, 0.95D);
        builder.comment(
                        "Visible region of the background image, relative to its size (0 to 1).",
                        "The screen uses these instead of rewriting the file, so the original picture is kept untouched."
                )
                .push("Crop");
        BACKGROUND_CROP_X = builder.defineInRange("x", 0.0D, 0D, 1D);
        BACKGROUND_CROP_Y = builder.defineInRange("y", 0.0D, 0D, 1D);
        BACKGROUND_CROP_WIDTH = builder.defineInRange("width", 1.0D, 0D, 1D);
        BACKGROUND_CROP_HEIGHT = builder.defineInRange("height", 1.0D, 0D, 1D);
        builder.pop();
        builder.pop();

        CONFIG = builder.build();
    }

    public static void tick(TickEvent.@NotNull ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;

        boolean reset = false;
        while (RESET_KEY.get().consumeClick()) reset = true;

        if (reset) {
            CardRegistry.resetPositions();
            ConfigSaver.changed();
        }

        ConfigSaver.tick();
    }

    public static @NotNull Palette palette() {
        return THEME.get().palette();
    }

    public static int borderColor() {
        if (BACKPLANE_BORDER_OVERRIDE.get()) {
            return BACKPLANE_BORDER_ALPHA.get() << 24
                    | BACKPLANE_BORDER_RED.get() << 16
                    | BACKPLANE_BORDER_GREEN.get() << 8
                    | BACKPLANE_BORDER_BLUE.get();
        }

        return palette().border();
    }
}
