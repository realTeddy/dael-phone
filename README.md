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

## Build

Needs JDK 17+ and the Android SDK (`local.properties` points at `~/Android/Sdk`).

```
./gradlew installDebug      # build and install on a connected phone
```
