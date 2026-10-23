# Material3FX

Material Design 3 for JavaFX: dynamic color, a complete restyle of the standard controls, and the
Material components JavaFX doesn't have.

```java
MaterialTheme theme = new MaterialTheme(Color.web("#6750A4"));
theme.setMode(ThemeMode.SYSTEM);        // follows the OS dark mode, live
theme.setDensity(Density.COMPACT);      // desktop-friendly sizes
theme.applyGlobally();                  // every window: stages, dialogs, menus, tooltips
```

Requires **Java 21+ and JavaFX 21+** (both LTS). No dependencies; Roboto and the Material Icons
fonts are bundled. Following the system dark mode (`ThemeMode.SYSTEM`) and accent color works on
Linux through the XDG desktop portal (COSMIC, GNOME, KDE), read over the session bus by a small
built-in D-Bus client (pure Java, no external process or native code) that also follows changes
live, and elsewhere through `Platform.getPreferences()`, which needs JavaFX 22+. JavaFX's own
detection is not used on Linux: it derives dark mode from the GTK theme, which is wrong on some
desktops (COSMIC reports a light theme while the desktop is dark).

The whole test suite passes on every combination the JavaFX releases themselves support:

| | JavaFX 21 | 22 | 23 | 24 | 25 | 26 | 27 |
|---|---|---|---|---|---|---|---|
| **JDK 21** | ✅ | ✅ | ✅ | needs JDK 22 | needs JDK 23 | needs JDK 24 | needs JDK 25 |
| **JDK 25** | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ |

To test a combination: `./gradlew :material3fx:test -Pjdk=21 -Pjavafx=22.0.2`.

## Installation

```kotlin
// build.gradle.kts
dependencies {
    implementation("io.github.squoirelle:material3fx:0.1.0")
}
```

```xml
<dependency>
    <groupId>io.github.squoirelle</groupId>
    <artifactId>material3fx</artifactId>
    <version>0.1.0</version>
</dependency>
```

