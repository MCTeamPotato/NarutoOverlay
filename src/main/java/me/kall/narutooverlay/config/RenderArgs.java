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

import java.util.ArrayList;
import java.util.List;

public final class RenderArgs {
    private static final int DEFAULT_FPS_COOLDOWN = 20;
    private static final int DEFAULT_GC_COOLDOWN = 20;
    private static final int DEFAULT_RAM_COOLDOWN = 20;

    private static final int DEFAULT_FPS_WINDOW = 1000;
    private static final int MAX_FPS_WINDOW = 1_000_000;

    public static final String DEFAULT_BACKGROUND_FOLDER = "narutooverlay";
    public static final List<String> DEFAULT_BACKGROUND_ORDER = List.of("bg1.jpg", "bg2.jpg", "logo.jpg");
    public static final String DEFAULT_BACKGROUND_IMAGE = DEFAULT_BACKGROUND_FOLDER + "/bg1.jpg";

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
    public static final ForgeConfigSpec.BooleanValue TEXT_SHIMMER;
    public static final ForgeConfigSpec.BooleanValue HUD_TEXT_SHIMMER;
    public static final ForgeConfigSpec.BooleanValue CONFIG_TEXT_SHIMMER;
    public static final ForgeConfigSpec.BooleanValue CONTROL_SHIMMER;
    public static final ForgeConfigSpec.DoubleValue TEXT_SHIMMER_SPEED;
    public static final ForgeConfigSpec.DoubleValue TEXT_SHIMMER_SCALE;
    public static final ForgeConfigSpec.DoubleValue TEXT_SHIMMER_STRENGTH;
    public static final ForgeConfigSpec.IntValue TEXT_SHIMMER_FPS;
    public static final ForgeConfigSpec.IntValue CONFIG_SHIMMER_FPS;
    public static final ForgeConfigSpec.BooleanValue TEXT_SHIMMER_CACHE;
    public static final ForgeConfigSpec.DoubleValue TEXT_OFFSET_Y;
    public static final ForgeConfigSpec.BooleanValue BACKPLANE_BORDER_OVERRIDE;
    public static final ForgeConfigSpec.IntValue BACKPLANE_BORDER_RED;
    public static final ForgeConfigSpec.IntValue BACKPLANE_BORDER_GREEN;
    public static final ForgeConfigSpec.IntValue BACKPLANE_BORDER_BLUE;
    public static final ForgeConfigSpec.IntValue BACKPLANE_BORDER_ALPHA;
    public static final ForgeConfigSpec.DoubleValue BACKPLANE_BORDER_WIDTH;

