# Verification

## How Turbo is checked
- Built against the real Minecraft 1.21.11 jar with Mojang mappings; every hook was written
  after reading the decompiled method (`./gradlew genSources`).
- `defaultRequire: 1` in `turbo.mixins.json`: a hook that misses its target fails
  loudly instead of silently doing nothing. All 32 mixins pass a forced Mixin audit
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

## Hiding entities and block entities behind walls (every preset, Minimal included)
How it decides: after vanilla's own checks say an entity would be drawn, Turbo casts rays from the
camera to the centre of the entity's culling box and to points at most 1 block apart on every face
of that box facing the camera. If every ray hits an opaque block first, the entity is skipped for
that frame. The box is the renderer's own culling box (the one vanilla's frustum test uses), padded
by the same 0.5 blocks vanilla pads it by, and widened to cover the entity's shadow.

Block entities (chests, signs, banners, heads, beds, books, spawners...) get the same test against
their block padded by 1 block on every side and 2 upward, so standing and wall banners, open
shulker lids, floating books and items stay inside the box. Block entities that draw outside their
block (beacon and end gateway beams, structure blocks) are never tested.

Kept on the safe side:
- Only full, opaque, drawn cubes block a ray. Glass, leaves, slabs, stairs, fences, barriers and
  other see-through or partial blocks never hide anything.
- A block only counts once its chunk section is built and shown (vanilla or Sodium), so nothing
  vanishes behind terrain that hasn't loaded in yet.
- Re-checked every frame: an entity that comes into view is drawn that same frame. "Visible" is
  remembered for 3 frames, "hidden" never.
- Always drawn: anything showing a nametag (nametags show through walls), glowing entities
  (outlines show through walls), leashed mobs, guardians and end crystals (beams), shulkers,
  display entities, fishing hooks, lightning, the ender dragon, you, your vehicle and passengers.
- Off when the camera is inside an opaque block (spectator).

Limit: rays are spaced up to 1 block apart, so an entity seen only through a sliver narrower than
that (for example through two offset windows lined up at a steep angle) can be skipped. It is on in
every preset, Minimal included; if you ever spot a missing entity, turn it off in the Entities tab.

Tested (1.1.0): a wall with a 1x1 window, a glass section, and husks, an armor stand, a named husk, a glowing
husk and an item frame on both sides. Screenshots with the setting off and on show the same
entities (differences are only arm-swing timing and clouds). With 78 husks behind the wall, 65 were
skipped. With the spectator camera inside the wall, none were.

## Other Minimal optimizations, and their limits
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
- Already vanilla: frustum culling, invisible-entity skipping, per-entity culling of passengers,
  block outline range, damage tilt, biome blend, fast graphics, entity shadows, F1 HUD hiding,
  menu FPS, GUI batching, font caching, only-visible chat lines, squared-distance checks in hot
  paths, inventory items drawn once into an atlas and reused, block-entity and TNT draw batching
  (the submit system groups every draw by render type).
- Nothing in vanilla to skip: block-entity wobble, seagrass/kelp/leaf waving (shader packs only),
  screen fade, hotbar slide, GUI-preview particles, client statistics tick, beehive/furnace renderers,
  shulker tooltip previews and held-item dynamic light (mods add these, vanilla doesn't).
- Nothing measurable to win: detailed map rendering (a map is one textured quad; markers and far
  item frames have their own settings), baby-mob scale, first-person hand, boss bar, recipe book,
  F3 charts (only drawn while open), tab list (only drawn while Tab is held, and its data comes
  from the server), advancement toast queue (the Toasts setting hides them), rain and thunder
  sound updates, the ender dragon at a distance (one entity, only in the End fight).
- Whole replacement renderers, not toggles: billboard entities, box-only entity models, flat-colour
  blocks, wireframe world.
