# Verification

## How Turbo is checked
- Built against the real Minecraft 1.21.11 jar with Mojang mappings; every hook was written
  after reading the decompiled method (`./gradlew genSources`).
- `defaultRequire: 1` in `turbo.mixins.json`: a hook that misses its target fails
  loudly instead of silently doing nothing. All 31 mixins pass a forced Mixin audit
  (`-Dturbo.mixinAudit=true`).
- Dev client with Sodium 0.8.14 on a local server, 89 settings on at once. Scene: armor stands,
  items (enchanted and full stacks), XP, a burning armored zombie with trim and shield, a
  guardian, lingering potion, TNT explosion, thunderstorm, chests, signs, two-layer banner,
  spawner, enchanting table, campfire, redstone torch, crit and dust particle bursts, near and
  far sounds, Blindness. No errors. The settings screen opens and renders. Preset logic is
  checked by a small test (Minimal equals the defaults; personal settings survive presets).

## Gameplay audit (why nothing changes how the game plays)
Every hook sits in a drawing, audio or HUD path:

| Hook | What it changes | Why gameplay is untouched |
|---|---|---|
| `EntityRenderDispatcher#shouldRender` | Whether an entity is drawn this frame | Only gates extraction/submission of the render state; the entity still ticks, collides, can be hit and picked up |
| `EntityRenderer#extractRenderState`, `#extractShadow`; Guardian/Wither/Avatar/ItemEntity renderers | Fields of the render state (nametag, leash, fire, shadow, beam, armor glow, cape, skin layers, item copies, bob) | Render states are throwaway per-frame copies |
| `WingsLayer#submit`, `EquipmentLayerRenderer#renderLayers`, `ShieldSpecialRenderer#extractArgument`, `ItemStackRenderState$LayerRenderState#setFoilType` | Elytra, armor glint and trims, shield patterns, dropped-item glint | Draw calls only; the items keep their data |
| `BlockEntityRenderDispatcher#tryExtractRenderState`; Chest/Banner renderers | Whether / how a block entity is drawn | Vanilla already returns null here for block entities it doesn't draw; chests still open |
| `ClientLevel#doAddParticle`, `#addDestroyBlockEffect`, `#addBreakingBlockEffect`, `#trackExplosionEffects`; `ParticleEngine#add` | Which particles spawn | Particles are client-only visuals |
| `SoundEngine#play` | Which sounds start | Client audio; nothing reads whether a sound played. Music and UI are never culled |
| `LevelRenderer` clouds/weather passes, block-destroy cracks; `SkyRenderer` stars/End sky; fog classes | Sky, weather, fog, cracks | Drawing only |
| `GameRenderer#renderLevel` (minimized) | Skips drawing the world | Still runs the two non-drawing jobs it does: crosshair `pick()` and draining the client light queue |
| `GameRenderer#bobView`, `#bobHurt`, `#processBlurEffect`; `Gui`, `ScreenEffectRenderer`, `ToastManager`, `PanoramaRenderer` | Camera sway, overlays, HUD, menus | Camera matrix and HUD only; aim comes from the player's look vector |
| `SpriteContents` / `$AnimationState` | Texture animation speed | Texture uploads only |
| `LeavesBlock#skipRendering` | Inner leaf faces | Only called by face culling (`Block#shouldRenderFace`) and Sodium's occlusion; not collision or light |
| `FramerateLimitTracker#getFramerateLimit` | FPS cap | Ticks run at 20/s regardless of FPS |

## Exact (Minimal) optimizations, and their limits
- **Fog culling**: only where everything past the fog's end is pure fog colour (water, lava,
  powder snow, Blindness, Darkness, the Nether; verified in the decompiled fog environments).
  Never in the Overworld or End, where the sky has its own gradient. Players, visible names,
  glowing, leashed, displays, beams and the ender dragon are always drawn. Note: Sodium already
  skips whole chunk sections hidden by fog, so on top of Sodium this mostly catches single
  entities in sections that are partly visible.
- **Inaudible sounds**: skipped only after subtitles were notified, only for one-shot, non-moving,
  non-streamed sounds, and only 8+ blocks past vanilla's own hearing range.
- **Minimized**: see the audit table.

## Planned items that were removed
The old config listed 134 ideas with no code. 37 were built. The rest were removed:

**Would change gameplay:** throttle mob AI and pathfinding, villager gossip, far-entity animation
ticks, squid tentacle sim, background tick throttle, fluid and neighbor-update throttles, crosshair
raycast throttle, sound occlusion (hides audio cues), entity spawn / metadata / block-update /
chunk / particle packet batching or dropping, approximate entity interpolation, coarse positions,
fast math approximations (sqrt, atan2, floor, trig caches, normalize, sine table), cached collision
shapes, adaptive render distance (reloads chunks on the fly).

**Nothing to implement:**
- Chunk meshing, culling and terrain (rebuild throttles, empty sections, translucent sort,
  occlusion cache, backface culling, cave checks, water surface, grass tint and biome colour
  caches, leaf detail, ambient occlusion): Sodium replaces this code entirely.
- Lighting throttles and caches: handled safely by the ScalableLux mod.
- Memory pooling, interning and caches: FerriteCore and ModernFix; the JIT handles short-lived objects.
- Already vanilla: frustum culling, invisible-entity skipping, block outline range, damage tilt,
  biome blend, fast graphics, entity shadows, F1 HUD hiding, menu FPS, GUI batching, font caching,
  only-visible chat lines.
- Nothing in vanilla to skip: block-entity wobble, seagrass/kelp/leaf waving (shader packs only),
  screen fade, hotbar slide, GUI-preview particles, client statistics tick, beehive/furnace renderers.
- Whole replacement renderers, not toggles: billboard entities, box-only entity models, flat-colour
  blocks, wireframe world.