    public static final ForgeConfigSpec.ConfigValue<String> BACKGROUND_IMAGE;
    public static final ForgeConfigSpec.ConfigValue<String> BACKGROUND_FOLDER;
    public static final ForgeConfigSpec.ConfigValue<List<? extends String>> BACKGROUND_ORDER;
    public static final ForgeConfigSpec.DoubleValue BACKGROUND_INTERVAL;
    public static final ForgeConfigSpec.DoubleValue BACKGROUND_FADE;
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
                        "NARUTO: warm black, flame orange, gold and chakra-glow yellow with red accents, sampled from Kurama Chakra Mode art.",
                        "CLASSIC: white text on a translucent gray backplane."
                )
                .defineEnum("theme", Theme.NARUTO);
        DROP_SHADOW = builder
                .comment("Whether to draw a shadow behind the text.")
                .define("dropShadow", false);
        EQUAL_WIDTH = builder
                .comment("Whether all visible overlay cards use the width of the widest one, so stacked cards line up.")
                .define("equalWidth", true);
        TEXT_SHIMMER = builder
                .comment(
                        "Sweep a golden chakra highlight across the overlay cards and the configuration screen text.",
                        "Only themes with an accent color (NARUTO) are affected; CLASSIC stays static."
                )
                .define("textShimmer", true);
        HUD_TEXT_SHIMMER = builder
                .comment("Shimmer the in-game overlay card text.")
                .define("hudTextShimmer", true);
        CONFIG_TEXT_SHIMMER = builder
                .comment("Shimmer labels, values, headings and button text in the configuration screen.")
                .define("configTextShimmer", true);
        CONTROL_SHIMMER = builder
                .comment("Shimmer switches, sliders, the logo circle and the ESC hint outline.")
                .define("controlShimmer", true);
        TEXT_SHIMMER_SPEED = builder
                .comment("Speed multiplier of the text shimmer sweep.")
                .defineInRange("textShimmerSpeed", 2.5D, 0.2D, 3.0D);
        TEXT_SHIMMER_SCALE = builder
                .comment("Spatial frequency multiplier of the shimmer ripple; higher values create denser ripples.")
                .defineInRange("textShimmerScale", 2.5D, 0.2D, 3.0D);
        TEXT_SHIMMER_STRENGTH = builder
                .comment("Colour strength of the shimmer: 0 keeps the base colours, 1 uses the full ripple colours.")
                .defineInRange("textShimmerStrength", 1.0D, 0.0D, 1.0D);
        TEXT_SHIMMER_FPS = builder
                .comment(
                        "How many times per second the HUD shimmer colours are recomputed.",
                        "Lower values reduce CPU/GC work; 30 is visually smooth, 5-240 supported."
                )
                .defineInRange("textShimmerFps", 30, 5, 240);
        CONFIG_SHIMMER_FPS = builder
                .comment(
                        "How many times per second the configuration screen shimmer is updated.",
                        "Lower values reduce CPU work while the settings screen is open."
                )
                .defineInRange("configShimmerFps", 30, 5, 120);
        TEXT_SHIMMER_CACHE = builder
                .comment(
                        "Cache the HUD's recoloured glyph sequence between updates instead of rebuilding it every frame.",
                        "Disable to compare against the old per-frame path; caching is recommended."
                )
                .define("textShimmerCache", true);
        TEXT_OFFSET_Y = builder
                .comment(
                        "Vertical offset of the overlay card text inside its backplane, in GUI pixels.",
                        "Increase it when the text sits too high; 0.5 is half a pixel."
                )
                .defineInRange("textOffsetY", 0.5D, -4.0D, 4.0D);

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
                        "File name of the fallback configuration screen background, relative to the game directory.",
                        "Used when backgroundFolder is empty; the configuration screen writes picked images there.",
                        "Leave empty for no fallback background."
                )
                .define("backgroundImage", DEFAULT_BACKGROUND_IMAGE);
        BACKGROUND_FOLDER = builder
                .comment(
                        "Folder that contains the configuration screen background pictures.",
                        "The mod extracts its bundled pictures into this folder on startup; all pictures inside are shown as a slideshow.",
                        "Relative paths are resolved against the game directory.",
                        "Leave this empty to fall back to the single backgroundImage setting."
                )
                .define("backgroundFolder", DEFAULT_BACKGROUND_FOLDER);
        BACKGROUND_ORDER = builder
                .comment(
                        "Playback order of the background pictures, as file names inside backgroundFolder.",
                        "Listed pictures are shown first in this order; pictures that are not listed follow in alphabetical order.",
                        "Leave the list empty to sort the whole folder by file name."
                )
                .defineListAllowEmpty("backgroundOrder", new ArrayList<>(DEFAULT_BACKGROUND_ORDER),
                        element -> element instanceof String);
        BACKGROUND_INTERVAL = builder
                .comment("Seconds each background picture stays on screen.")
                .defineInRange("backgroundInterval", 12.0D, 2.0D, 300.0D);
        BACKGROUND_FADE = builder
                .comment("Seconds of the cross-fade between two background pictures. Set to 0 to switch instantly.")
                .defineInRange("backgroundFade", 0.6D, 0.0D, 3.0D);
        BACKGROUND_DIM = builder
                .comment("How strongly the configuration screen background image is darkened, 0 to 0.95.")
                .defineInRange("backgroundDim", 0.55D, 0D, 0.95D);
        builder.comment(
                        "Visible region of the single fallback background image, relative to its size (0 to 1).",
                        "Slideshow pictures ignore these values and are framed automatically.",
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
            return BACKPLANE_BORDER_ALPHA.get() << 24 | BACKPLANE_BORDER_RED.get() << 16 | BACKPLANE_BORDER_GREEN.get() << 8 | BACKPLANE_BORDER_BLUE.get();
        }

        return palette().border();
    }
}
