package com.eyad.turbo.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * All Turbo settings (generated). Every @Tweak field is read live by a mixin, so changes
 * apply without a restart. Field defaults are the Minimal preset: they look and play
 * exactly like vanilla.
 *
 * Rule for every setting: it may change what is DRAWN or HEARD, never how the game plays
 * (ticks, physics, AI, input, targeting, packets).
 */
public class TurboConfig {

    /** Last preset applied from the menu. Changing any single setting does not change this. */
    public Preset preset = Preset.MINIMAL;


    // ===== General =====
    @Tweak(category="General", label="Master enable", tooltip="Turn every Turbo tweak on or off at once.", live=true)
    public boolean masterEnable = true;
    @Tweak(category="General", label="Log a summary at startup", live=true)
    public boolean logStartup = true;
    @Tweak(category="General", label="Show Turbo debug line", tooltip="Top-right corner: preset, FPS, live particles, and how many entities and block entities were skipped last frame, plus how many entities were hidden behind walls.", live=true)
    public boolean debugOverlay = false;

    // ===== Frame =====
    @Tweak(category="Frame", label="Skip world rendering while minimized", tooltip="Vanilla keeps rendering the world (at 10 FPS) while minimized even though nothing is shown. Crosshair targeting still updates, so AFK setups behave the same. Looks identical.", live=true)
    public boolean skipRenderWhenMinimized = true;
    @Tweak(category="Frame", label="Cap FPS when window is unfocused", tooltip="Vanilla already drops to 10 FPS when minimized or AFK. This also caps FPS while the window is visible but not focused (e.g. on a second monitor).", live=true)
    public boolean unfocusedFpsLimit = false;
    @Tweak(category="Frame", label="Unfocused FPS", live=true)
    public int unfocusedFps = 10;
    @Tweak(category="Frame", label="Cap FPS while a screen is open in-game", tooltip="Inventory, chat, pause and other screens while in a world.", live=true)
    public boolean menuFpsLimit = false;
    @Tweak(category="Frame", label="In-game screen FPS", live=true)
    public int menuFps = 30;
    @Tweak(category="Frame", label="No menu background blur", tooltip="Skips the blur pass behind menus (same as vanilla Menu Background Blur 0).", live=true)
    public boolean noPauseBlur = false;
    @Tweak(category="Frame", label="No title-screen panorama", tooltip="Title screen background is plain.", live=true)
    public boolean skipBackgroundPanorama = false;

