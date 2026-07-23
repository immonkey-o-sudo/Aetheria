# Video Overlay Feature

Plays a video (YouTube, Twitch, or a direct file/stream URL) fullscreen over the
game, with a hotkey that swaps between "video fullscreen" and "gameplay
fullscreen." Lives entirely in `io.hamlook.aetheria.features.video` as a
self-contained module — it doesn't depend on any of the feature packages that
weren't part of this codebase snapshot.

## How it works

- **`VideoStreamResolver`** turns a YouTube/Twitch page URL into a direct,
  playable stream URL using `yt-dlp`. Direct file/stream links (`.mp4`,
  `.m3u8`, `rtmp://`, ...) are passed straight through untouched.
- **`YtDlpBootstrap`** downloads a portable `yt-dlp.exe` into
  `config/Aetheria/bin/` the first time it's needed. No manual install step.
- **`VideoPlayer`** wraps VLCJ in headless/callback mode — VLC decodes into a
  raw pixel buffer instead of opening its own native window.
- **`VideoFrameTexture`** uploads that raw buffer into an OpenGL texture each
  frame (`GL_BGRA`, matches VLC's RV32 output byte-for-byte, no channel swap
  needed).
- **`GuiVideoOverlay`** is a normal `GuiScreen` that draws that texture
  fullscreen, letterboxed to preserve aspect ratio. It overrides
  `doesGuiPauseGame()` to `false` so the world keeps ticking behind the video
  (important on Hypixel/multiplayer).
- **`VideoOverlayFeature`** polls the configured hotkey every client tick and
  calls `mc.displayGuiScreen(...)` / `mc.displayGuiScreen(null)` to swap —
  reusing Minecraft's own screen-open/close logic for mouse grab/release.

## Requirements

- **64-bit VLC media player installed** on the user's machine. VLCJ discovers
  `libvlc` at runtime via `NativeDiscovery`; VLC itself isn't bundled in the
  mod jar. (JVM bitness must match VLC's — both 64-bit.)
- **Windows** — this was scoped Windows-only; `yt-dlp.exe` and the download
  bootstrap assume that.
- **Internet access** for the one-time `yt-dlp.exe` download and for
  resolving YouTube/Twitch URLs each time playback starts.

## Configuring

No in-game GUI editor exists for this yet (the MoulConfig-based config screen
this pack otherwise uses wasn't part of this snapshot). Edit the JSON file
directly — it's created with defaults on first run:

```
config/Aetheria/video-overlay.json
```

| Field                      | Meaning                                                        |
|----------------------------|-----------------------------------------------------------------|
| `videoUrl`                 | YouTube / Twitch / direct video URL to play                    |
| `toggleKeyCode`             | LWJGL2 `Keyboard.KEY_*` code for the swap hotkey (default F10)  |
| `volume`                    | 0–100                                                           |
| `muteGameWhileFullscreen`   | Mutes Minecraft's master volume while the video is fullscreen   |
| `loop`                      | Restarts playback automatically when the video ends             |

## Known limitations / things to sanity-check on your build machine

- I couldn't compile or run this against a real Minecraft/Forge 1.8.9
  toolchain in the environment I wrote it in (no network access to Mojang/
  Forge/Maven Central from there), so **please build and test locally**
  before relying on it.
- Class names for `Tessellator` / `WorldRenderer` / `GlStateManager` /
  `VertexFormats` in `GuiVideoOverlay` reflect the standard 1.8.9 MCP mapping
  layout used elsewhere in this pack. If your exact MCP mapping version names
  these slightly differently, you'll need to adjust those imports.
- Live Twitch streams and age-restricted/region-locked YouTube videos may
  behave differently through `yt-dlp -f b`; if a link fails to resolve, check
  the log line yt-dlp prints and adjust the format selector in
  `VideoStreamResolver` if needed (e.g. `-f "best[height<=1080]"`).
- Both VLC's audio output and Minecraft's own sound engine run independently;
  `muteGameWhileFullscreen` only affects Minecraft's volume, not VLC's.
