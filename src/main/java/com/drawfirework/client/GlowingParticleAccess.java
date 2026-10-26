package com.drawfirework.client;

/**
 * Duck interface injected into {@link net.minecraft.client.particle.Particle} via Mixin.
 * Allows Draw &amp; Hide Firework to tag its own spawned particles for fullbright rendering
 * without affecting vanilla particles or altering the world lighting pipeline.
 */
public interface GlowingParticleAccess {
    void drawfirework$setFullbright(boolean fullbright);
    boolean drawfirework$isFullbright();
}
