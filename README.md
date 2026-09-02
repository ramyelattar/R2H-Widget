# R2H Widget

**R2H Widget** is an Android customization studio focused on polished home-screen widgets, wallpapers, icon experiences, and coordinated themes.

The application combines native Android widget providers with a Jetpack Compose configuration experience while keeping launcher rendering compatible with Android's `RemoteViews` widget model.

## Widget Catalog

| Widget | Implementation |
| --- | --- |
| Digital Clock | Live `TextClock`-based launcher widget |
| Analog Clock | Locally rendered analog dial |
| Battery | Device battery state and power events |
| Weather | Location-aware weather through Open-Meteo |
| Search | Launcher search widget |
| Music Player | Media session / notification-based playback state |

Legacy widget records remain supported where required for existing pinned instances.

## Features

### Widget Studio

R2H Widget provides per-widget configuration rather than duplicating every visual variation as a separate catalog item.

Supported configuration areas include:

- Typography
- Layout
- Backgrounds
- Effects
- Actions
- Responsive sizing
- Presets
- Per-widget-instance persistence

### Digital Clock

- Native launcher time updates through `TextClock`
- Configurable time and date presentation
- Responsive layout
- Presets and visual effects
- Launcher-safe font mapping

### Analog Clock

- Custom painted dial
- Minute-boundary updates
- Time and timezone resynchronization
- Launcher-safe bitmap rendering

### Battery

- Real device battery state
- Event-driven updates
- No unnecessary minute polling

### Weather

- Real Open-Meteo data
- Location-aware updates
- Explicit unavailable states when permission, network, location, or source data is missing

### Music

- Reads supported media-session / notification state
- Playback controls and metadata
- Requires Android media / notification-listener access where applicable

## Offline Customization

The application includes dedicated customization surfaces for:

- **Icons**
- **Wallpapers**
- **Themes**
- **Favorites**

### Wallpapers

- Local wallpaper catalog
- Search and category filtering
- Favorites
- Preview and inspection
- Home / Lock / Both targets
- Application through Android `WallpaperManager`

### Icon Packs

The project contains a bundled icon-pack build pipeline and multiple icon-pack variants.

The Gradle build can package generated icon-pack APKs into the main application so compatible launchers can use the provided icon resources.

> Android launchers control whether an icon pack can be applied directly. Launchers without a supported public apply API may require the user to select the icon pack manually.

### Themes

Themes coordinate multiple customization choices, including:

- Widget / clock presets
- Icon selections
- Wallpapers
- Accent configuration

## Architecture

R2H Widget separates two UI environments:

1. **Jetpack Compose** for the application and configuration studio.
2. **RemoteViews / AppWidget providers** for launcher widgets.

The two environments share configuration/domain models while rendering through their platform-specific UI systems.

## Tech Stack

| Layer | Technology |
| --- | --- |
| Platform | Android |
| Language | Kotlin |
| App UI | Jetpack Compose |
| Design | Material 3 |
| Widgets | Android AppWidget + RemoteViews |
| Persistence | DataStore |
| Weather | Open-Meteo |
| Wallpaper | Android WallpaperManager |
| Testing | JUnit, Robolectric, Compose UI Test |
| Build | Gradle Kotlin DSL |

## Android Configuration

- **Minimum Android SDK:** 34
- **Target SDK:** 37
- **Compile SDK:** 37
- **Application ID:** `com.r2h_widget`
- **Current version:** `1.0`

## Build

### Requirements

- Android Studio
- Android SDK
- JDK compatible with the project Gradle toolchain

### Debug Build

```bash
./gradlew :app:assembleDebug
```

On Windows:

```powershell
.\gradlew.bat :app:assembleDebug
```

The application build also prepares the bundled icon-pack payload required by the selected build variant.

### Unit Tests

```bash
./gradlew :app:testDebugUnitTest
```

### Lint

```bash
./gradlew :app:lintDebug
```

## Release Signing

Release signing can be provided locally through environment variables or Gradle properties.

The repository does not need to contain signing passwords or private keys.

## Permissions & Platform Notes

Some features depend on Android platform capabilities:

- Weather requires suitable location access.
- Music state/control may require notification-listener or media access.
- Wallpaper changes use Android's wallpaper APIs.
- Icon-pack activation depends on the installed launcher.
- Widget pinning is ultimately confirmed by the launcher.

## Repository

[R2H-Widget](https://github.com/ramyelattar/R2H-Widget)

---

**R2H — Native Android customization without unnecessary cloud dependencies.**
