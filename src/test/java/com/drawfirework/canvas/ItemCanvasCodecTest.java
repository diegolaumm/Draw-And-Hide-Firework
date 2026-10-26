package com.drawfirework.canvas;

import com.drawfirework.firework.DevCanvases;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.serialization.JsonOps;
import io.netty.buffer.Unpooled;
import io.netty.handler.codec.DecoderException;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.nbt.NbtOps;
import org.junit.Test;
import static org.junit.Assert.*;

public class ItemCanvasCodecTest {
    private static JsonObject json() {
        return JsonParser.parseString(CanvasSerialization.toJson(DevCanvases.smiley())).getAsJsonObject();
    }
    private static RegistryFriendlyByteBuf buffer() {
        return new RegistryFriendlyByteBuf(Unpooled.buffer(), RegistryAccess.EMPTY);
    }
    private static void rejects(JsonObject input) {
        assertTrue(ItemCanvasCodecs.CODEC.parse(JsonOps.INSTANCE, input).isError());
    }

    @Test public void persistentRoundTripPreservesEveryArgb() {
        var canvas = DevCanvases.smiley();
        var encoded = ItemCanvasCodecs.CODEC.encodeStart(JsonOps.INSTANCE, canvas).getOrThrow();
        assertEquals(canvas, ItemCanvasCodecs.CODEC.parse(JsonOps.INSTANCE, encoded).getOrThrow());
    }
    @Test public void persistentNbtBoundaryUsesTheSameProductCodec() {
        var canvas = DevCanvases.smiley();
        var encoded = ItemCanvasCodecs.CODEC.encodeStart(NbtOps.INSTANCE, canvas).getOrThrow();
        assertEquals(canvas, ItemCanvasCodecs.CODEC.parse(NbtOps.INSTANCE, encoded).getOrThrow());
        var oversized = new CanvasData(33, 1, new int[33]);
        var generalNbt = CanvasSerialization.CODEC.encodeStart(NbtOps.INSTANCE, oversized).getOrThrow();
        assertTrue(ItemCanvasCodecs.CODEC.parse(NbtOps.INSTANCE, generalNbt).isError());
    }
    @Test public void exact32BoundaryAndGeneral256RemainValid() {
        var canvas = new CanvasData(32, 32, new int[1024]);
        assertEquals(canvas, ItemCanvasCodecs.CODEC.parse(JsonOps.INSTANCE,
                ItemCanvasCodecs.CODEC.encodeStart(JsonOps.INSTANCE, canvas).getOrThrow()).getOrThrow());
        assertEquals(65536, new CanvasData(256, 256, new int[65536]).pixelCount());
    }
    @Test public void oversizedEncodeAndDecodeAreRejected() {
        for (var c : new CanvasData[]{new CanvasData(33, 1, new int[33]),
                new CanvasData(1, 33, new int[33]), new CanvasData(256, 256, new int[65536])}) {
            assertTrue(ItemCanvasCodecs.CODEC.encodeStart(JsonOps.INSTANCE, c).isError());
            rejects(JsonParser.parseString(CanvasSerialization.toJson(c)).getAsJsonObject());
            assertThrows(IllegalArgumentException.class, () -> ItemCanvasCodecs.validate(c));
        }
    }
    @Test public void malformedDimensionsAndVersionAreRejected() {
        for (String key : new String[]{"width", "height"}) for (String value : new String[]{"0", "-1", "33", "2147483647", "4294967297", "1.5"}) {
            var input = json(); input.add(key, JsonParser.parseString(value)); rejects(input);
        }
        var input = json(); input.addProperty("version", 2); rejects(input);
        input = json(); input.remove("width"); rejects(input);
        input = json(); input.addProperty("format", "other"); rejects(input);
    }
    @Test public void malformedAndOversizedPixelStringsAreRejected() {
        for (String pixels : new String[]{"", "00000000", "g".repeat(2048), "0".repeat(8193)}) {
            var input = json(); input.addProperty("pixels", pixels); rejects(input);
        }
        assertThrows(IllegalArgumentException.class, () -> new CanvasData(2, 2, new int[3]));
        assertThrows(IllegalArgumentException.class, () -> ItemCanvasCodecs.validate(null));
    }
    @Test public void streamRoundTripHasFixedUpperBoundAndPreservesFollowingField() {
        int[] pixels = new int[1024];
        for (int i = 0; i < pixels.length; i++) pixels[i] = i * 0x1020304;
        var canvas = new CanvasData(32, 32, pixels);
        var buf = buffer();
        try {
            ItemCanvasCodecs.STREAM_CODEC.encode(buf, canvas);
            assertEquals(4099, buf.readableBytes());
            assertEquals(ItemCanvasCodecs.MAX_NETWORK_BYTES, buf.readableBytes());
            buf.writeInt(12345);
            assertEquals(canvas, ItemCanvasCodecs.STREAM_CODEC.decode(buf));
            assertEquals(12345, buf.readInt());
            assertEquals(0, buf.readableBytes());
        } finally { buf.release(); }
    }
    @Test public void streamRejectsBadHeadersBeforePixels() {
        for (int[] header : new int[][]{{2,1,1},{1,0,1},{1,1,0},{1,33,1},{1,1,255},{1,255,255}}) {
            var buf = buffer();
            try {
                for (int value : header) buf.writeByte(value);
                assertThrows(DecoderException.class, () -> ItemCanvasCodecs.STREAM_CODEC.decode(buf));
                assertEquals(3, buf.readerIndex());
            } finally { buf.release(); }
        }
    }
    @Test public void streamRejectsTruncationAndOversizedEncoding() {
        for (int size : new int[]{0,1,2,3,6}) {
            var buf = buffer();
            try {
                byte[] truncated = {1,1,1,0,0,0}; buf.writeBytes(truncated, 0, size);
                assertThrows(DecoderException.class, () -> ItemCanvasCodecs.STREAM_CODEC.decode(buf));
            } finally { buf.release(); }
        }
        var buf = buffer();
        try {
            assertThrows(IllegalArgumentException.class, () -> ItemCanvasCodecs.STREAM_CODEC.encode(buf,
                    new CanvasData(33,1,new int[33])));
            assertEquals(0, buf.writerIndex());
        } finally { buf.release(); }
    }
}
