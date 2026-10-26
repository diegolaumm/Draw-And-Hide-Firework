package com.drawfirework.network;

import com.drawfirework.canvas.CanvasData;
import com.drawfirework.canvas.ItemCanvasCodecs;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import java.util.Objects;
import java.util.UUID;

/** Same bounded component format for server snapshot and C2S save, separate channel IDs. */
public record EditorCanvasPayload(UUID token, CanvasData canvas) implements CustomPacketPayload {
    public static final int MAX_BYTES = 1 + 16 + ItemCanvasCodecs.MAX_NETWORK_BYTES; // 4116
    public static final Type<EditorCanvasPayload> SAVE = new Type<>(Identifier.fromNamespaceAndPath("drawfirework", "save_canvas"));
    public static final Type<Opened> OPENED_TYPE = new Type<>(Identifier.fromNamespaceAndPath("drawfirework", "editor_opened"));
    public EditorCanvasPayload { Objects.requireNonNull(token); ItemCanvasCodecs.validate(canvas); }
    public static final StreamCodec<RegistryFriendlyByteBuf, EditorCanvasPayload> CODEC = StreamCodec.of((b,p) -> {
        b.writeByte(1); b.writeLong(p.token.getMostSignificantBits()); b.writeLong(p.token.getLeastSignificantBits());
        ItemCanvasCodecs.STREAM_CODEC.encode(b, p.canvas);
    }, b -> {
        if (b.readableBytes() < 20 || b.readableBytes() > MAX_BYTES || b.readUnsignedByte() != 1)
            throw new IllegalArgumentException("Invalid editor payload size/version");
        UUID token = new UUID(b.readLong(), b.readLong());
        var result = new EditorCanvasPayload(token, ItemCanvasCodecs.STREAM_CODEC.decode(b));
        if (b.readableBytes() != 0) throw new IllegalArgumentException("Trailing editor data");
        return result;
    });
    @Override public Type<EditorCanvasPayload> type() { return SAVE; }

    public record Opened(UUID token, CanvasData canvas) implements CustomPacketPayload {
        public Opened { Objects.requireNonNull(token); ItemCanvasCodecs.validate(canvas); }
        public static final StreamCodec<RegistryFriendlyByteBuf, Opened> CODEC = StreamCodec.of(
                (b,p) -> EditorCanvasPayload.CODEC.encode(b, new EditorCanvasPayload(p.token, p.canvas)),
                b -> { var p = EditorCanvasPayload.CODEC.decode(b); return new Opened(p.token, p.canvas); });
        @Override public Type<Opened> type() { return OPENED_TYPE; }
    }
}
