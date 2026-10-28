# NarutoOverlay

[Chinese](README.md) | **English**

> _An information display mod, into which I poured my deep love for Uzumaki Naruto._
>
> _— The happiest little pig in the world_

NarutoOverlay is a **client-side HUD information mod for Minecraft 1.20.1 Forge**. By default it only shows the instantaneous FPS, a translucent backplane and a text shimmer. Everything else — minimum / average / 1% low FPS, GC and memory information — can be enabled in the in-game configuration screen.

The configuration screen is built with the optional Modern UI API provided by [NarutoUI](https://www.curseforge.com/minecraft/mc-mods/narutoui). Install **NarutoUI 1.0.0+** and [ModernUI](https://www.curseforge.com/minecraft/mc-mods/modern-ui) to open it; the HUD works without them.

---

## Features

### HUD cards

| Card | Default | Fields |
|---|---|---|
| **FPS** | Enabled | Instantaneous FPS (enabled by default), minimum FPS, average FPS, 1% low |
| **GC** | Disabled | Collector name, last GC cost, time since the last GC |
| **RAM** | Disabled | Used / max memory, usage percentage (disabled by default), allocated memory |

- Every card can be enabled independently, moved, and configured field by field, including whether field labels and units are shown.
- FPS below 60 is shown in yellow, below 30 in red. RAM usage at 75% or higher is yellow, at 90% or higher is red.
- Cards can share the width of the widest card so stacked cards line up.
- Supports drop shadows, a translucent backplane, a rounded border and a custom border color.

### Themes

- **NARUTO**: warm black, flame orange, gold, chakra yellow and red accents, sampled from Kurama Chakra Mode art.
- **CLASSIC**: pure white text on a translucent gray backplane.
- Themes only change colors; the text shimmer is available only in NARUTO.

### Text shimmer

- A golden chakra highlight sweeps across HUD and configuration text in the NARUTO theme.
- Category switches: overlay text, configuration text, and controls & accents (switches, sliders, logo circle, ESC hint outline).
- Adjustable ripple speed, ripple density, color strength, and separate HUD / configuration update rates.
- The HUD uses cached recoloring instead of rebuilding `Style`/`TextColor` for every glyph on every frame.

### Configuration screen background

- Supports a single background image or a folder slideshow with cross-fades.
- On startup the bundled `bg1.jpg`, `bg2.jpg` and `logo.jpg` are extracted to `<game directory>/narutooverlay` and used as the default slideshow.
- Choose a folder, pick a single image, crop that image, and tune slideshow interval, fade duration and background dim.

---

## Quick start

- By default, only the **instantaneous FPS** card is shown in the top-left corner.
- Open chat (default `T`) and **left-click-drag** any card to move it.
- With chat open, **right-click** any card to open the configuration screen.
- You can also open it from **Mods → NarutoOverlay → Config**.
- Press `ESC` in the configuration screen to save and close.

If a card is dragged off-screen, bind a key to **“Reset Text Positions”** under **Controls → Key Binds** and press it to restore all card positions.

---

## Controls

| Action | How |
|---|---|
| Move a card | Open chat, hold left click on the card and drag |
| Open the configuration screen | Open chat and right-click a card, or use the Config button in the mod list |
| Save and close the configuration screen | `ESC` |
| Reset all card positions | Bind “Reset Text Positions” and press the key |
| Reset one card | “Reset this card” on the Cards page |
| Reset all cards | “Reset all settings” on the Cards page (cards only; appearance/background are not reset) |

---

## Configuration screen

The screen has three pages.

### Cards

- Enable or disable each card.
- X / Y coordinates and top-left / top-right / bottom-left / bottom-right presets.
- Field list: toggle each field independently and choose whether its label and unit are shown.
- Reset one card, or reset all cards.

### Appearance

- **Theme**: NARUTO / CLASSIC.
- **Display**: drop shadow, equal width, draw backplane, master text shimmer switch.
- **Shimmer tuning**: category switches, speed, density, color strength, HUD update rate, configuration update rate, cached recoloring.
- **Backplane border**: border width and custom RGBA color.
- **Performance**: FPS / GC / RAM update intervals and the FPS sample window.

### About

- Mod version, environment and configuration file path.
- Controls reference.
- Background settings: choose folder, clear folder, choose a single image, picture count, slideshow interval, fade duration and background dim.

---

## Background and slideshow

### Default behavior

On startup the three images bundled in the jar are extracted to:

```text
<game directory>/narutooverlay/
├── bg1.jpg
├── bg2.jpg
└── logo.jpg
```

Default configuration:

```toml
[ConfigScreen]
backgroundFolder = "narutooverlay"
backgroundOrder = ["bg1.jpg", "bg2.jpg", "logo.jpg"]
backgroundImage = "narutooverlay/bg1.jpg"
```

- Existing files are **never overwritten**, so you can replace them with your own images.
- Every image in the folder participates in the slideshow. Files listed in `backgroundOrder` come first in that order; the rest follow alphabetically by file name.
- Slideshow images are automatically center-cropped to fill the window. Only the single-image fallback uses the `Crop` region.
- If `backgroundFolder` is empty, the mod falls back to the single `backgroundImage`.

> If you only want the three bundled images, move the other pictures out of the `narutooverlay` folder.

### Single-image mode

- Choosing an image on the About page copies it to `<game directory>/narutooverlay-config-bg.<extension>`.
- The crop UI stores the visible region in the config file and never rewrites the original image.
- Clearing the folder makes the screen use this single image.

### Supported formats

`png`, `jpg`, `jpeg`, `bmp`, `gif`. Only the first level of the folder is scanned; subfolders are ignored.

---

## Text shimmer

The shimmer is available only in the **NARUTO** theme. It is unavailable in CLASSIC.

### Categories

| Switch | Controls |
|---|---|
| Overlay text | In-game HUD card text |
| Configuration text | Headings, labels, values and button text in the configuration screen |
| Controls & accents | Switches, sliders, logo circle and the ESC hint outline |

Turning off the master **Text shimmer** switch stops all three categories and restores their base colors.

### Parameters

| Parameter | Range | Default | Description |
|---|---|---|---|
| Ripple speed | 0.2–3.0 | 2.5 | How fast the color band drifts |
| Ripple density | 0.2–3.0 | 2.5 | Spatial frequency; higher creates denser ripples |
| Color strength | 0–100% | 100% | 0 keeps the base color; higher values approach the full ripple color |
| HUD update rate | 5–240 Hz | 30 | How often HUD colors are recomputed; lower reduces CPU/GC work |
| Config update rate | 5–120 Hz | 30 | How often configuration colors are updated |
| Cached recoloring | On/off | On | Off falls back to the old per-frame path; intended for comparison |

The HUD cache builds a recolored `FormattedCharSequence` snapshot on each update. On cache-hit frames there are no per-glyph `sin`, `Style` or `TextColor` allocations.

---

## Advanced configuration

Configuration file:

```text
<game directory>/config/narutooverlay-client.toml
```

Common keys:

| Key | Description |
|---|---|
| `Quality.theme` | `NARUTO` / `CLASSIC` |
| `Quality.textShimmer` | Master text shimmer switch |
| `Quality.hudTextShimmer` | HUD text shimmer |
| `Quality.configTextShimmer` | Configuration text shimmer |
| `Quality.controlShimmer` | Switch / slider / logo / ESC outline shimmer |
| `Quality.textShimmerSpeed` | Ripple speed |
| `Quality.textShimmerScale` | Ripple density |
| `Quality.textShimmerStrength` | Color strength |
| `Quality.textShimmerFps` | HUD update rate |
| `Quality.configShimmerFps` | Configuration update rate |
| `Quality.textShimmerCache` | HUD cached recoloring |
| `Quality.textOffsetY` | Vertical offset of HUD text inside the backplane (sub-pixel) |
| `Quality.dropShadow` | Drop shadow behind HUD text |
| `Quality.equalWidth` | Equal card width |
| `Quality.Backplane.drawBackplane` | Draw the translucent backplane |
| `Quality.Backplane.backplaneBorderWidth` | Border width in GUI pixels |
| `Quality.Backplane.backplaneBorderOverride` | Use the custom RGBA border color |
| `ConfigScreen.backgroundFolder` | Slideshow folder, relative to the game directory |
| `ConfigScreen.backgroundOrder` | Playback order; unlisted images follow alphabetically |
| `ConfigScreen.backgroundImage` | Single-image fallback when the folder is empty |
| `ConfigScreen.backgroundInterval` | Seconds per image (2–300) |
| `ConfigScreen.backgroundFade` | Cross-fade seconds (0–3) |
| `ConfigScreen.backgroundDim` | Background dim (0–0.95) |
| `ConfigScreen.Crop.x/y/width/height` | Visible crop region for the single-image fallback |

Almost everything can be changed in-game; editing the TOML is only necessary for advanced or bulk changes.

---

## Performance

- By default only the instantaneous FPS card is visible, so the overhead is small.
- Enabling many cards and fields while setting the HUD update rate to 240 Hz significantly increases font drawing and GC pressure.
- On high-refresh displays or CPU-bound systems:
  - lower **HUD update rate** to 20–30 Hz;
  - keep only **Overlay text** enabled and disable **Configuration text / Controls & accents**;
  - keep **Cached recoloring** enabled;
  - disable cards and fields you do not need;
  - increase the FPS / GC / RAM update intervals.

HUD shimmer only runs while the in-game HUD is rendered. Configuration screen shimmer only runs while the settings screen is open and is not kept alive in the background.

---

## FAQ

**Q: The configuration screen is missing or there is no Config button.**  
A: Install NarutoUI 1.0.0+ and ModernUI 3.12.0+. They are optional configuration-screen dependencies; the HUD itself does not require them.

**Q: The text shimmer is not visible.**  
A: Make sure the theme is NARUTO, the master switch and the relevant category switches are enabled, and color strength is greater than 0. The shimmer is unavailable in CLASSIC.

**Q: The background is not rotating.**  
A: Check that the folder configured in `backgroundFolder` exists and that images are directly inside it. Verify that the file names in `backgroundOrder` match the actual files. The About page shows the picture count.

**Q: Why do images I did not add appear in the slideshow?**  
A: Every image in the folder participates. `backgroundOrder` only controls ordering, not filtering. Move unwanted images out of the folder.

**Q: The logo is cropped.**  
A: Slideshow images are center-cropped to fill the window. Use single-image mode with the crop UI, or replace the logo with an image that has a more suitable aspect ratio.

**Q: A card was dragged off-screen.**  
A: Bind “Reset Text Positions” under Controls and press it, or use “Reset all settings” on the Cards page (cards only).

**Q: How do I reset everything?**  
A: Delete `config/narutooverlay-client.toml` and restart the game. The `narutooverlay` background folder is not deleted.

**Q: Does this mod need to be installed on the server?**  
A: No. It is a client-side HUD mod; the server does not need it.

---

## License and credits

- Author: **Kasualix**, **DeepSeek-V4.1-Flash-1017**
- License: [LGPL-3.0](https://github.com/MCTeamPotato/NarutoOverlay?tab=LGPL-3.0-1-ov-file)
- The configuration screen uses [ModernUI](https://www.curseforge.com/minecraft/mc-mods/modern-ui) through NarutoUI. Thanks to BloCamLimb and contributors.
- Thanks to Minecraft, Forge, and Naruto.

> _“Shimmer pierces the dark; frames rule the world.”_