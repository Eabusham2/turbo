# Profiling FPS with Spark on a server

On DonutSMP you are not op, so use the **client** command: `/sparkc` (also `/sparkclient`).
`/spark` is the server command and won't work there.

1. Install Spark 1.10.170 for Fabric 1.21.11 (Modrinth: "spark"). Needs Fabric API.
2. Join the server and stand where FPS is worst (spawn, a shop area, a mob farm).
3. Run: `/sparkc profiler start --timeout 60`
   Keep playing normally for the full 60 seconds (Spark warns below 30 s).
4. Spark posts a link in chat when it finishes. Send me that link.
   (Stop early: `/sparkc profiler stop`. Re-open the last one: `/sparkc profiler open`.)
5. Do one run with Turbo's settings off (master switch) and one with them on, same spot.

## Reading it yourself
Open the link, expand `Render thread` → `Minecraft.runTick` → `GameRenderer.render`.
The biggest percentages show where frame time goes:
- `EntityRenderDispatcher` / `extractEntity` high → entity settings (decor distance, max distance).
- `BlockEntityRenderDispatcher` high → block entity settings.
- `ParticleEngine` high → particle cap and per-type switches.
- Sodium / `SodiumWorldRenderer` / chunk building high → that's Sodium's area (lower render distance).
- `Window.updateDisplay` / waiting on the GPU high → GPU-bound: lower render distance or resolution.
