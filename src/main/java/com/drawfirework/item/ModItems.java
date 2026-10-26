package com.drawfirework.item;

import com.drawfirework.DrawFirework;
import com.drawfirework.canvas.CanvasData;
import com.drawfirework.canvas.ItemCanvasCodecs;
import com.drawfirework.component.ModDataComponents;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public final class ModItems {
    private static final ResourceKey<Item> TEST_KEY = ResourceKey.create(Registries.ITEM,
            Identifier.fromNamespaceAndPath(DrawFirework.MOD_ID, "test_firework"));
    public static final Item TEST_FIREWORK = Registry.register(BuiltInRegistries.ITEM, TEST_KEY,
            new TestFireworkItem(new Item.Properties().setId(TEST_KEY).stacksTo(1)));

    private ModItems() {}
    public static void initialize() { /* Triggers registration. */ }

    public static ItemStack createTestStack(CanvasData canvas) {
        ItemCanvasCodecs.validate(canvas);
        ItemStack stack = new ItemStack(TEST_FIREWORK);
        stack.set(ModDataComponents.CANVAS, canvas);
        return stack;
    }
}
