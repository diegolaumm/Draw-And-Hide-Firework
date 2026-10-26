package com.drawfirework.client;

import com.drawfirework.canvas.CanvasData;
import com.drawfirework.canvas.ParticleQuality;
import com.drawfirework.canvas.SamplingStrategy;
import com.drawfirework.firework.DevCanvases;
import com.drawfirework.firework.DustPattern;
import com.mojang.blaze3d.vertex.PoseStack;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.rendering.v1.level.LevelRenderEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.network.chat.Component;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;

import java.util.ArrayList;
import java.util.List;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import static com.drawfirework.client.FireworkTimeline.*;

/** Bounded local Dust lifecycle; image remains the Phase 01 technical comparison. */
@Environment(EnvType.CLIENT)
public final class HeartPrototype implements ClientModInitializer {
    // Phase 04A: the fixed heart now lives in DustPattern.HEART (identical points/colours).
    private final List<DustPoint> dust = new ArrayList<>();
    private final List<Particle> trail = new ArrayList<>();
    private final List<Vec3> velocities = new ArrayList<>();
    private DustPlane plane;
    private java.util.UUID lastEvent;
    private ClientLevel owner;
    private Vec3 center;
    private int age;
    private boolean image;
    private DustStyle style = DustStyle.BLOOM_130;
    private FireworkTimeline timeline = style.timeline();
    private DustPattern pattern = DustPattern.HEART;
    private DustLightingMode lightingMode = DustLightingMode.DEFAULT;
    private ParticleQuality quality = ParticleQuality.DEFAULT;
    private SamplingStrategy samplingStrategy = SamplingStrategy.DEFAULT;
    private static ParticleQuality activeQuality = ParticleQuality.DEFAULT;
    private static SamplingStrategy activeSamplingStrategy = SamplingStrategy.DEFAULT;
    // Phase 04A.1 / 04A.2 diagnostics (CPU timings only).
    private String metricLabel;
    private long prepareNanos, spawnNanos, holdNanosSum, holdNanosMax;
    private int holdTicks;

    private static final class DustPoint {
        final Particle particle;
        final DustStyle.Sample sample;
        float size = 1;
        float releaseSize = 1;

        DustPoint(Particle particle, DustStyle.Sample sample) {
            this.particle = particle;
            this.sample = sample;
        }
    }

