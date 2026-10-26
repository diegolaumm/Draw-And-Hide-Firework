package com.drawfirework.network;

import com.drawfirework.canvas.CanvasData;
import com.drawfirework.canvas.ItemCanvasCodecs;
import com.drawfirework.component.ModDataComponents;
import com.drawfirework.editor.EditSession;
import com.drawfirework.editor.EditorRequestGate;
import com.drawfirework.item.ModItems;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import java.util.UUID;
import java.util.WeakHashMap;

/** All state access/mutation happens on the server thread. No renderer references. */
public final class ServerCanvasEditor {
    private static final WeakHashMap<ServerPlayer, State> STATES = new WeakHashMap<>();
    private static final class State {
        final EditorRequestGate requests = new EditorRequestGate();
        EditSession session;
        ItemStack snapshot;
        Object level;
        boolean current(ServerPlayer p) {
            return session != null && level == p.level().dimension() && session.current(p.tickCount, eligible(p),
                    p.getMainHandItem(), p.getInventory().getSelectedSlot(), p.getMainHandItem().get(ModDataComponents.CANVAS),
                    sameStack(p.getMainHandItem(), snapshot));
        }
        void clear() { session = null; snapshot = null; level = null; }
    }
    private ServerCanvasEditor() {}
    private static boolean eligible(ServerPlayer p) {
        return p.isAlive() && !p.isRemoved() && !p.hasDisconnected() && p.isCreative()
                && p.getMainHandItem().is(ModItems.TEST_FIREWORK);
    }
    private static boolean sameStack(ItemStack a, ItemStack b) {
        return b != null && a.getCount() == b.getCount() && ItemStack.isSameItemSameComponents(a, b);
    }
    private static void message(ServerPlayer p, String key) {
        p.sendSystemMessage(Component.translatable("editor.drawfirework." + key));
    }
    public static void initialize() {
        PayloadTypeRegistry.serverboundPlay().register(OpenEditorPayload.TYPE, OpenEditorPayload.CODEC);
        PayloadTypeRegistry.serverboundPlay().register(EditorCanvasPayload.SAVE, EditorCanvasPayload.CODEC);
        PayloadTypeRegistry.clientboundPlay().register(EditorCanvasPayload.OPENED_TYPE, EditorCanvasPayload.Opened.CODEC);
        ServerPlayNetworking.registerGlobalReceiver(OpenEditorPayload.TYPE, (payload, ctx) ->
                ctx.server().execute(() -> open(ctx.player())));
        ServerPlayNetworking.registerGlobalReceiver(EditorCanvasPayload.SAVE, (payload, ctx) ->
                ctx.server().execute(() -> save(ctx.player(), payload)));
        ServerTickEvents.END_SERVER_TICK.register(server -> STATES.forEach((p,s) -> {
            if (s.session != null && !s.current(p)) s.clear();
        }));
        ServerPlayConnectionEvents.DISCONNECT.register((handler,server) -> STATES.remove(handler.player));
        ServerLifecycleEvents.SERVER_STOPPED.register(server -> STATES.clear());
    }
    private static void open(ServerPlayer p) {
        State s = STATES.computeIfAbsent(p, ignored -> new State());
        if (!s.requests.admit(p.tickCount, true)) { message(p, "rate"); return; }
        s.clear();
        if (!eligible(p) || !ServerPlayNetworking.canSend(p, EditorCanvasPayload.OPENED_TYPE)) {
            message(p, "denied"); return;
        }
        ItemStack held = p.getMainHandItem();
        CanvasData original = held.get(ModDataComponents.CANVAS);
        try { if (original != null) ItemCanvasCodecs.validate(original); }
        catch (IllegalArgumentException e) { message(p, "invalid"); return; }
        UUID token = UUID.randomUUID();
        s.snapshot = held.copy(); s.level = p.level().dimension();
        s.session = new EditSession(token, held, p.getInventory().getSelectedSlot(), original, p.tickCount);
        CanvasData initial = original == null ? CanvasData.blank() : original;
        ServerPlayNetworking.send(p, new EditorCanvasPayload.Opened(token, initial));
    }
    private static void save(ServerPlayer p, EditorCanvasPayload payload) {
        State s = STATES.computeIfAbsent(p, ignored -> new State());
        if (!s.requests.admit(p.tickCount, false)) { message(p, "rate"); return; }
        ItemStack held = p.getMainHandItem();
        boolean success = s.session != null && s.session.save(payload.token(), p.tickCount,
                eligible(p) && s.level == p.level().dimension(), held, p.getInventory().getSelectedSlot(),
                held.get(ModDataComponents.CANVAS), sameStack(held, s.snapshot), payload.canvas(), () -> {
                    held.set(ModDataComponents.CANVAS, payload.canvas());
                    p.getInventory().setChanged();
                    p.containerMenu.broadcastChanges();
                });
        s.clear();
        message(p, success ? "saved" : "stale");
    }
}
