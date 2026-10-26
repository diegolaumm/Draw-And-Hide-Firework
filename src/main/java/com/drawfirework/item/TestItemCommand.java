package com.drawfirework.item;

import com.drawfirework.firework.DevCanvases;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;

public final class TestItemCommand {
    private TestItemCommand() {}

    public static void initialize() {
        CommandRegistrationCallback.EVENT.register((dispatcher, registry, environment) -> dispatcher.register(
                Commands.literal("drawfirework_test_item")
                        .requires(source -> source.getPlayer() != null && source.getPlayer().isCreative())
                        .then(Commands.literal("verify").executes(context -> {
                            var player = context.getSource().getPlayerOrException();
                            if (!player.isCreative()) return 0;
                            try {
                                ItemStackDiagnostics.verifyHeld(player.getMainHandItem());
                                ItemStackDiagnostics.verify(player.registryAccess());
                                player.sendSystemMessage(Component.literal("04C.1 ItemStack diagnostics PASS: Smiley/32x32, copy, NBT/JSON/stream, missing/invalid component."));
                                com.drawfirework.DrawFirework.LOGGER.info("04C.1 ItemStack diagnostics PASS");
                                return 1;
                            } catch (RuntimeException e) {
                                com.drawfirework.DrawFirework.LOGGER.error("04C.1 ItemStack diagnostics FAIL", e);
                                context.getSource().sendFailure(Component.literal("ItemStack diagnostics FAIL; see server log."));
                                return 0;
                            }
                        }))
                        .executes(context -> {
                            var player = context.getSource().getPlayerOrException();
                            // Check live server state again at execution, never trust client game mode.
                            if (!player.isCreative()) return 0;
                            int slot = player.getInventory().getFreeSlot();
                            if (slot < 0) {
                                context.getSource().sendFailure(Component.literal("Test firework: inventory full."));
                                return 0;
                            }
                            player.getInventory().setItem(slot, ModItems.createTestStack(DevCanvases.smiley()));
                            player.getInventory().setChanged();
                            player.containerMenu.broadcastChanges();
                            player.sendSystemMessage(Component.literal("Creative test firework granted (Smiley data)."));
                            return 1;
                        })));
    }
}
