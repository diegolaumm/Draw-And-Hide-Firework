package com.drawfirework.network;

import com.drawfirework.component.ModDataComponents;
import com.drawfirework.item.ModItems;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import java.util.UUID;
import java.util.WeakHashMap;

public final class ServerPlayback {
    private static final WeakHashMap<ServerPlayer,UseGate> GATES = new WeakHashMap<>();
    private ServerPlayback() {}
    public static void initialize() { PayloadTypeRegistry.clientboundPlay().register(PlaybackPayload.TYPE,PlaybackPayload.CODEC); }
    public static boolean use(ServerPlayer player, InteractionHand hand) {
        var stack=player.getItemInHand(hand);
        if (!player.isAlive() || !ServerPlayNetworking.canSend(player,PlaybackPayload.TYPE)) return false;
        var center=player.getEyePosition().add(player.getLookAngle().scale(12));
        var canvas=stack.get(ModDataComponents.CANVAS);
        PlaybackPayload event;
        try {
            event = new PlaybackPayload(UUID.randomUUID(),player.level().dimension().identifier().toString(),
                    center.x,center.y,center.z,canvas);
        } catch (IllegalArgumentException e) { return false; }
        return GATES.computeIfAbsent(player,p -> new UseGate()).send(player.tickCount,player.isCreative(),
                stack.is(ModItems.TEST_FIREWORK),canvas, () -> ServerPlayNetworking.send(player,
                        event));
    }
}
