# Changelog

## v0.8.1

### Fixed

- Fixed keyboard input routing for modal dialogs containing multiple text fields.
- Character input is now sent to the currently focused control before the dialog root is queried.
- Keyboard editing actions, including Backspace, cursor movement, Enter, and Escape, now follow the focused control in dialogs.
- Dialog focus is still validated against the active dialog tree, preventing detached or underlying-page controls from receiving input.

### Compatibility

- No breaking changes in this release.
- Existing dialog implementations and `UiTextField` usage remain compatible.
- Downstream mods no longer need to manually forward text input through individual fields, for example `fieldA.textInput(...) || fieldB.textInput(...)`.
