# Define Configuration

RCUI has two related but separate parts:

1. The optional `config` module describes, loads and saves values.
2. The `core` module displays those values with UI components.

The processor does not invent your page layout. You still choose the section
and control for each value.

## Define a model

The model must be public, concrete and have a public no-argument constructor.
Mark the class with `@RcuiConfig` and persisted fields with `@Setting`:

```java
@RcuiConfig(id = "example", file = "example.yaml",
    wrapperName = "ExampleConfig", schemaVersion = 1)
public final class ExampleConfigModel {
    @Setting(section = "general", key = "enabled",
        title = "Enable feature",
        description = "Turns the feature on or off.")
    public boolean enabled = true;

    @Setting(section = "display", key = "scale",
        title = "Interface scale", min = 0.5, max = 2.0, step = 0.1)
    public double scale = 1.0;
}
```

`id` identifies the configuration. `file` is a relative YAML name.
`wrapperName` is the generated wrapper. `section` and `key` describe the
stored path. `title` and `description` are UI metadata. `min`, `max`
and `step` describe numeric constraints. `schemaVersion` supports migrations.

Only annotated fields are persisted. Fields must be public, non-static and
non-final. Do not put Minecraft objects in the model.

## Load the generated wrapper

The processor generates `ExampleConfig` in the model's package:

```java
try (ExampleConfig config = ExampleConfig.createAndLoad(configDirectory)) {
    boolean current = config.model().enabled;
    config.enabled().set(!current);
    config.flush();
}
```

The wrapper exposes `createAndLoad`, `save`, `flush`, `reload` and
`close`, plus one binding method per field:

```java
config.enabledBinding();
config.scaleBinding();
```

The binding getter reads the current value and its setter updates the model.
Saving remains the host's responsibility. Save on commit, or provide a Save
button and call `flush()` only after confirmation.

## Build controls from bindings

Boolean values use `UiToggle`:

```java
Ui.Node enabledControl = Ui.toggle(
    UiText.literal(""),
    config.enabledBinding()
);
```

For numbers, share one `UiSetting` and `UiNumberSpec` between controls:

```java
UiSetting<Double> scale = UiSetting.of(config.scaleBinding(), 1.0)
    .describedBy(UiText.literal("Controls interface size."));

UiNumberSpec<Double> scaleSpec = UiNumberSpec.builder(UiNumberSpec.DOUBLE)
    .range(0.5, 2.0)
    .step(0.1)
    .formatter(value -> String.format(java.util.Locale.ROOT, "%.1f", value))
    .build();

Ui.Node scaleControl = Ui.numberControl(scale, scaleSpec);
Ui.Node scaleField = Ui.numericField(scale, scaleSpec);
```

The setting owns the binding and default value. The number specification owns
the type, range, step and display format. Reusing them keeps every numeric
control consistent.

## Validate text

Text fields can reject a value before writing it back:

```java
AtomicReference<String> profile = new AtomicReference<>("Rethink");

Ui.Node field = Ui.textField(
    UiBinding.of(profile::get, profile::set)
).placeholder(UiText.literal("Profile name"))
 .validator(value -> value.trim().isEmpty()
     ? UiValidationResult.error(UiText.literal("A name is required"))
     : UiValidationResult.OK);

Ui.Node row = Ui.formField(UiText.literal("Profile name"), field);
```

Validation belongs to the UI interaction, but the host decides whether an
invalid value can be saved and how the error is reported.

## Complete page

```java
Ui.Node content = Ui.column()
    .gap(12)
    .add(Ui.section(UiText.literal("GENERAL"))
        .add(Ui.settingRow(UiText.literal("Enable feature"),
            Ui.toggle(UiText.literal(""), config.enabledBinding()))))
    .add(Ui.section(UiText.literal("DISPLAY"))
        .add(Ui.formField(UiText.literal("Interface scale"),
            Ui.numberControl(scale, scaleSpec))));

UiTemplate page = Ui.topNavigationTemplate()
    .header(UiText.literal("Example Mod"))
    .content(content)
    .footer(Ui.row()
        .mainAxisAlignment(UiMainAxisAlignment.END)
        .add(Ui.button(UiText.literal("Save"), () -> {
            try {
                config.flush();
            } catch (java.io.IOException exception) {
                // Notify the user through the host.
            }
        })))
    .build();
```

Keep the generated wrapper alive while the page can edit it. Close it when the
host screen is disposed, not immediately after building the node tree.

![Configuration page](../imags/configuration-page.png)
<!-- TODO: Add a screenshot showing bindings, numeric constraints and an error. -->

## Demo

The `General` page shows boolean, numeric and selection bindings. `Input`
shows local bindings, and `Data` demonstrates collection editing and validation.
