package com.drawfirework.client.mixin;

import com.drawfirework.client.DustLightingMode;
import com.drawfirework.client.GlowingParticleAccess;
import net.minecraft.client.particle.Particle;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Mixin into {@link Particle} to allow selective fullbright rendering for mod-spawned particles.
 * Default is {@code false}, ensuring 100% of vanilla and third-party particles are unaffected.
 */
@Mixin(Particle.class)
public abstract class ParticleMixin implements GlowingParticleAccess {
    @Unique
    private boolean drawfirework$fullbright;

    @Override
    public void drawfirework$setFullbright(boolean fullbright) {
        this.drawfirework$fullbright = fullbright;
    }

    @Override
    public boolean drawfirework$isFullbright() {
        return this.drawfirework$fullbright;
    }

    @Inject(method = "getLightCoords", at = @At("HEAD"), cancellable = true)
    private void drawfirework$overrideLightCoords(float partialTick, CallbackInfoReturnable<Integer> cir) {
        if (this.drawfirework$fullbright) {
            cir.setReturnValue(DustLightingMode.FULL_BRIGHT_COORDS);
        }
    }
}
