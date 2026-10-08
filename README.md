# Dael's Phone

A pretend phone for a toddler. Everything is local: no network, no real calls, no ads.

## What's inside

- **Phone**: a dial pad that speaks each digit. Dialing any number "connects" to a silly animal or vehicle.
- **Family**: tap Mom, Dad, or anyone else to start a pretend video call that plays a clip you recorded.
  The front camera shows the child's own face in the corner like a real call.
- **Surprise incoming calls**: every few minutes someone from the family "calls".
- **Animals, Piano, Paint, Peekaboo** games.
- **Parent settings**: hold the top-right corner of the home screen for 3 seconds, then answer a
  multiplication question. From there you add photos, record or pick videos, toggle kiosk mode,
  and exit the app.

## Kiosk mode

With kiosk mode on, the app pins itself (Android screen pinning) on every resume so the Home and
Back gestures cannot leave it. Android shows a one-time confirmation dialog the first time.
Use "Unpin and exit app" in parent settings to leave. The app also registers as a HOME launcher,
so on a dedicated old phone you can make it the default home screen.

## Voices and icons

Spoken phrases are pre-generated natural voice clips in `app/src/main/res/raw`, made with the
[Kokoro](https://github.com/hexgrad/kokoro) neural TTS by `tools/gen_voices.py`. Re-run it with
`--child <name>` to regenerate them for your own child. Phrases without a clip (for example a
family member with an unusual name) fall back to the phone's text-to-speech engine.
Icons are drawn as vector drawables by `tools/gen_icons.py`.

## Making it impossible to unpin (dedicated phone only)

Screen pinning can be escaped by holding Back and Recents. On a spare phone with no Google
account signed in, make the app the device owner once over ADB and lock task mode becomes
unescapable until you remove it:

```
adb shell dpm set-device-owner me.tewodros.dael/.DaelAdmin
adb shell dpm remove-active-admin me.tewodros.dael/.DaelAdmin   # to undo
```

This is not possible on a phone that already has accounts, which is why it is not the default.

## Build

Needs JDK 17+ and the Android SDK (`local.properties` points at `~/Android/Sdk`).

```
./gradlew installDebug      # build and install on a connected phone
```
