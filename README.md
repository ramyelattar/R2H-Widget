# R2H Widget

R2H Widget is an AMOLED-first Android widget studio. The catalog keeps one
stable product per supported home-screen provider; visual variations are
configured inside the studio rather than duplicated as catalog entries.

## Catalog

| Product | ID | Provider |
| --- | --- | --- |
| Digital Clock | `digital-clock` | `DigitalClockWidgetReceiver` (live `TextClock`) |
| Analog Clock | `analog-clock` | `AnalogClockWidgetReceiver` (painted dial bitmap) |
| Battery | `battery` | `BatteryWidgetReceiver` (platform battery broadcasts) |
| Weather | `weather` | `WeatherWidgetReceiver` (`OpenMeteoWeatherRepository`) |
| Search | `search` | `SearchWidgetReceiver` |
| Music Player | `music` | `MusicWidgetReceiver` (media session / notification listener) |

Gallery sections are Widgets, Icons, Walls, and Themes. The legacy calendar
provider remains registered only for widgets pinned before the catalog was
simplified.

## Current implementation

- IMPLEMENTED: six stable catalog products with real RemoteViews providers,
  per-instance persistence, pin/update/edit flows, and resize callbacks.
- IMPLEMENTED: Digital Clock studio with typed time/date/layout/background,
  effects, actions, responsive sizing, presets, and launcher-safe system-font
  mapping shared by Compose and RemoteViews.
- IMPLEMENTED: Analog Clock studio and shared painter, including minute-boundary
  refreshes and real launcher rendering.
- IMPLEMENTED: Battery studio and event-driven rendering from real device state.
- IMPLEMENTED: Weather configuration and rendering through Open-Meteo when a
  location is available; the widget shows an explicit unavailable state when
  permission, location, network, or source data is unavailable.
- IMPLEMENTED: Search and Music studios with real launcher providers. Music
  playback data requires the user to grant notification-listener/media access.
- IMPLEMENTED: offline Icons, Walls, and Themes surfaces. Icon previews use
  actual installed app icons plus bundled mappings; wallpapers are applied by
  `WallpaperManager`; themes persist coordinated icon, wallpaper, clock, and
  accent selections.
- IMPLEMENTED: persistent favorites, widget-instance cleanup, legacy codec
  migration, and stable-ID navigation.
- LIMITATION: Android launchers own icon-pack application. Nothing Launcher has
  no verified public direct apply API, so the app reports manual launcher
  selection instead of claiming that an icon pack was applied.
- NOT DEVICE VERIFIED in this source/build pass: launcher pin acceptance,
  wallpaper application, icon-pack selection, and multi-instance visual checks.

## Build gates

```text
.\gradlew.bat :app:testDebugUnitTest --no-daemon --max-workers=1 --console=plain
.\gradlew.bat :app:assembleDebug --no-daemon --max-workers=1 --console=plain
.\gradlew.bat :app:lintDebug --no-daemon --max-workers=1 --console=plain
```

## Widget update strategy

- **Digital Clock** uses Android `TextClock`; the launcher owns the live local
  time refresh and `updatePeriodMillis` remains disabled.
- **Analog Clock** paints the dial into a bitmap and schedules one lightweight
  minute-boundary alarm. Doze may defer an exact tick; the next update catches
  up. Time-zone, time-set, boot, and launcher update broadcasts resync it.
- **Battery** reacts to battery and power broadcasts. It does not poll with a
  minute worker.
- **Weather** refreshes after location updates and network reads from the
  Open-Meteo endpoint. Missing permissions/data produce an unavailable state,
  never fabricated weather.
- **Music** reads the selected media session/notification snapshot and updates
  from playback actions and media access events.

All providers resolve their typed configuration from the persisted
`appWidgetId` record before rendering. Compose previews and launcher rendering
share domain models but do not share a UI tree.

## Offline customization

`customization/` contains the independent DataStore-backed selection boundary:

- **Icons**: Graphite Liquid Glass and Neon Liquid Glass. The app renders real
  installed application icons with the selected surface treatment and parses
  the classic `appfilter.xml` mapping. Compatible launchers may offer their own
  import/apply flow; Nothing Launcher remains manual-selection only.
- **Walls**: local bundled artwork with preview, search/category filtering,
  favorites, zoom/pan inspection, and Home/Lock/Both `WallpaperManager` apply
  targets.
- **Themes**: offline bundles coordinating a clock preset, icon pack, local
  wallpaper, and app accent. Applying a theme applies the local wallpaper and
  leaves launcher-owned icon selection explicit.

## Compatibility and scope

Legacy digital-clock, battery, weather, and calendar records remain decodable.
Calendar is retained only for already-pinned instances. Remote Compose is not
used in production. Billing, cloud sync, AI, ONNX, and remote widget data are
outside the current offline customization scope.