    // ===== Entities =====
    @Tweak(category="Entities", label="Skip entities and block entities hidden by fog", tooltip="Underwater, in lava or powder snow, with Blindness or Darkness, and in the Nether, anything past the fog's end is drawn as pure fog colour, so it is skipped. Players, named, glowing and leashed entities and beacon beams are always drawn. Looks identical.", live=true)
    public boolean fogCulling = true;
    @Tweak(category="Entities", label="Hide entities behind walls", tooltip="Skips drawing entities that solid blocks completely hide from where you're looking. Checked with rays every frame, so nothing pops in late. Glass, leaves, slabs and other see-through or partial blocks never hide anything. Entities showing a nametag, glowing, leashed, beams and displays are always drawn.", live=true)
    public boolean occlusionCulling = false;
    @Tweak(category="Entities", label="Max entity render distance (0 = vanilla)", tooltip="You, your vehicle and your passengers are never hidden.", live=true)
    public int entityRenderDistance = 0;
    @Tweak(category="Entities", label="Limit nametag distance", tooltip="Vanilla draws nametags out to 64 blocks.", live=true)
    public boolean limitNametags = false;
    @Tweak(category="Entities", label="Nametag max distance (blocks)", live=true)
    public int nametagDistance = 24;
    @Tweak(category="Entities", label="Decor distance (blocks)", tooltip="Used by the 'beyond decor distance' toggles.", live=true)
    public int decorEntityDistance = 24;
    @Tweak(category="Entities", label="Hide item frames beyond decor distance", tooltip="Big win at spawns and shops full of map art.", live=true)
    public boolean cullItemFrameMaps = false;
    @Tweak(category="Entities", label="Hide paintings beyond decor distance", live=true)
    public boolean cheapPaintingRender = false;
    @Tweak(category="Entities", label="Hide armor stands beyond decor distance", live=true)
    public boolean cheapArmorStands = false;
    @Tweak(category="Entities", label="Hide dropped items beyond decor distance", tooltip="Items can still be picked up.", live=true)
    public boolean hideFarDroppedItems = false;
    @Tweak(category="Entities", label="Hide XP orbs beyond decor distance", tooltip="Orb movement and pickup are untouched.", live=true)
    public boolean disableXpOrbRender = false;
    @Tweak(category="Entities", label="Hide falling sand and gravel beyond decor distance", live=true)
    public boolean skipFallingBlockRender = false;
    @Tweak(category="Entities", label="Hide primed TNT beyond decor distance", live=true)
    public boolean cullTntFlashRender = false;
    @Tweak(category="Entities", label="Hide arrows, rockets and shulker bullets beyond decor distance", live=true)
    public boolean reduceProjectileParticles = false;
    @Tweak(category="Entities", label="Hide fishing hooks and lines beyond decor distance", live=true)
    public boolean disableFishingLine = false;
    @Tweak(category="Entities", label="Hide boats and minecarts beyond decor distance", live=true)
    public boolean cheapBoatPaddleAnim = false;
    @Tweak(category="Entities", label="Hide leash ropes", live=true)
    public boolean disableLeashRender = false;
    @Tweak(category="Entities", label="Hide lightning bolts", tooltip="The flash and thunder still happen.", live=true)
    public boolean skipLightningBoltDetail = false;
    @Tweak(category="Entities", label="Show one item for dropped stacks", tooltip="Vanilla draws up to 5 copies of an item for a stack on the ground.", live=true)
    public boolean flattenItemEntities = false;
    @Tweak(category="Entities", label="Dropped items don't bob or spin", live=true)
    public boolean cheapItemEntityBob = false;
    @Tweak(category="Entities", label="No enchant glint on dropped items", live=true)
    public boolean noDroppedItemGlint = false;
    @Tweak(category="Entities", label="No fire drawn on burning entities", tooltip="They still burn.", live=true)
    public boolean skipEntityFireRender = false;
    @Tweak(category="Entities", label="No guardian laser beams", tooltip="The attack still happens.", live=true)
    public boolean skipGuardianBeamRender = false;
    @Tweak(category="Entities", label="No wither armor glow", live=true)
    public boolean skipWitherShieldRender = false;
    @Tweak(category="Entities", label="No entity shadows", tooltip="Also skips the per-entity shadow block scan. Same look as vanilla Entity Shadows off.", live=true)
    public boolean disableShadowsEntirely = false;
    @Tweak(category="Entities", label="Hide player capes", live=true)
    public boolean skipThirdPersonCapeRender = false;
    @Tweak(category="Entities", label="Hide skin outer layers (hat, jacket, sleeves, pants)", live=true)
    public boolean skipPlayerModelLayers = false;
    @Tweak(category="Entities", label="Hide elytra wings", tooltip="Flying still works.", live=true)
    public boolean cheapElytraRender = false;
    @Tweak(category="Entities", label="Plain shields (no banner pattern)", live=true)
    public boolean skipShieldDecorationRender = false;
    @Tweak(category="Entities", label="No armor trims", live=true)
    public boolean skipArmorTrimRender = false;
    @Tweak(category="Entities", label="No enchant glint on worn armor", live=true)
    public boolean skipGlintOnArmor = false;

