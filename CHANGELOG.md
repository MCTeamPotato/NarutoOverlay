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
- Restore collapsible sections in the Cards page through NarutoUI 1.4.0's `MuiCollapsibleCard`.
- Use a local NarutoUI jar from `deps/` during development instead of the Curse Maven dependency.
# 1.3.0
- Rebuild the Cards page from the generic `MuiCollapsibleCard`, `MuiNumberField`, `MuiChip`, and `MuiTextAction` components.
- Move the rail logo/hint to `MuiLogo` / `MuiHintBadge`.
- Move the About usage rows, path actions and folder buttons to `MuiDescriptionRow` / `MuiTextAction` / `MuiLayout`.
- Move the border-color row to `MuiColorField`.
- Fix the Reset-all translation key and let text actions, chips and theme previews participate in shimmer correctly.
- Refactor the Cards page into `CardsView` / `CardView` / `FieldView` view models so callbacks use method references instead of static helpers full of captured locals.
- Remove the redundant `ConfigUiHost` layer; pages now depend on NarutoUI's expanded `MuiScreenHost` (`onThemeChanged` included).
- Update imports for NarutoUI's new `screen` / `widget` / `theme` / `background` / `shimmer` / `overlay` / `util` package layout.
- Update the local NarutoUI dependency to `1.20.1-1.4.0`.