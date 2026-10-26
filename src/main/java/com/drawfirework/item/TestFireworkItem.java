package com.drawfirework.item;

import com.drawfirework.canvas.ItemCanvasCodecs;
import com.drawfirework.component.ModDataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;

/** Server-authorized Creative test playback; never renders on the server. */
public final class TestFireworkItem extends Item {
    public TestFireworkItem(Properties properties) { super(properties); }

    @Override public InteractionResult use(Level level, Player player, InteractionHand hand) {
        if (level.isClientSide()) return InteractionResult.PASS;
        if (!player.isCreative()) {
            player.sendSystemMessage(Component.literal("Test firework: Creative only; nothing consumed."));
            return InteractionResult.FAIL;
        }
        var canvas = player.getItemInHand(hand).get(ModDataComponents.CANVAS);
        try {
            ItemCanvasCodecs.validate(canvas);
        } catch (IllegalArgumentException e) {
            player.sendSystemMessage(Component.literal("Test firework: missing or invalid canvas; use refused."));
            return InteractionResult.FAIL;
        }
        if (!(player instanceof net.minecraft.server.level.ServerPlayer serverPlayer)
                || !com.drawfirework.network.ServerPlayback.use(serverPlayer, hand)) return InteractionResult.FAIL;
        return InteractionResult.SUCCESS_SERVER;
    }
}
