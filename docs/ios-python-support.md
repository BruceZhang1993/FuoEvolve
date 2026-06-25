# iOS Python Runtime Wiring

The iOS app shell is ready for AVPlayer and system remote controls, but the
Python runtime is intentionally left as an explicit integration step.

To complete it:

1. Add BeeWare Python Apple Support `Python.xcframework` to the Xcode target.
2. Bundle the same `fuo_mobile` package used by Android.
3. Bundle/install `feeluown==5.1.1` and `fuo-netease==1.0.8` plus their Python
   dependencies.
4. Replace `PythonCoreBridge` stubs with calls to `fuo_mobile.bridge`.

Keep the Swift-facing JSON contract identical to Android:

- `search(keyword)` returns `{"tracks": [...]}`
- `play(trackId)` returns a `PlaybackPayload`
- `next()` and `previous()` return a `PlaybackPayload` or `null`