    // ===== Block Entities =====
    @Tweak(category="Block Entities", label="Hide ALL block entities beyond N blocks", tooltip="Chests, signs, banners, heads, beds etc. Vanilla draws them to 64 blocks.", live=true)
    public boolean limitBlockEntityRender = false;
    @Tweak(category="Block Entities", label="Block entity distance (blocks)", tooltip="Used by the toggle above and the per-type toggles below.", live=true)
    public int blockEntityDistance = 32;
    @Tweak(category="Block Entities", label="Hide chests, ender chests and shulker boxes beyond block entity distance", live=true)
    public boolean skipChestLidAnimFar = false;
    @Tweak(category="Block Entities", label="Hide signs beyond sign distance", tooltip="Includes hanging signs.", live=true)
    public boolean cheapSignTextRender = false;
    @Tweak(category="Block Entities", label="Sign distance (blocks)", live=true)
    public int signTextDistance = 24;
    @Tweak(category="Block Entities", label="Hide mob heads beyond block entity distance", live=true)
    public boolean skipSkullRotation = false;
    @Tweak(category="Block Entities", label="Hide banners beyond block entity distance", live=true)
    public boolean limitBannerLayers = false;
    @Tweak(category="Block Entities", label="Max banner pattern layers (0 = all)", tooltip="Banners draw only the first N pattern layers.", live=true)
    public int maxBannerLayers = 0;
    @Tweak(category="Block Entities", label="Hide bells beyond block entity distance", live=true)
    public boolean skipBellSwingAnim = false;
    @Tweak(category="Block Entities", label="Hide conduits beyond block entity distance", live=true)
    public boolean skipConduitAnim = false;
    @Tweak(category="Block Entities", label="Hide end gateway beams beyond block entity distance", live=true)
    public boolean cheapEndGatewayAnim = false;
    @Tweak(category="Block Entities", label="Hide enchanting table and lectern books", tooltip="The table and lectern blocks still show.", live=true)
    public boolean disableEnchantBook = false;
    @Tweak(category="Block Entities", label="Hide the spinning mob inside spawners", tooltip="The spawner cage still shows.", live=true)
    public boolean disableSpawnerRender = false;
    @Tweak(category="Block Entities", label="Hide all beacon beams", live=true)
    public boolean disableBeaconBeam = false;
    @Tweak(category="Block Entities", label="Hide beacon beams beyond beacon distance", live=true)
    public boolean cheapBeaconBeamFar = false;
    @Tweak(category="Block Entities", label="Beacon beam distance (blocks)", tooltip="Horizontal. Vanilla draws beams to 256.", live=true)
    public int beaconBeamDistance = 96;

    // ===== Particles =====
    @Tweak(category="Particles", label="Cap total live particles", tooltip="Exact count across all particle groups. Vanilla caps each group at 16384.", live=true)
    public boolean limitParticles = false;
    @Tweak(category="Particles", label="Max particles", live=true)
    public int maxParticles = 4000;
    @Tweak(category="Particles", label="Don't spawn particles beyond N blocks", tooltip="Vanilla already skips particles beyond 32 blocks, so only values below 32 help. Particles the game forces to show are exempt.", live=true)
    public boolean cullDistantParticles = false;
    @Tweak(category="Particles", label="Particle spawn distance (blocks)", live=true)
    public int particleCullDistance = 16;
    @Tweak(category="Particles", label="No block-break and mining particles", live=true)
    public boolean disableBlockBreakParticles = false;
    @Tweak(category="Particles", label="No rain splash particles", tooltip="Rain sounds are unaffected.", live=true)
    public boolean disableRainSplash = false;
    @Tweak(category="Particles", label="No explosion particles", tooltip="Explosion sounds are unaffected.", live=true)
    public boolean disableExplosionParticles = false;
    @Tweak(category="Particles", label="No potion effect swirls", tooltip="Includes lingering potion clouds.", live=true)
    public boolean disablePotionParticles = false;
    @Tweak(category="Particles", label="No potion swirls beyond decor distance", tooltip="Swirls near you still show.", live=true)
    public boolean limitMobParticleEffects = false;
    @Tweak(category="Particles", label="No crit and enchanted-hit particles", live=true)
    public boolean disableCritParticles = false;
    @Tweak(category="Particles", label="No bubble particles", live=true)
    public boolean disableBubbleParticles = false;
    @Tweak(category="Particles", label="No campfire smoke", live=true)
    public boolean cheapCampfireSmoke = false;
    @Tweak(category="Particles", label="No nether portal particles", live=true)
    public boolean reducePortalParticleRender = false;
    @Tweak(category="Particles", label="No firework spark trails", live=true)
    public boolean reduceFireworkParticleRender = false;
    @Tweak(category="Particles", label="No redstone dust particles", tooltip="From redstone wire, torches and ore.", live=true)
    public boolean skipRedstoneDustGlow = false;
    @Tweak(category="Particles", label="No end rod and dragon breath particles", live=true)
    public boolean disableEndParticles = false;
    @Tweak(category="Particles", label="No item pickup fly-in animation", tooltip="Items are still picked up.", live=true)
    public boolean disableItemPickupAnim = false;

