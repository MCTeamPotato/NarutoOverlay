# 1.0.0
- Initial release
# 1.0.1
- Support selecting, merging, dragging, and splitting HUD card groups with a shared outline. 
- Smooth shimmer speed, strength, and density changes.
- Add an About-page action to open the config folder.
# 1.1.0
- Rebuild the configuration UI on NarutoUI's Modern UI API and declare NarutoUI as an optional dependency. 
- Keep HUD shimmer independent of the optional UI libraries
- Refine grouped-card rendering and dragging.
# 1.2.0
- Migrate ConfigUI implementation to using NarutoUI API
# 1.3.0
- Add a reset action for each card and mark configuration changes for saving when a card is reset. 
- Update the bundled NarutoUI artifacts and add English and Chinese labels.
# 1.5.4
- Give every non-Naruto theme a full light-to-deep shimmer ramp built from its accent and deep separator colors, instead of only white-to-accent.
- HUD shimmer now samples the same nine-stop ramp as the configuration screen, so Jade/Azure/Sakura/Graphite/Custom get visible light, core and deep tones like Naruto's chakra ramp.

# 1.5.3
- Rebuild the whole configuration shell when the theme changes, so rail logo, navigation highlight, section headings and page widgets all switch to the new palette together.
- Use NarutoUI 1.7.2's context-aware theme provider and `reloadTheme()`.

# 1.5.2
- Make the theme-to-white shimmer visible for Jade, Azure, Sakura, Graphite and Custom instead of only Naruto.
- HUD shimmer now sweeps each theme's accent through white; the configuration screen uses the same accent-to-white OKLab band.

# 1.5.1
- Keep non-Naruto themes free of the Naruto gold/white/red shimmer: JADE, AZURE, SAKURA, GRAPHITE and CUSTOM now use an accent-based OKLab band.
- Make theme-picker preview cards static so they show the candidate palette instead of borrowing the selected theme's shimmer.

# 1.5.0
- Add Jade, Azure, Sakura and Graphite themes alongside Naruto and Classic.
- Add a Custom theme: pick an accent and a backplane color, and derive readable label, unit, separator, text and border colors from it.
- Add a themed preview card for every theme, including the current custom palette.
- Use the local NarutoUI 1.7.1 project jar instead of CurseMaven.

# 1.4.0
- Add a fade transition when switching configuration pages, powered by NarutoUI's page transition API.
- Update the NarutoUI artifacts to 1.7.0. No longer shadows it.