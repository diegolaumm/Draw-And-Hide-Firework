package com.drawfirework.canvas;

import com.mojang.serialization.Codec;
import io.netty.handler.codec.DecoderException;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;

/** Item boundary only; does not reduce the draft/model limit. */
public final class ItemCanvasCodecs {
    public static final int MAX_SIZE = 32;
    public static final int MAX_PIXELS = MAX_SIZE * MAX_SIZE;
    public static final int MAX_NETWORK_BYTES = 3 + MAX_PIXELS * Integer.BYTES;
    public static final Codec<CanvasData> CODEC = CanvasSerialization.boundedCodec(MAX_SIZE);
    public static final StreamCodec<RegistryFriendlyByteBuf, CanvasData> STREAM_CODEC = StreamCodec.of(
            (buf, canvas) -> {
                validate(canvas);
                buf.writeByte(canvas.version());
                buf.writeByte(canvas.width());
                buf.writeByte(canvas.height());
                for (int y = 0; y < canvas.height(); y++) for (int x = 0; x < canvas.width(); x++)
                    buf.writeInt(canvas.pixel(x, y));
            }, buf -> {
                if (buf.readableBytes() < 3) throw new DecoderException("Truncated canvas header");
                int version = buf.readUnsignedByte();
                int width = buf.readUnsignedByte();
                int height = buf.readUnsignedByte();
                if (version != CanvasData.VERSION || width < 1 || height < 1 || width > MAX_SIZE || height > MAX_SIZE)
                    throw new DecoderException("Invalid item canvas version or dimensions");
                int count = width * height;
                if (count > MAX_PIXELS || buf.readableBytes() < count * Integer.BYTES)
                    throw new DecoderException("Invalid item canvas payload size");
                int[] pixels = new int[count];
                for (int i = 0; i < count; i++) pixels[i] = buf.readInt();
                // Following bytes belong to the containing ItemStack, not this component.
                return new CanvasData(width, height, pixels);
            });

    private ItemCanvasCodecs() {}

    public static CanvasData validate(CanvasData canvas) {
        if (canvas == null || canvas.width() < 1 || canvas.height() < 1
                || canvas.width() > MAX_SIZE || canvas.height() > MAX_SIZE
                || canvas.pixelCount() != (long) canvas.width() * canvas.height() || canvas.pixelCount() > MAX_PIXELS)
            throw new IllegalArgumentException("Item canvas must be 1..32 per dimension and at most 1024 pixels");
        return canvas;
    }
}