    // ===== Render =====
    @Tweak(category="Render", label="No damage camera tilt", tooltip="Same as vanilla Damage Tilt 0%.", live=true)
    public boolean disableScreenShake = false;
    @Tweak(category="Render", label="No view bobbing", tooltip="Same as the vanilla View Bobbing option.", live=true)
    public boolean disableViewBob = false;
    @Tweak(category="Render", label="No portal and nausea screen overlay", live=true)
    public boolean disablePortalDistortion = false;
    @Tweak(category="Render", label="No carved pumpkin helmet overlay", live=true)
    public boolean disablePumpkinOverlay = false;
    @Tweak(category="Render", label="No powder snow frost overlay", live=true)
    public boolean disablePowderSnowOverlay = false;
    @Tweak(category="Render", label="No spyglass scope overlay", tooltip="You still zoom.", live=true)
    public boolean disableSpyglassOverlay = false;
    @Tweak(category="Render", label="No on-fire screen overlay", live=true)
    public boolean disableFireOverlay = false;
    @Tweak(category="Render", label="No underwater screen overlay", live=true)
    public boolean disableWaterOverlay = false;
    @Tweak(category="Render", label="No totem pop animation", tooltip="The totem still works.", live=true)
    public boolean disableTotemAnimation = false;
    @Tweak(category="Render", label="No block-breaking cracks", tooltip="Mining still works; you just don't see the progress cracks.", live=true)
    public boolean disableBreakOverlay = false;
    @Tweak(category="Render", label="No map markers", tooltip="Player and banner markers on maps are hidden.", live=true)
    public boolean skipMapDecorationRender = false;
    @Tweak(category="Render", label="No rain and snow rendering", live=true)
    public boolean disableWeatherRender = false;
    @Tweak(category="Render", label="No clouds", tooltip="Same as vanilla Clouds: Off.", live=true)
    public boolean disableClouds = false;
    @Tweak(category="Render", label="No stars", live=true)
    public boolean disableStars = false;
    @Tweak(category="Render", label="No End sky texture", live=true)
    public boolean disableEndSky = false;
    @Tweak(category="Render", label="Remove all fog", tooltip="Uses the game's own no-fog path. Visual only.", live=true)
    public boolean disableFog = false;
    @Tweak(category="Render", label="No underwater fog", live=true)
    public boolean cheapWaterFogRender = false;
    @Tweak(category="Render", label="No Nether fog", live=true)
    public boolean skipNetherFogDensity = false;
    @Tweak(category="Render", label="No End fog", live=true)
    public boolean skipEndFogRender = false;

    // ===== Animations =====
    @Tweak(category="Animations", label="Freeze animated textures", tooltip="Water, lava, fire, portals and other animated textures stop animating. Saves the 20/s atlas redraws.", live=true)
    public boolean disableTextureAnimations = false;
    @Tweak(category="Animations", label="Water animates at half speed", live=true)
    public boolean reduceWaterAnimFps = false;
    @Tweak(category="Animations", label="Lava animates at half speed", live=true)
    public boolean reduceLavaAnimFps = false;
    @Tweak(category="Animations", label="Fire animates at half speed", live=true)
    public boolean reduceFireAnimFps = false;
    @Tweak(category="Animations", label="Chest lids don't animate", tooltip="Chests still open.", live=true)
    public boolean disableChestAnim = false;
    @Tweak(category="Animations", label="Banners don't wave", live=true)
    public boolean skipBannerWave = false;
    @Tweak(category="Animations", label="Capes hang still", live=true)
    public boolean disableCapeFlutter = false;

