package com.drawfirework.network;

import com.drawfirework.canvas.CanvasData;
import com.drawfirework.canvas.ItemCanvasCodecs;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import java.util.UUID;

public record PlaybackPayload(UUID id, String dimension, double x, double y, double z, CanvasData canvas)
        implements CustomPacketPayload {
    public static final Type<PlaybackPayload> TYPE = new Type<>(Identifier.fromNamespaceAndPath("drawfirework","play_canvas"));
    public PlaybackPayload {
        if (id == null || dimension == null || dimension.length() > 128
                || Identifier.tryParse(dimension) == null || !valid(x) || !valid(y) || !valid(z))
            throw new IllegalArgumentException("Invalid playback event");
        ItemCanvasCodecs.validate(canvas);
    }
    private static boolean valid(double n) { return Double.isFinite(n) && Math.abs(n) <= 30_000_000; }
    public static final StreamCodec<RegistryFriendlyByteBuf, PlaybackPayload> CODEC = StreamCodec.of((b,p) -> {
        b.writeByte(1); b.writeLong(p.id.getMostSignificantBits()); b.writeLong(p.id.getLeastSignificantBits());
        b.writeUtf(p.dimension,128); b.writeDouble(p.x); b.writeDouble(p.y); b.writeDouble(p.z);
        ItemCanvasCodecs.STREAM_CODEC.encode(b,p.canvas);
    }, b -> {
        if (b.readableBytes() > 4654 || b.readUnsignedByte() != 1) throw new IllegalArgumentException("Invalid playback size/version");
        UUID id = new UUID(b.readLong(), b.readLong());
        String dimension = b.readUtf(128);
        double x=b.readDouble(), y=b.readDouble(), z=b.readDouble();
        // Validate metadata before allocating pixels.
        if (Identifier.tryParse(dimension)==null || !valid(x)||!valid(y)||!valid(z))
            throw new IllegalArgumentException("Invalid playback coordinates/dimension");
        var result = new PlaybackPayload(id,dimension,x,y,z,ItemCanvasCodecs.STREAM_CODEC.decode(b));
        if (b.readableBytes()!=0) throw new IllegalArgumentException("Trailing playback data");
        return result;
    });
    @Override public Type<PlaybackPayload> type() { return TYPE; }
}
