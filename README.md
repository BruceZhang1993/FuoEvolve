# FuoEvolve

FeelUOwn mobile player prototype for Android and iOS.

The first stage uses Kotlin Multiplatform and Compose Multiplatform for shared UI
and state. FeelUOwn Python core plus `fuo-netease` provide search and media
resolution. Android uses Media3 for native playback; iOS has a Swift AVPlayer
shell ready for the same bridge.

## Modules

- `shared`: shared Compose UI and player state.
- `androidApp`: Android app, Chaquopy bridge, Media3 playback service.
- `iosApp`: iOS Swift shell and native playback bridge.
- `androidApp/src/main/python/fuo_mobile`: Python adapter around FeelUOwn core.

## Android

The Android build packages Python through Chaquopy. The default provider list is
stored in `androidApp/src/main/assets/providers.json` and enables `fuo_netease`.

```bash
./gradlew :androidApp:assembleDebug
```

This project requires a modern Android toolchain and JDK 17 or newer.

## iOS

The Swift shell is under `iosApp/FuoEvolve`. It defines the same native playback
surface using AVPlayer and MediaPlayer remote commands. The Python Apple Support
XCFramework still needs to be added to the Xcode project before device builds.

## Provider Extension

Add a Python package dependency and append a provider module to
`providers.json`, for example:

```json
{
  "enabled": ["fuo_netease", "fuo_qqmusic"]
}
```