    // ===== Sound =====
    @Tweak(category="Sound", label="Skip sounds too far away to hear", tooltip="Vanilla still starts sounds that are out of hearing range. These are skipped. Looping, moving and long sounds are never skipped. Sounds identical.", live=true)
    public boolean skipInaudibleSounds = true;
    @Tweak(category="Sound", label="Don't play positional sounds beyond N blocks", tooltip="Music, UI and other non-positional sounds are never affected.", live=true)
    public boolean cullDistantSounds = false;
    @Tweak(category="Sound", label="Sound distance (blocks)", live=true)
    public int soundCullDistance = 32;
    @Tweak(category="Sound", label="Cap how many sounds play at once", tooltip="Vanilla allows up to 247.", live=true)
    public boolean capConcurrentSounds = false;
    @Tweak(category="Sound", label="Max sounds at once", live=true)
    public int maxConcurrentSounds = 64;
    @Tweak(category="Sound", label="Merge identical sounds in the same tick", tooltip="The same sound at the same block in the same tick plays once instead of stacking.", live=true)
    public boolean dedupeSimultaneousSounds = false;

    // ===== HUD =====
    @Tweak(category="HUD", label="No vignette", tooltip="Same as the vanilla Vignette option.", live=true)
    public boolean skipVignetteRender = false;
    @Tweak(category="HUD", label="Hide scoreboard sidebar", live=true)
    public boolean disableScoreboard = false;
    @Tweak(category="HUD", label="Hide toast popups", tooltip="Advancements and recipes are still unlocked.", live=true)
    public boolean disableToasts = false;
    @Tweak(category="HUD", label="Hide mount health when it's full", live=true)
    public boolean skipMountHealthRender = false;

    // ===== World =====
    @Tweak(category="World", label="Smart leaves: cull hidden inner leaf faces", tooltip="Vanilla only does this when leaves are opaque. This also does it with see-through leaves: the outside looks the same, the hidden inside is not drawn.", live=true)
    public boolean skipDistantLeafRender = false;

    // ===== Aggressive =====
    @Tweak(category="Aggressive", label="Disable ALL particles", live=true)
    public boolean noParticlesAtAll = false;
    @Tweak(category="Aggressive", label="Disable clouds, weather and stars", live=true)
    public boolean noAtmosphere = false;
    @Tweak(category="Aggressive", label="Disable ALL screen overlays", tooltip="Fire, water, in-wall, pumpkin, powder snow, spyglass, portal, vignette.", live=true)
    public boolean noOverlays = false;
    @Tweak(category="Aggressive", label="Hide ALL block entities", tooltip="Chests, signs, banners etc. disappear entirely.", live=true)
    public boolean noBlockEntities = false;
    @Tweak(category="Aggressive", label="Mute ALL sounds", live=true)
    public boolean disableAllSounds = false;

    // ==================================================================
    // Persistence
    // ==================================================================
    private static TurboConfig INSTANCE;
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    public static TurboConfig get() {
        if (INSTANCE == null) INSTANCE = load();
        return INSTANCE;
    }

    /** Master gate every mixin checks first. */
    public static boolean on() {
        return get().masterEnable;
    }

    private static Path path() {
        return FabricLoader.getInstance().getConfigDir().resolve("turbo.json");
    }

    private static TurboConfig load() {
        Path p = path();
        try {
            if (Files.exists(p)) {
                TurboConfig c = GSON.fromJson(Files.readString(p), TurboConfig.class);
                if (c != null) {
                    if (c.preset == null) c.preset = Preset.CUSTOM;
                    c.save();
                    return c;
                }
            }
        } catch (IOException | RuntimeException e) {
            System.err.println("[Turbo] config read failed, using defaults: " + e.getMessage());
        }
        TurboConfig fresh = new TurboConfig();
        fresh.save();
        return fresh;
    }

    public void save() {
        try {
            Files.createDirectories(path().getParent());
            Files.writeString(path(), GSON.toJson(this));
        } catch (IOException e) {
            System.err.println("[Turbo] config write failed: " + e.getMessage());
        }
    }
}
