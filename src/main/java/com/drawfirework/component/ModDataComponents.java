package com.drawfirework.component;

import com.drawfirework.DrawFirework;
import com.drawfirework.canvas.CanvasData;
import com.drawfirework.canvas.ItemCanvasCodecs;
import net.minecraft.core.Registry;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;

public final class ModDataComponents {
    public static final DataComponentType<CanvasData> CANVAS = Registry.register(
            BuiltInRegistries.DATA_COMPONENT_TYPE, Identifier.fromNamespaceAndPath(DrawFirework.MOD_ID, "canvas"),
            DataComponentType.<CanvasData>builder().persistent(ItemCanvasCodecs.CODEC)
                    .networkSynchronized(ItemCanvasCodecs.STREAM_CODEC).build());

    private ModDataComponents() {}
    public static void initialize() { /* Triggers registration before items. */ }
}
