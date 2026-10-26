package com.drawfirework.canvas;

import com.google.gson.Strictness;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.codecs.PrimitiveCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.io.IOException;
import java.io.StringReader;
import java.math.BigDecimal;
import java.util.HashSet;
import java.util.HexFormat;

/** Bounded JSON boundary plus a persistent Codec. No file or network I/O. */
public final class CanvasSerialization {
    public static final int MAX_JSON_BYTES = 600_000;
    public static final String FORMAT = "argb32-hex";
    private static final HexFormat HEX = HexFormat.of();
    // Codec.INT uses Number.intValue(), which can truncate fractional/overflowing JSON numbers.
    private static final Codec<Integer> EXACT_INT = new PrimitiveCodec<>() {
        @Override public <T> DataResult<Integer> read(DynamicOps<T> ops, T input) {
            return ops.getNumberValue(input).flatMap(number -> {
                try {
                    String text = number.toString();
                    if (text.length() > 32) return DataResult.error(() -> "Integer token too long");
                    return DataResult.success(new BigDecimal(text).intValueExact());
                } catch (NumberFormatException | ArithmeticException e) {
                    return DataResult.error(() -> "Expected exact 32-bit integer");
                }
            });
        }
        @Override public <T> T write(DynamicOps<T> ops, Integer value) { return ops.createInt(value); }
    };
    private record Wire(int version, int width, int height, String format, String pixels) {}
    /** Same versioned format, with a smaller allocation bound for item components. */
    public static Codec<CanvasData> boundedCodec(int maxSize) {
        if (maxSize < 1 || maxSize > CanvasData.MAX_SIZE) throw new IllegalArgumentException("Invalid codec size limit");
        Codec<Integer> dimension = EXACT_INT.validate(n -> n >= 1 && n <= maxSize
                ? DataResult.success(n) : DataResult.error(() -> "Canvas dimension exceeds codec limit"));
        Codec<Wire> wireCodec = RecordCodecBuilder.create(i -> i.group(
            EXACT_INT.fieldOf("version").forGetter(Wire::version),
            dimension.fieldOf("width").forGetter(Wire::width),
            dimension.fieldOf("height").forGetter(Wire::height),
            Codec.string(0, FORMAT.length()).fieldOf("format").forGetter(Wire::format),
            Codec.string(0, maxSize * maxSize * 8).fieldOf("pixels").forGetter(Wire::pixels)
    ).apply(i, Wire::new));
        return wireCodec.flatXmap(w -> {
        try { return DataResult.success(decode(w)); }
        catch (IllegalArgumentException e) { return DataResult.error(e::getMessage); }
        }, c -> c.width() <= maxSize && c.height() <= maxSize
                ? DataResult.success(wire(c)) : DataResult.error(() -> "Canvas dimension exceeds codec limit"));
    }
    public static final Codec<CanvasData> CODEC = boundedCodec(CanvasData.MAX_SIZE);

    private CanvasSerialization() {}
    private static Wire wire(CanvasData c) {
        StringBuilder hex = new StringBuilder(c.pixelCount() * 8);
        for (int y = 0; y < c.height(); y++) for (int x = 0; x < c.width(); x++)
            hex.append(HEX.toHexDigits(c.pixel(x, y)));
        return new Wire(c.version(), c.width(), c.height(), FORMAT, hex.toString());
    }
    private static CanvasData decode(Wire w) {
        if (w.version != CanvasData.VERSION || !FORMAT.equals(w.format))
            throw new IllegalArgumentException("Unsupported canvas version or format");
        int count = CanvasData.checkedArea(w.width, w.height);
        if (w.pixels.length() != count * 8) throw new IllegalArgumentException("Incorrect encoded pixel length");
        int[] pixels = new int[count];
        for (int p = 0; p < count; p++) pixels[p] = HEX.fromHexDigits(w.pixels, p * 8, p * 8 + 8);
        return new CanvasData(w.width, w.height, pixels);
    }

    public static String toJson(CanvasData c) {
        Wire w = wire(c);
        return "{\"version\":1,\"width\":" + w.width + ",\"height\":" + w.height
                + ",\"format\":\"" + FORMAT + "\",\"pixels\":\"" + w.pixels + "\"}";
    }

    /** ASCII-only format: character count equals UTF-8 byte count. Reject before parsing. */
    public static CanvasData fromJson(String json) {
        if (json == null || json.length() > MAX_JSON_BYTES) throw new IllegalArgumentException("JSON exceeds size limit");
        for (int i = 0; i < json.length(); i++) if (json.charAt(i) > 127)
            throw new IllegalArgumentException("Canvas JSON must be ASCII");
        try (JsonReader r = new JsonReader(new StringReader(json))) {
            r.setStrictness(Strictness.STRICT);
            r.beginObject();
            var seen = new HashSet<String>();
            int version = 0, width = 0, height = 0;
            String format = "", pixels = "";
            while (r.hasNext()) {
                String key = r.nextName();
                if (!seen.add(key)) throw new IllegalArgumentException("Duplicate field: " + key);
                switch (key) {
                    case "version" -> version = integer(r);
                    case "width" -> width = integer(r);
                    case "height" -> height = integer(r);
                    case "format" -> format = string(r);
                    case "pixels" -> pixels = string(r);
                    default -> throw new IllegalArgumentException("Unknown canvas field");
                }
            }
            r.endObject();
            if (seen.size() != 5 || r.peek() != JsonToken.END_DOCUMENT)
                throw new IllegalArgumentException("Missing fields or trailing JSON");
            return decode(new Wire(version, width, height, format, pixels));
        } catch (IOException | IllegalStateException e) {
            throw new IllegalArgumentException("Invalid canvas JSON", e);
        }
    }
    private static String string(JsonReader r) throws IOException {
        if (r.peek() != JsonToken.STRING) throw new IllegalArgumentException("Expected string");
        return r.nextString();
    }
    private static int integer(JsonReader r) throws IOException {
        if (r.peek() != JsonToken.NUMBER) throw new IllegalArgumentException("Expected integer");
        return Integer.parseInt(r.nextString());
    }
}
