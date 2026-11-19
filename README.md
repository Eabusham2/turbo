# Turbo

Client-side FPS mod for **Minecraft 1.21.11 (Fabric)**. It sits on top of Sodium and skips
drawing and audio work you don't need: far-away clutter, hidden-by-fog objects, particle floods,
inaudible sounds, overlays.

**Rule for every setting: it may change what is drawn or heard, never how the game plays.**
No setting touches ticks, physics, mob AI, input, crosshair targeting or network packets.
See [VERIFICATION.md](VERIFICATION.md) for the hook-by-hook audit.

## Presets

Pick one in ModMenu → Turbo → Presets, then Save.

| Preset | Looks | What it does |
|---|---|---|
| **Minimal** (default) | Identical to vanilla | Skips the world render while minimized, skips entities and block entities fully hidden by fog, skips sounds too far away to hear. |
| **Optimized** (recommended) | About the same | Minimal, plus: hides item frames, armor stands, paintings, dropped items, XP orbs, projectiles past 32 blocks; block entities past 48; nametags past 32; one model per dropped stack; particle cap; smart leaves; half-speed water/lava/fire animation; sound cap and de-duplication; 30 FPS when unfocused; no menu blur. |
| **Potato** | Noticeably plainer | Every setting on, smallest distances. Also mutes all sounds and hides the scoreboard, since it is literally everything; switch those two back off if you want them. |

Changing any single setting afterwards switches the preset to **Custom**.

## Settings (115)

| Tab | Examples |
|---|---|
| Frame | Skip rendering while minimized, unfocused / in-menu FPS caps, no menu blur, no title panorama |
| Entities | Fog culling, max entity distance, nametag distance, hide decor past a distance (item frames, armor stands, paintings, items, XP, TNT, projectiles, boats, minecarts), no shadows, no fire on entities, no guardian beams, capes, skin layers, elytra, shield patterns, armor trims and glint |
| Block Entities | Hide all past N, per type (chests, signs, heads, banners, bells, conduits, gateways, spawner mobs, enchanting books, beacon beams), banner layer cap |
| Particles | Exact global cap, spawn distance, per-type switches (block break, rain, explosion, potion, crit, bubbles, campfire smoke, portal, firework, redstone dust, end rod, pickup animation) |
| Render | Damage tilt, view bob, all screen overlays, mining cracks, map markers, rain/snow, clouds, stars, End sky, fog (all / underwater / Nether / End) |
| Animations | Freeze or half-speed animated textures, chest lids, banner wave, cape flutter |
| Sound | Skip inaudible sounds, distance cull, concurrent cap, same-tick de-duplication |
| HUD | Vignette, scoreboard, toasts, full-health mount bar |
| World | Smart leaves |
| Aggressive | All particles / overlays / block entities / sounds off |

Optional debug line (General tab): shows the preset, live particles and how many entities and
block entities were skipped last frame.

## Install

1. Fabric Loader 0.18.4+ for Minecraft 1.21.11
2. [Fabric API](https://modrinth.com/mod/fabric-api), [Cloth Config](https://modrinth.com/mod/cloth-config) 21.11.x, [ModMenu](https://modrinth.com/mod/modmenu) 17.x
3. Download `turbo-<version>.jar` from [Releases](../../releases) into `mods/`

Tested with Sodium 0.8.14. Turbo does not touch terrain/chunk rendering, which is Sodium's job.

## Build

Java 21. The wrapper downloads Gradle 9.8.2 and Loom 1.16.3.

```
./gradlew build                  # build/libs/turbo-<version>.jar
./gradlew runClient -PwithSodium # dev client with Sodium (dev only, never bundled)
./gradlew runServer              # local test server in run-server/
```

Dev-only JVM flags: `-Dturbo.mixinAudit=true` forces every mixin to apply at startup;
`-Dturbo.openConfigOnJoin=true` opens the settings screen after joining a world.

Pushing a tag like `v1.0.0` makes GitHub Actions build the jar and attach it, with the
sources jar, to a release.

## License

MIT