JavaFX is not pulled in: the application declares its own (`javafx.controls`, any version from
21), with the platform artifacts it ships, as usual with the
[JavaFX Gradle plugin](https://github.com/openjfx/javafx-gradle-plugin) or the
[JavaFX Maven plugin](https://github.com/openjfx/javafx-maven-plugin). With JPMS:

```java
module my.app {
    requires io.github.squoirelle.material3fx;
}
```

## Run the demo and the sample app

```
./gradlew :material3fx-demo:run           # the showcase app, with a live theme editor
./gradlew :material3fx-demo:screenshots   # renders every page, light and dark, to material3fx-demo/build/screenshots
./gradlew :material3fx-sample:run         # Stockroom, a small inventory app written in FXML
./gradlew :material3fx:test
```

## Using FXML

Every control works in FXML (the helpers `Buttons`, `Tooltips`, `Snackbar.make` and the dialog
launchers are for code): no-arg or `@NamedArg` constructors, and a default property for
nested children (`content` for cards and scaffolds, `items` for navigation and carousels,
`actions` for app bars and banners, `field` for form rows, `buttons` for segmented buttons).
The sample app, `material3fx-sample`, builds all its screens this way:

```xml
<AdaptiveScaffold navigationTitle="Stockroom">
    <navigationItems>
        <NavigationItem text="Inventory" icon="inventory_2"/>
        <NavigationItem kind="DIVIDER"/>
        <NavigationItem text="Settings" icon="settings"/>
    </navigationItems>
    <StackPane fx:id="pageHost"/>
</AdaptiveScaffold>

<FormPane fx:id="form">
    <FormRow label="Name" required="true">
        <MaterialTextField fx:id="name" maxLength="40"/>
    </FormRow>
    <ValidationSummary form="$form"/>
</FormPane>
```

The sample shows the usual pieces of a desktop application: a menu bar with accelerators, pages
loaded from FXML with a controller factory, background loading with `Task` and progress
indicators, a filterable table with a context menu, a validated form, dialogs and snackbars,
and settings bound to the theme.

## What's in it

### 1. Dynamic color (`io.github.squoirelle.material3fx.color`)
A Java port of Material's color science: CAM16, HCT, tonal palettes.

- `ColorScheme`: all 49 Material color roles from one seed color, light or dark.
- 7 scheme variants: tonal spot (default), neutral, vibrant, expressive, monochrome, fidelity, content.
- Contrast levels (standard / medium / high).
- Overridable secondary, tertiary, neutral and error key colors.
- Custom (extended) colors such as `success` or `warning`, harmonized towards the primary.
- `SeedExtractor`: picks seed colors from an image.

### 2. Theme (`io.github.squoirelle.material3fx`)
`MaterialTheme` turns a scheme into CSS looked-up colors and applies them with the stylesheet.
Every property can change at runtime: seed, mode (light / dark / system), variant, contrast,
density, font family and custom colors. `setUseSystemAccent(true)` takes the seed from the OS
accent color, following it live (falls back to the seed when the OS has none), and
`setHarmonizeCustomColors(false)` keeps custom colors exactly as given instead of shifting their
hue towards the primary.

In your own CSS, use the role names, plus translucent variants for overlays:

```css
.my-panel {
    -fx-background-color: -md-sys-color-surface-container;
    -fx-border-color: -md-sys-color-outline-variant;
}
.my-panel:hover { -fx-background-color: -md-sys-color-surface-container, -md-sys-color-on-surface-a08; }
.status-ok { -fx-text-fill: -md-custom-success; }   /* theme.getCustomColors().put("success", ...) */
```

The theme also generates semantic **success** and **warning** roles (`-md-sys-color-success`,
`-md-sys-color-warning-container`...), harmonized with the seed, used by `Banner` and `StatusLight`.

### Adaptive layout

`AdaptiveScaffold` follows Material's window size classes (compact &lt; 600, medium &lt; 840, expanded
&lt; 1200, large &lt; 1600, extra-large): it shows the destinations in a navigation bar, rail or drawer
(or a modal drawer when there are too many for a bar or rail), and list-detail content in one or
two panes. All variants share one selection.

```java
AdaptiveScaffold scaffold = new AdaptiveScaffold();
scaffold.getNavigationItems().addAll(new NavigationItem("Inbox", "inbox"), new NavigationItem("Map", "map"));
topBar.setNavigationIcon(scaffold.getNavigationButton());   // shown only when a modal drawer is used
scaffold.setListPane(messages);
scaffold.setDetailPane(message);
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

### 4. Components (`io.github.squoirelle.material3fx.controls`)

Every component follows the JavaFX control model: a `Control` (or the JavaFX control it
specializes) with its look and behavior in a `Skin` (`io.github.squoirelle.material3fx.skins`), created by
`createDefaultSkin()` and replaceable with `setSkin` or `-fx-skin` in CSS. Where JavaFX has an
equivalent, the component *is* that control:

| Component | Model |
|---|---|
| `Switch` | `ToggleButton`: `selected`, `onAction`, toggle groups |
| `Chip` (assist, filter, input, suggestion, elevated) | `ToggleButton`: filter chips toggle, the others fire like buttons; leading icon = `graphic` |
| `MaterialTextField` (filled / outlined, floating label, icons, prefix / suffix, counter, validator, password) | `TextField`: `TextFormatter`, `onAction`, prompt text, selection, context menu all work |
| `SearchBar` (with suggestions) | `TextField`: submitting fires `onAction` |
| `MaterialDialog<R>` (basic, icon, custom content, full screen; `confirm` / `alert`) | like `Dialog<R>`: `getButtonTypes()`, `ButtonData`, `resultConverter`; `show(owner)` returns a `CompletableFuture<R>` |
| `NavigationBar`, `NavigationRail`, `NavigationDrawer` (standard + modal, headlines, dividers, badges) | like `TabPane`: `SingleSelectionModel<NavigationItem>` |
| `SegmentedButton` (single / multi) | a `Control` holding `ToggleButton`s and a `ToggleGroup` |
| `TimePicker` (dial, 12h / 24h, `showDialog`) | like `DatePicker`: `ObjectProperty<LocalTime> value` |
| `NumberField` (min / max / step, units, locale-aware) | a `MaterialTextField` with a `Double` `value` |
| `DateRangePicker` + `DateRangeCalendar` + `DateRangePickerDialog` | like `DatePicker`: `ObjectProperty<DateRange> value` |
| `RangeSlider` | like `Slider`, with `lowValue` / `highValue` |
| `SplitButton` (filled, tonal, outlined, elevated) | `SplitMenuButton`: items, `onAction`, `show()` |
| `MenuHeader` | a `CustomMenuItem`: section titles in any menu |
| `FormPane` + `FormRow` (labels beside or above fields), `ValidationSummary` | a layout pane like `GridPane`; fields implement `Validatable` |
| `AdaptiveScaffold` (size classes, bar / rail / drawer, list-detail panes) | a `Scaffold` |
| `Divider` | `Separator` |
| `IconButton` (4 variants, toggle), `Fab` (3 sizes, 4 colors, extended), `Buttons` factories | `Button` |
| `Badge` | `Label` |
| `Card`, `ListItem`, `Avatar`, `TopAppBar`, `BottomAppBar`, `Carousel`, `Snackbar`, `BottomSheet`, `SideSheet`, `Banner`, `StatusLight` | `Control` + `Skin` |
| `Scaffold` | a layout pane, like `BorderPane`; also hosts the overlays |
| `Icon` (2,000+ Material icons), `StateLayer`, `Overlay`, `Tooltips`, `DatePickerDialog` | building blocks and helpers |

Overlays (dialogs, sheets, snackbars, modal drawer) open inside the window, above a scrim. They
need a `Scaffold` ancestor, or a `StackPane` as the scene root.

```java
MaterialDialog.confirm(node, "Delete mission?", "This can't be undone.", "Delete", "Cancel")
        .thenAccept(confirmed -> { if (confirmed) delete(); });

MaterialDialog<ButtonType> dialog = new MaterialDialog<>("Reset settings?", "Preferences go back to defaults.");
dialog.getButtonTypes().addAll(ButtonType.CANCEL, new ButtonType("Reset", ButtonData.OK_DONE));
dialog.show(node).thenAccept(button -> ...);

Snackbar.make(node, "Waypoint deleted").action("Undo", this::undo).show();

MaterialTextField callsign = new MaterialTextField("Callsign");
callsign.setMaxLength(8);
callsign.setValidator(text -> text.length() < 3 ? "At least three letters" : null);
callsign.setOnAction(e -> submit());     // it is a TextField
```

### 5. Keyboard and accessibility

| Component | Keys |
|---|---|
| Navigation bar / rail / drawer | arrows move focus, Home / End, Enter / Space select |
| Segmented button | Left / Right move between segments (toggle group arrows in single select) |
| Switch, chips, buttons | Space / Enter (standard button behavior); Delete / Backspace remove input chips |
| Clickable `ListItem`s in a list, clickable `Card`s | Up / Down between list items, Enter / Space activate |
| Time picker | editable hour / minute boxes; on the dial, arrows, Page Up / Down, Enter (hours to minutes) |
| Carousel | Left / Right, Home / End |
| Dialogs, sheets, modal drawer | Escape dismisses; Tab and Shift+Tab stay inside the overlay (focus trap) |
| Search bar | Down opens / enters suggestions, Escape closes them |
| Range slider | Tab between the two thumbs; arrows, Page Up / Down, Home / End |
| Number field | Up / Down one step, Page Up / Down ten steps; Enter commits |
| Date range calendar | arrows by day / week, Page Up / Down by month, Home / End, Enter selects start then end |
| Date range picker, split button | Enter / Space / Alt+Down open the dialog or menu |

Components report accessible roles and states to screen readers: switches and filter chips are
toggle buttons, navigation destinations are selectable tab items, clickable cards and list items
are buttons, password fields hide their text, icon buttons take their name from their tooltip.

## Troubleshooting

**Black flickering during animations on Linux** (switches, progress indicators, ripples).
Seen with NVIDIA drivers under XWayland (JavaFX has no native Wayland support), e.g. on COSMIC.
It comes from vsynced buffer swaps, not from Material3FX: the software renderer (`-Dprism.order=sw`)
doesn't show it, and disabling dirty-region repaints (`-Dprism.dirtyopts=false`) doesn't help.
Turning vsync off fixes it:

```
java -Dprism.vsync=false ...          # and/or the environment variable __GL_SYNC_TO_VBLANK=0
```

The demo does this by default on Linux (`./gradlew :material3fx-demo:run -Pvsync=true` to compare).
Other diagnostics: `-Pprism=sw` (software renderer), `-Pdirtyopts=false`.

**Clicks on a menu or dropdown go to the window behind it (Linux, Wayland).**
On some Wayland compositors (seen on COSMIC, through XWayland), the parts of a JavaFX popup
that extend outside its window do not receive clicks: the click reaches the window behind,
which comes to the front. This is a JavaFX / XWayland limitation that every JavaFX application
has there, and Material3FX does not work around it: JavaFX positions its popups itself, and
moving them from outside fights that positioning. Leave room below controls that open popups
near the bottom of a window, or make the window taller.

## Releasing

1. Set a release version in `build.gradle.kts` (drop `-SNAPSHOT`).
2. Build the signed bundle:
   `./gradlew :material3fx:publishAllPublicationsToStagingRepository -PsigningKey="$(cat key.asc)" -PsigningPassword=...`
   (an ASCII-armored GPG key; the public key must be on a key server).
3. Zip the contents of `build/staging-deploy` and upload it at
   [central.sonatype.com](https://central.sonatype.com) (namespace `io.github.squoirelle`, verified
   through the GitHub account).

CI (`.github/workflows/ci.yml`) runs the tests on Linux, Windows and macOS with the minimum
versions, plus Java 21 + JavaFX 23 and Java 25 + JavaFX 25, and builds an unsigned bundle.

## Project layout

- `material3fx`: the library (JPMS module `io.github.squoirelle.material3fx`)
- `material3fx-demo`: the showcase app and the screenshot renderer
- `material3fx-sample`: Stockroom, a sample application built with FXML

## License

Material3FX is licensed under the [Apache License 2.0](LICENSE). It bundles, from Google:

- the Material Icons and Material Icons Outlined fonts (Apache 2.0),
- the Roboto fonts (SIL Open Font License 1.1, text included next to the fonts),

and its color science is a modified port of
[material-color-utilities](https://github.com/material-foundation/material-color-utilities)
(Apache 2.0). See [NOTICE](NOTICE).
