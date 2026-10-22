# m3fx

Material Design 3 for JavaFX: dynamic color, a complete restyle of the standard controls, and the
Material components JavaFX doesn't have.

```java
MaterialTheme theme = new MaterialTheme(Color.web("#6750A4"));
theme.setMode(ThemeMode.SYSTEM);        // follows the OS dark mode, live
theme.setDensity(Density.COMPACT);      // desktop-friendly sizes
theme.applyGlobally();                  // every window: stages, dialogs, menus, tooltips
```

Requires Java 21+ and JavaFX 22+ (built and tested with JDK 25 / JavaFX 25). No dependencies;
Roboto and the Material Icons fonts are bundled.

## Run the demo

```
./gradlew :m3fx-demo:run           # the showcase app, with a live theme editor
./gradlew :m3fx-demo:screenshots   # renders every page, light and dark, to m3fx-demo/build/screenshots
./gradlew :m3fx-core:test
```

## What's in it

### 1. Dynamic color (`dev.m3fx.color`)
A Java port of Material's color science: CAM16, HCT, tonal palettes.

- `ColorScheme`: all 49 Material color roles from one seed color, light or dark.
- 7 scheme variants: tonal spot (default), neutral, vibrant, expressive, monochrome, fidelity, content.
- Contrast levels (standard / medium / high).
- Overridable secondary, tertiary, neutral and error key colors.
- Custom (extended) colors such as `success` or `warning`, harmonized towards the primary.
- `SeedExtractor`: picks seed colors from an image.

### 2. Theme (`dev.m3fx`)
`MaterialTheme` turns a scheme into CSS looked-up colors and applies them with the stylesheet.
Every property can change at runtime: seed, mode (light / dark / system), variant, contrast,
density, font family and custom colors.

In your own CSS, use the role names, plus translucent variants for overlays:

```css
.my-panel {
    -fx-background-color: -md-sys-color-surface-container;
    -fx-border-color: -md-sys-color-outline-variant;
}
.my-panel:hover { -fx-background-color: -md-sys-color-surface-container, -md-sys-color-on-surface-a08; }
.status-ok { -fx-text-fill: -md-custom-success; }   /* theme.getCustomColors().put("success", ...) */
```

You also get the type scale (`Typography.TITLE_LARGE.apply(label)` or the `title-large` style
class), elevation classes (`md-elevation-1` … `5`), surface classes, and motion tokens (`Motion`).

**Density** is a first-class setting. Material's default sizes target touch screens, and each
density step (-1, -2, -3) removes 4px from buttons, fields, list items, table rows, chips, tabs,
menus and bars. For data-heavy desktop UIs, `COMPACT` or `DENSE` is usually what you want.

### 3. Standard JavaFX controls, restyled
No code changes needed. `Button`, `ToggleButton`, `CheckBox`, `RadioButton`, `Hyperlink`,
`MenuButton`, `TextField`, `TextArea`, `ComboBox`, `ChoiceBox`, `DatePicker`, `Spinner`, `Slider`,
`ProgressBar`, `ProgressIndicator`, `ListView`, `TableView`, `TreeView`, `TreeTableView`, `TabPane`,
`TitledPane`, `Accordion`, `SplitPane`, `ScrollPane`, `ToolBar`, `MenuBar`, `ContextMenu`,
`Tooltip`, `Pagination`, `Separator` and `Dialog`/`Alert` all follow the theme.

Buttons, toggles, checkboxes and radios get skins with Material state layers and ripples. Progress
indicators get the MD3 circular and linear skins (track with gaps, stop indicator). Sliders get the
active track and a value label.

Style classes add the Material variants: `.button.tonal`, `.elevated`, `.outlined`, `.text`,
`.error-button`; `.text-field.filled`; `.table-view.striped` / `.grid`; `.tab-pane.secondary`;
`.tooltip.rich`.

### 4. Components (`dev.m3fx.controls`)

| Area | Components |
|---|---|
| Actions | `Buttons` (factories), `IconButton` (4 variants, toggle), `Fab` (3 sizes, 4 colors, extended), `SegmentedButton` (single / multi) |
| Selection & display | `Chip` (assist, filter, input, suggestion, elevated), `Switch` (with icons), `Badge`, `Avatar`, `Card` (elevated, filled, outlined, clickable), `Divider`, `Icon` (2,000+ Material icons, outlined / filled) |
| Text input | `MaterialTextField` (filled / outlined, floating label, leading / trailing icons, prefix / suffix, counter, validator, password toggle, clearable), `SearchBar` (with suggestions) |
| Navigation | `NavigationBar`, `NavigationRail`, `NavigationDrawer` (standard + modal, headlines, dividers, badges), sharing one `NavigationItem` model; `TopAppBar` (small, center-aligned, medium, large, scroll-aware), `BottomAppBar` |
| Layout | `Scaffold` (top bar, navigation, content, bottom bar, FAB, overlay host), `ListItem` (1–3 lines, leading / trailing, clickable, `ListView` cell factory), `Carousel` (multi-browse) |
| Overlays | `MaterialDialog` (basic, icon, custom content, full-screen; `confirm` / `alert` helpers returning `CompletableFuture`), `Snackbar` (queued, action, close), `BottomSheet`, `SideSheet`, `Tooltips` (plain / rich) |
| Pickers | `TimePicker` (dial, 12h / 24h, keyboard entry, `showDialog`), `DatePickerDialog` |
| Infrastructure | `StateLayer` (hover / focus / press overlay + ripple for your own controls), `Overlay`, `Motion` |

Overlays (dialogs, sheets, snackbars, modal drawer) open inside the window, above a scrim. They
need a `Scaffold` ancestor, or a `StackPane` as the scene root.

```java
MaterialDialog.confirm(node, "Delete mission?", "This can't be undone.", "Delete", "Cancel")
        .thenAccept(confirmed -> { if (confirmed) delete(); });

Snackbar.make(node, "Waypoint deleted").action("Undo", this::undo).show();

MaterialTextField callsign = new MaterialTextField("Callsign");
callsign.setMaxLength(8);
callsign.setValidator(text -> text.length() < 3 ? "At least three letters" : null);
```

## Troubleshooting

**Black flickering during animations on Linux** (switches, progress indicators, ripples).
Seen with NVIDIA drivers under XWayland (JavaFX has no native Wayland support), e.g. on COSMIC.
It comes from vsynced buffer swaps, not from m3fx: the software renderer (`-Dprism.order=sw`)
doesn't show it, and disabling dirty-region repaints (`-Dprism.dirtyopts=false`) doesn't help.
Turning vsync off fixes it:

```
java -Dprism.vsync=false ...          # and/or the environment variable __GL_SYNC_TO_VBLANK=0
```

The demo does this by default on Linux (`./gradlew :m3fx-demo:run -Pvsync=true` to compare).
Other diagnostics: `-Pprism=sw` (software renderer), `-Pdirtyopts=false`.

## Project layout

- `m3fx-core`: the library (JPMS module `dev.m3fx`)
- `m3fx-demo`: the showcase app and the screenshot renderer

## Licenses

m3fx bundles Roboto (Apache 2.0 / OFL) and the Material Icons fonts (Apache 2.0) from Google.