    @Override
    public void onInitializeClient() {
        net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking.registerGlobalReceiver(
                com.drawfirework.network.PlaybackPayload.TYPE, (event, context) -> {
                    var expectedWorld = context.player().level();
                    context.client().execute(() -> {
                        var client = context.client();
                        if (client.level == null || client.level != expectedWorld || client.player != context.player()
                                || !client.hasSingleplayerServer()
                                || !client.level.dimension().identifier().toString().equals(event.dimension())
                                || event.id().equals(lastEvent)) return;
                        lastEvent = event.id();
                        var core = canvasPattern(event.canvas(), CanvasOptions.DEFAULT);
                        activate(client.level, false, DustStyle.BLOOM_130, core,
                                DustPlane.atCenter(CanvasOptions.DEFAULT,
                                        new org.joml.Vector3d(event.x(),event.y(),event.z()), DustStyle.BLOOM_130.timeline()));
                    });
                });
        net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents.DISCONNECT.register((handler,client) -> {
            clear(); lastEvent = null;
        });
        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registry) ->
                dispatcher.register(literal("drawfirework")
                        .then(literal("heart").executes(c -> start(c.getSource(), false))
                                .then(literal("image").executes(c -> start(c.getSource(), true)))
                                .then(literal("dust").executes(c -> start(c.getSource(), false))
                                        .then(literal("normal").executes(c -> start(c.getSource(), false, DustStyle.SOFT)))
                                        .then(literal("bloom115").executes(c -> start(c.getSource(), false, DustStyle.BLOOM_115)))
                                        .then(literal("bloom130").executes(c -> start(c.getSource(), false, DustStyle.BLOOM_130)))
                                        .then(literal("legacy").executes(c -> start(c.getSource(), false, DustStyle.LEGACY)))))
                        .then(canvasCommand())
                        .then(lightingCommand())
                        .then(qualityCommand())
                        .then(samplingCommand())
                        .then(literal("clear").executes(c -> { clear(); return 1; }))));
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (owner != null && (owner != client.level || client.player == null)) { clear(); lastEvent = null; }
        });
        ClientTickEvents.END_LEVEL_TICK.register(level -> {
            if (owner != level) return;
            if (level != Minecraft.getInstance().level) { clear(); return; }
            if (++age >= (image ? 36 : timeline.end())) { clear(); return; }
            if (!image) tickDust();
        });
        LevelRenderEvents.COLLECT_SUBMITS.register(context -> {
            if (owner == null || !image) return;
            var camera = context.levelState().cameraRenderState;
            float spread = Math.min(1, (age + context.levelState().worldPartialTicks) / 8f);
            // Capture immutable values for the deferred geometry callback.
            PoseStack pose = new PoseStack();
            pose.translate(center.x - camera.pos.x, center.y - camera.pos.y, center.z - camera.pos.z);
            pose.rotate(camera.orientation);
            context.submitNodeCollector().submitCustomGeometry(pose, RenderTypes.debugQuads(), (entry, vertices) -> {
                for (DustPattern.Point point : DustPattern.HEART.points()) {
                    float x = point.x() * spread;
                    float y = point.y() * spread;
                    float half = .21f * spread;
                    int color = 0xff000000 | point.rgb();
                    vertices.addVertex(entry, x - half, y - half, 0).setColor(color);
                    vertices.addVertex(entry, x + half, y - half, 0).setColor(color);
                    vertices.addVertex(entry, x + half, y + half, 0).setColor(color);
                    vertices.addVertex(entry, x - half, y + half, 0).setColor(color);
                }
            });
        });
    }

    private int start(FabricClientCommandSource source, boolean useImage) {
        return start(source, useImage, DustStyle.BLOOM_130);
    }

    private int start(FabricClientCommandSource source, boolean useImage, DustStyle preset) {
        return start(source, useImage, preset, DustPattern.HEART, null, null);
    }

    private static final java.util.Map<String, java.util.function.Supplier<CanvasData>> CANVASES = java.util.Map.of(
            "smiley", DevCanvases::smiley, "dense", DevCanvases::dense,
            "grid", DevCanvases::grid, "rect", DevCanvases::rectangle,
            "smiley_warm", () -> DevCanvases.smiley(DevCanvases.SmileyPalette.WARM),
            "smiley_cool", () -> DevCanvases.smiley(DevCanvases.SmileyPalette.COOL));

    /**
     * /drawfirework canvas &lt;smiley|smiley_warm|smiley_cool|dense|grid|rect&gt; [100|125|150] [128|192|266] [plain] [dynamic|fixed]
     * Each option is optional but must keep that order. Default orientation is Fixed World Plane.
     */
    private LiteralArgumentBuilder<FabricClientCommandSource> canvasCommand() {
        var root = literal("canvas");
        for (var entry : CANVASES.entrySet()) {
            root.then(canvasNode(entry.getKey(), entry.getKey(), entry.getValue(), CanvasOptions.DEFAULT, 0));
        }
        return root;
    }

    private LiteralArgumentBuilder<FabricClientCommandSource> canvasNode(
            String text, String name, java.util.function.Supplier<CanvasData> canvas, CanvasOptions o, int stage) {
        var node = literal(text).executes(c -> startCanvas(c.getSource(), name, canvas.get(), o));
        if (stage <= 0) for (int s : CanvasOptions.SCALES)
            node.then(canvasNode(Integer.toString(s), name, canvas, new CanvasOptions(s, o.budget(), o.plain(), o.fixed()), 1));
        if (stage <= 1) for (int b : CanvasOptions.BUDGETS)
            node.then(canvasNode(Integer.toString(b), name, canvas, new CanvasOptions(o.scalePercent(), b, o.plain(), o.fixed()), 2));
        if (stage <= 2)
            node.then(canvasNode("plain", name, canvas, new CanvasOptions(o.scalePercent(), o.budget(), true, o.fixed()), 3));
        if (stage <= 3) {
            node.then(canvasNode("dynamic", name, canvas, new CanvasOptions(o.scalePercent(), o.budget(), o.plain(), false), 4));
            node.then(canvasNode("fixed", name, canvas, new CanvasOptions(o.scalePercent(), o.budget(), o.plain(), true), 4));
        }
        return node;
    }

    /**
     * Phase 04A development path: fixed CanvasData through CanvasDownsampler into the accepted Bloom 130 lifecycle.
     * Options only change layout scale (about the fixed centre), sample count (always &lt;= 266 x 3 = 798 particles)
     * and, for "plain", remove the 3-particle micro offset; Bloom 130%, hold 14 and timeline are untouched.
     * Canvas patterns default to Fixed World Plane ("fixed"); "dynamic" remains an explicit development comparison mode.
     */
    private int startCanvas(FabricClientCommandSource source, String name, CanvasData canvas, CanvasOptions o) {
        DustStyle preset = o.plain() ? DustStyle.BLOOM_130_PLAIN : DustStyle.BLOOM_130;
        long t0 = System.nanoTime();
        DustPattern core = canvasPattern(canvas, o);
        long prepare = System.nanoTime() - t0;
        int displayBudget = (o != null && o.hasExplicitBudget())
                ? Math.min(o.budget(), activeQuality.sampleLimit(preset.particlesPerPoint()))
                : activeQuality.sampleLimit(preset.particlesPerPoint());
        String label = name + " scale" + o.scalePercent() + "% budget" + displayBudget + (o.plain() ? " plain" : "") + (o.fixed() ? " fixed" : " dynamic");
        int result = start(source, false, preset, core, label, o);
        if (result == 1) { metricLabel = label; prepareNanos = prepare; }
        return result;
    }

    private int start(FabricClientCommandSource source, boolean useImage, DustStyle preset,
                      DustPattern core, String canvasName, CanvasOptions options) {
        Minecraft client = source.getClient();
        if (!client.hasSingleplayerServer()) {
            source.sendError(Component.literal("Prototype: single-player only."));
            return 0;
        }
        int coreParticles;
        try {
            coreParticles = core.coreParticles(preset.particlesPerPoint(), quality.coreBudget());
        } catch (IllegalArgumentException e) {
            source.sendError(Component.literal("Core particle budget exceeded (max " + quality.coreBudget() + ")."));
            return 0;
        }
        var eye = source.getPlayer().getEyePosition();
        var look = source.getPlayer().getLookAngle();
        activate(source.getLevel(), useImage, preset, core, DustPlane.start(options,
                new org.joml.Vector3d(eye.x, eye.y, eye.z), new org.joml.Vector3d(look.x, look.y, look.z), preset.timeline()));
        if (canvasName != null) {
            String feedback = "Canvas " + canvasName + ": " + core.points().size()
                    + " samples x " + style.particlesPerPoint() + " = " + coreParticles
                    + " core particles (max " + quality.coreBudget() + ") | Bloom 130% | "
                    + (options != null && options.fixed() ? "fixed orientation" : "dynamic orientation")
                    + (lightingMode != DustLightingMode.BASELINE ? " | lighting: " + lightingMode.commandName() : "")
                    + (quality != ParticleQuality.DEFAULT ? " | quality: " + quality.commandName() : "")
                    + (samplingStrategy != SamplingStrategy.DEFAULT ? " | sampling: " + samplingStrategy.commandName() : "")
                    + " | clear cancels";
            source.sendFeedback(Component.literal(feedback));
            return 1;
        }
        source.sendFeedback(Component.literal(image ? "Image comparison: 8 formation + 28 hold ticks"
                : "Dust " + (style.equals(DustStyle.LEGACY) ? "legacy" : Math.round(style.bloomTarget() * 100) + "%")
                + ": A20 / B8 / C" + style.holdTicks() + " / D20 / E14 | "
                + style.particlesPerPoint() + " particles/point"
                + (lightingMode != DustLightingMode.BASELINE ? " | lighting: " + lightingMode.commandName() : "")
                + (quality != ParticleQuality.DEFAULT ? " | quality: " + quality.commandName() : "")
                + (samplingStrategy != SamplingStrategy.DEFAULT ? " | sampling: " + samplingStrategy.commandName() : "")
                + " | clear cancels"));
        return 1;
    }

    static DustPattern canvasPattern(CanvasData canvas, CanvasOptions options) {
        var preset = (options != null && options.plain()) ? DustStyle.BLOOM_130_PLAIN : DustStyle.BLOOM_130;
        int maxBudget = activeQuality.sampleLimit(preset.particlesPerPoint());
        int budget;
        if (options == null || options == CanvasOptions.DEFAULT || !options.hasExplicitBudget()) {
            budget = maxBudget;
        } else {
            // Explicitly specified budget: respect caller's explicit value, capped by core budget limit
            budget = Math.min(options.budget(), maxBudget);
        }
        return DustPattern.fromCanvas(canvas, preset.particlesPerPoint(), budget, activeQuality.coreBudget(), activeSamplingStrategy)
                .scaled(options != null ? options.scalePercent() / 100f : 1f);
    }

    private void activate(ClientLevel level, boolean useImage, DustStyle preset, DustPattern core, DustPlane newPlane) {
        core.coreParticles(preset.particlesPerPoint(), quality.coreBudget());
        clear();
        owner=level; image=useImage; style=preset; timeline=style.timeline(); pattern=core; plane=newPlane;
        var anchor=plane.center(); center=new Vec3(anchor.x,anchor.y,anchor.z);
        if (!image) sound(SoundEvents.FIREWORK_ROCKET_LAUNCH,center.add(0,-6,0));
    }

    private void tickDust() {
        Minecraft client = Minecraft.getInstance();
        var camera = client.gameRenderer == null ? null : client.gameRenderer.mainCamera();
        if (camera == null) { clear(); return; }
        trail.removeIf(p -> !p.isAlive());
        if (age < LAUNCH_END) {
            Vec3 pos = center.add(0, -6 + 6 * age / (double) LAUNCH_END, 0);
            Particle p = client.particleEngine.createParticle(new DustParticleOptions(0xffcc75, .8f),
                    pos.x, pos.y, pos.z, 0, 0, 0);
            if (p != null) {
                p.setLifetime(10);
                trail.add(p);
                applyLighting(p, 0xffcc75);
            }
            return;
        }
        boolean positioning = plane.update(age, camera.rotation());
        if (age == LAUNCH_END) {
            sound(SoundEvents.FIREWORK_ROCKET_BLAST, center);
            long spawnStart = System.nanoTime();
            List<DustPattern.Point> points = pattern.points();
            for (int point = 0; point < points.size(); point++) {
                DustPattern.Point source = points.get(point);
                for (int copy = 0; copy < style.particlesPerPoint(); copy++) {
                    Particle p = client.particleEngine.createParticle(
                            new DustParticleOptions(source.rgb(), 1.2f * source.sizeScale()),
                            center.x, center.y, center.z, 0, 0, 0);
                    if (p != null) {
                        p.setLifetime(timeline.end() - LAUNCH_END + 10);
                        applyLighting(p, source.rgb());
                    }
                    dust.add(new DustPoint(p, style.sample(point, copy, source.x(), source.y())));
                }
            }
            spawnNanos = System.nanoTime() - spawnStart;
        }
        if (positioning) {
            long holdStart = System.nanoTime();
            positionDust();
            long holdTick = System.nanoTime() - holdStart;
            holdNanosSum += holdTick; holdNanosMax = Math.max(holdNanosMax, holdTick); holdTicks++;
            return;
        }
        if (age == timeline.holdEnd()) {
            // Use the last hold orientation; never reposition during dispersion/fade.
            for (DustPoint point : dust) {
                var s = point.sample;
                Vector3f v = plane.rotate(s.x() * .02f, s.y() * .02f, s.z() * .02f);
                velocities.add(new Vec3(v.x, v.y + .012, v.z));
                point.releaseSize = point.size;
                // Preserve Phase 02.5 normal/legacy; bloom never snaps back to 100%.
                if (style.bloomTarget() == 1) {
                    if (point.particle != null) point.particle.scale(1 / point.size);
                    point.size = 1;
                }
            }
        }
        int elapsed = age - timeline.holdEnd();
        double drag = Math.pow(.96, elapsed);
        for (int i = 0; i < dust.size(); i++) {
            Particle p = dust.get(i).particle;
            if (p == null || !p.isAlive()) continue;
            if (style.bloomTarget() > 1 && age < timeline.fadeStart()) {
                DustPoint point = dust.get(i);
                float blend = DustStyle.smoothStep(elapsed / (float) DustStyle.BLOOM_TICKS);
                float size = point.releaseSize + (style.bloomTarget() - point.releaseSize) * blend;
                p.scale(size / point.size);
                point.size = size;
            }
            Vec3 v = velocities.get(i);
            p.setParticleSpeed(v.x * drag, v.y * drag - .0015 * elapsed, v.z * drag);
            // Vanilla Dust is OPAQUE: progressively shrink the soft sprite, not fake alpha.
            if (age >= timeline.fadeStart()) p.scale(timeline.fadeScaleAt(age));
        }
    }

    private void sound(net.minecraft.sounds.SoundEvent sound, Vec3 pos) {
        owner.playLocalSound(pos.x, pos.y, pos.z, sound, SoundSource.PLAYERS, .7f, 1, false);
    }

    private void positionDust() {
        float spread = FireworkTimeline.spread(age);
        for (DustPoint point : dust) {
            Particle particle = point.particle;
            if (particle == null || !particle.isAlive()) continue;
            var s = point.sample;
            var position = plane.position(s.x(), s.y(), s.z(), spread);
            float size = style.particleSize(age, s.phase());
            particle.scale(size / point.size);
            point.size = size;
            particle.setParticleSpeed(0, 0, 0);
            particle.setPos(position.x, position.y, position.z);
        }
    }

    private void clear() {
        if (spawnNanos > 0) {
            // CPU-side mod work only (not frame time / FPS). prepare = downsample at command time,
            // spawn = createParticle burst at explosion tick, hold = positionDust per hold tick.
            com.drawfirework.DrawFirework.LOGGER.info(
                    "[04A.1 timing] {} | prepare {} ms | spawn {} particles {} ms | hold {} ticks avg {} ms max {} ms",
                    metricLabel == null ? "heart" : metricLabel, prepareNanos / 1e6, dust.size(), spawnNanos / 1e6,
                    holdTicks, holdTicks == 0 ? 0 : holdNanosSum / 1e6 / holdTicks, holdNanosMax / 1e6);
        }
        spawnNanos = prepareNanos = holdNanosSum = holdNanosMax = 0;
        holdTicks = 0;
        metricLabel = null;
        for (DustPoint point : dust) if (point.particle != null) point.particle.remove();
        dust.clear();
        for (Particle particle : trail) particle.remove();
        trail.clear();
        velocities.clear();
        plane = null;
        owner = null;
        center = null;
        age = 0;
        image = false;
        pattern = DustPattern.HEART;
    }

    private void applyLighting(Particle p, int rgb) {
        if (p == null) return;
        if (lightingMode.isFullbright() && p instanceof GlowingParticleAccess glowing) {
            glowing.drawfirework$setFullbright(true);
        }
        if (lightingMode.isExactRgb() && p instanceof net.minecraft.client.particle.SingleQuadParticle sqp) {
            sqp.setColor(DustLightingMode.red(rgb), DustLightingMode.green(rgb), DustLightingMode.blue(rgb));
        }
    }

    private LiteralArgumentBuilder<FabricClientCommandSource> lightingCommand() {
        var root = literal("lighting").executes(c -> {
            c.getSource().sendFeedback(Component.literal("Current lighting mode: "
                    + lightingMode.commandName() + " (" + lightingMode.description() + ")"));
            return 1;
        });
        for (DustLightingMode mode : DustLightingMode.values()) {
            root.then(literal(mode.commandName()).executes(c -> {
                this.lightingMode = mode;
                c.getSource().sendFeedback(Component.literal("Lighting mode set to: "
                        + mode.commandName() + " (" + mode.description() + ")"));
                return 1;
            }));
        }
        root.then(literal("reset").executes(c -> {
            resetLightingMode();
            c.getSource().sendFeedback(Component.literal("Lighting mode reset to " + lightingMode.commandName() + "."));
            return 1;
        }));
        return root;
    }

    public DustLightingMode lightingMode() {
        return lightingMode;
    }

    public void setLightingMode(DustLightingMode mode) {
        this.lightingMode = mode == null ? DustLightingMode.DEFAULT : mode;
    }

    public void resetLightingMode() {
        setLightingMode(DustLightingMode.DEFAULT);
    }

    private LiteralArgumentBuilder<FabricClientCommandSource> qualityCommand() {
        var root = literal("quality").executes(c -> {
            c.getSource().sendFeedback(Component.literal("Current particle quality: "
                    + quality.commandName() + " (" + quality.description() + ")"));
            return 1;
        });
        for (ParticleQuality q : ParticleQuality.values()) {
            root.then(literal(q.commandName()).executes(c -> {
                setQuality(q);
                c.getSource().sendFeedback(Component.literal("Particle quality set to: "
                        + q.commandName() + " (" + q.description() + ")"));
                return 1;
            }));
        }
        root.then(literal("reset").executes(c -> {
            resetQuality();
            c.getSource().sendFeedback(Component.literal("Particle quality reset to " + quality.commandName() + "."));
            return 1;
        }));
        return root;
    }

    public static ParticleQuality activeQuality() {
        return activeQuality;
    }

    public static void setActiveQuality(ParticleQuality q) {
        activeQuality = q == null ? ParticleQuality.DEFAULT : q;
    }

    public ParticleQuality quality() {
        return quality;
    }

    public void setQuality(ParticleQuality q) {
        this.quality = q == null ? ParticleQuality.DEFAULT : q;
        setActiveQuality(this.quality);
    }

    public void resetQuality() {
        setQuality(ParticleQuality.DEFAULT);
    }

    private LiteralArgumentBuilder<FabricClientCommandSource> samplingCommand() {
        var root = literal("sampling").executes(c -> {
            c.getSource().sendFeedback(Component.literal("Current sampling strategy: "
                    + samplingStrategy.commandName() + " (" + samplingStrategy.description() + ")"));
            return 1;
        });
        for (SamplingStrategy s : SamplingStrategy.values()) {
            root.then(literal(s.commandName()).executes(c -> {
                setSamplingStrategy(s);
                c.getSource().sendFeedback(Component.literal("Sampling strategy set to: "
                        + s.commandName() + " (" + s.description() + ")"));
                return 1;
            }));
        }
        root.then(literal("reset").executes(c -> {
            resetSamplingStrategy();
            c.getSource().sendFeedback(Component.literal("Sampling strategy reset to " + samplingStrategy.commandName() + "."));
            return 1;
        }));
        return root;
    }

    public static SamplingStrategy activeSamplingStrategy() {
        return activeSamplingStrategy;
    }

    public static void setActiveSamplingStrategy(SamplingStrategy s) {
        activeSamplingStrategy = s == null ? SamplingStrategy.DEFAULT : s;
    }

    public SamplingStrategy samplingStrategy() {
        return samplingStrategy;
    }

    public void setSamplingStrategy(SamplingStrategy s) {
        this.samplingStrategy = s == null ? SamplingStrategy.DEFAULT : s;
        setActiveSamplingStrategy(this.samplingStrategy);
    }

    public void resetSamplingStrategy() {
        setSamplingStrategy(SamplingStrategy.DEFAULT);
    }

    private static LiteralArgumentBuilder<FabricClientCommandSource> literal(String name) {
        return LiteralArgumentBuilder.literal(name);
    }
}
