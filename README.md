# Screenshot Overhaul

![Fabric](https://img.shields.io/badge/Loader-Fabric-blue)
![NeoForge](https://img.shields.io/badge/Loader-NeoForge-orange)
![Environment: Client](https://img.shields.io/badge/Environment-Client-red)
![License: MIT](https://img.shields.io/badge/License-MIT-yellow)

Improve Minecraft screenshots with a built-in gallery, metadata, custom names, previews, panoramas, and map
integrations.

---

## Features

- Built-in screenshot gallery available from the title screen and pause menu.
- Full-screen screenshot viewer with navigation, delete, and copy actions.
- Search, sorting, and favorites filtering in the gallery.
- Persistent favorites and an option to hide specific screenshots from compatible maps.
- PNG metadata added to screenshots: position, dimension, biome, seed, world, server, version, resource packs, shader,
  and timestamp.
- Metadata editing directly from the gallery.
- Configurable screenshot directory, with support for subfolders.
- Custom screenshot file names using tokens (`<datetime>`, `<worldname>`, `<shader>`, etc.).
- Animated screenshot preview after capture.
- Options to hide the HUD, chat, or hand only while taking a screenshot.
- Optional automatic screenshot capture when an advancement is unlocked.
- Panorama capture with a dedicated keybind.
- Panorama gallery and support for using a captured panorama as the title screen background.
- Optional integration with Xaero's World Map and JourneyMap to display screenshots on the map.
- Iris compatibility to save the active shader in metadata and file-name tokens.
- Fabric and NeoForge support.

---

## Installation

1. Install Minecraft `26.1` with Fabric or NeoForge.
2. Install the dependencies for your loader:
    - Fabric: Fabric Loader, Fabric API.
    - NeoForge: NeoForge.
3. Download the Screenshot Overhaul `.jar` file.
    - [Modrinth](https://modrinth.com/project/screenshot-overhaul)
    - [CurseForge](https://www.curseforge.com/minecraft/mc-mods/screenshot-overhaul)
4. Place the `.jar` file in your `.minecraft/mods/` folder.
5. Launch the game once to generate the configuration file.

This is a client-side mod: it must be installed on the client, not on a dedicated server.

---

## Quick Usage

- Open the screenshot gallery from the title screen or pause menu.
- Press `F9` to take a panorama screenshot.
- Use the `Change panorama` button on the title screen to choose a captured panorama as the background.
- Open the settings from the gallery, through Mod Menu on Fabric, or from NeoForge's mod configuration screen.

---

## Configuration

The configuration file is created automatically on first launch: `.minecraft/config/screenshot_overhaul.json`.

### General

| Option                            | Type    | Default | Description                                                     |
|-----------------------------------|---------|:-------:|-----------------------------------------------------------------|
| `enableWholeMod`                  | Boolean | `true`  | Enables or disables the mod's features.                         |
| `showScreenshotsOnXaerosWorldMap` | Boolean | `true`  | Shows screenshots on Xaero's World Map if the mod is installed. |
| `showScreenshotsOnJourneyMap`     | Boolean | `true`  | Shows screenshots on JourneyMap if the mod is installed.        |

### Screenshot

| Option                        | Type    |    Default    | Description                                                                               |
|-------------------------------|---------|:-------------:|-------------------------------------------------------------------------------------------|
| `screenshotsFileName`         | String  | `<datetime>`  | Pattern used to name screenshots. Can include tokens and subfolders.                      |
| `screenshotsDir`              | Path    | `screenshots` | Directory where screenshots are saved.                                                    |
| `showChatMessage`             | Boolean |    `true`     | Shows the Minecraft chat message after taking a screenshot.                               |
| `showPreview`                 | Boolean |    `true`     | Shows an animated preview after taking a screenshot.                                      |
| `hideHudOnScreenshot`         | Boolean |    `false`    | Hides the HUD while taking a screenshot.                                                  |
| `hideChatOnScreenshot`        | Boolean |    `false`    | Hides the chat while taking a screenshot. Disabled when `hideHudOnScreenshot` is enabled. |
| `hideHandOnScreenshot`        | Boolean |    `false`    | Hides the player's hand while taking a screenshot.                                        |
| `grabScreenshotOnAdvancement` | Boolean |    `false`    | Automatically takes a screenshot when an advancement is unlocked.                         |
| `advancementScreenshotDelay`  | Integer |     `20`      | Delay before taking an advancement screenshot, in ticks. Clamped between `0` and `100`.   |

### Panorama

| Option                    | Type    |        Default        | Description                                                                    |
|---------------------------|---------|:---------------------:|--------------------------------------------------------------------------------|
| `panoramaFolderName`      | String  | `panorama_<datetime>` | Pattern used to name panorama folders.                                         |
| `panoramaResolution`      | Integer |        `1024`         | Resolution in pixels for each panorama face. Clamped between `256` and `4096`. |
| `rotationSpeed`           | Integer |         `10`          | Title screen panorama rotation speed. Clamped between `0` and `100`.           |
| `rotationDirection`       | Enum    |       `TO_LEFT`       | Panorama rotation direction. Values: `TO_LEFT`, `TO_RIGHT`.                    |
| `verticalAngle`           | Integer |         `10`          | Panorama vertical angle. Clamped between `-180` and `180`.                     |
| `startingHorizontalAngle` | Integer |          `0`          | Initial panorama horizontal angle. Clamped between `0` and `360`.              |

### UI-Saved Data

These values are saved by the mod but are not primary gameplay options.

| Option                 | Type           |   Default   | Description                                             |
|------------------------|----------------|:-----------:|---------------------------------------------------------|
| `screenshotSortOrder`  | Enum           | `DATE_DESC` | Saved sort order for the screenshot gallery.            |
| `panoramaSortOrder`    | Enum           | `DATE_DESC` | Saved sort order for the panorama gallery.              |
| `selectedPanoramaUUID` | UUID or `null` |   `null`    | Panorama currently used as the title screen background. |

### Naming Tokens

The `screenshotsFileName` and `panoramaFolderName` options support the following tokens:

| Token         | Description                                          |
|---------------|------------------------------------------------------|
| `<datetime>`  | Date and time formatted by Minecraft for file names. |
| `<year>`      | Four-digit year.                                     |
| `<month>`     | Two-digit month.                                     |
| `<day>`       | Two-digit day.                                       |
| `<hour>`      | Two-digit hour.                                      |
| `<minute>`    | Two-digit minute.                                    |
| `<second>`    | Two-digit second.                                    |
| `<worldname>` | Singleplayer world name, empty in multiplayer.       |
| `<serverip>`  | Server address, empty in singleplayer.               |
| `<version>`   | Minecraft version.                                   |
| `<shader>`    | Active shader if Iris is installed, empty otherwise. |

Examples:

```text
<year>/<month>/<datetime>
<worldname>/screenshot_<datetime>
<serverip>/<datetime>_<shader>
```

---

## Compatibility

| Mod               | Integration                                                |
|-------------------|------------------------------------------------------------|
| Mod Menu          | Settings access on Fabric.                                 |
| Xaero's World Map | Optional screenshot display on the map.                    |
| JourneyMap        | Optional screenshot display on the map.                    |
| Iris              | Active shader detection for metadata and file-name tokens. |

---

## License

MIT - see [LICENSE](LICENSE) for details.
