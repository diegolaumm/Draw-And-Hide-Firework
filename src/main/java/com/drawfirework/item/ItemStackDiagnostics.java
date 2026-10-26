package com.drawfirework.item;

import com.drawfirework.canvas.CanvasData;
import com.drawfirework.component.ModDataComponents;
import com.drawfirework.firework.DevCanvases;
import com.mojang.serialization.JsonOps;
import io.netty.buffer.Unpooled;
import net.minecraft.core.RegistryAccess;
import net.minecraft.nbt.NbtOps;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.resources.RegistryOps;
import net.minecraft.world.item.ItemStack;

/** Explicit server-command integration probe using the live game's registry context. */
public final class ItemStackDiagnostics {
    private ItemStackDiagnostics() {}

    public static void verifyHeld(ItemStack stack) {
        require(stack.is(ModItems.TEST_FIREWORK), "hold the granted test firework in your main hand");
        require(DevCanvases.smiley().equals(stack.get(ModDataComponents.CANVAS)), "held Smiley canvas (including after reload)");
    }

    public static void verify(RegistryAccess registries) {
        for (CanvasData canvas : new CanvasData[]{DevCanvases.smiley(), CanvasData.blank()}) {
            var stack = ModItems.createTestStack(canvas);
            require(canvas.equals(stack.get(ModDataComponents.CANVAS)), "stack write/read");
            var copy = stack.copy();
            var exposed = copy.get(ModDataComponents.CANVAS).pixels();
            exposed[0] ^= 0xffffffff;
            require(canvas.equals(copy.get(ModDataComponents.CANVAS)), "copy array isolation");
            copy.remove(ModDataComponents.CANVAS);
            require(canvas.equals(stack.get(ModDataComponents.CANVAS)), "copy component isolation");
            var nbtOps = RegistryOps.create(NbtOps.INSTANCE, registries);
            var nbt = ItemStack.CODEC.encodeStart(nbtOps, stack).getOrThrow();
            var loaded = ItemStack.CODEC.parse(nbtOps, nbt).getOrThrow();
            require(stack.getItem() == loaded.getItem() && stack.getCount() == loaded.getCount()
                    && canvas.equals(loaded.get(ModDataComponents.CANVAS)), "ItemStack NBT roundtrip");
            var buf = new RegistryFriendlyByteBuf(Unpooled.buffer(), registries);
            try {
                ItemStack.STREAM_CODEC.encode(buf, stack);
                var decoded = ItemStack.STREAM_CODEC.decode(buf);
                require(decoded.getItem() == stack.getItem() && decoded.getCount() == stack.getCount()
                        && canvas.equals(decoded.get(ModDataComponents.CANVAS)) && buf.readableBytes() == 0,
                        "ItemStack stream roundtrip");
            } finally { buf.release(); }
            var jsonOps = RegistryOps.create(JsonOps.INSTANCE, registries);
            var encoded = ItemStack.CODEC.encodeStart(jsonOps, stack).getOrThrow().getAsJsonObject();
            require(canvas.equals(ItemStack.CODEC.parse(jsonOps, encoded).getOrThrow().get(ModDataComponents.CANVAS)),
                    "ItemStack JSON roundtrip");
            var component = encoded.getAsJsonObject("components").getAsJsonObject("drawfirework:canvas");
            component.addProperty("width", 33);
            require(ItemStack.CODEC.parse(jsonOps, encoded).isError(), "ItemStack invalid dimension rejection");
            component.addProperty("width", canvas.width());
            component.addProperty("pixels", "0".repeat(8193));
            require(ItemStack.CODEC.parse(jsonOps, encoded).isError(), "ItemStack oversized payload rejection");
        }
        var missing = new ItemStack(ModItems.TEST_FIREWORK);
        require(missing.get(ModDataComponents.CANVAS) == null, "missing component");
    }

    private static void require(boolean condition, String name) {
        if (!condition) throw new IllegalStateException("ItemStack diagnostic failed: " + name);
    }
}
