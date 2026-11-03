package com.example.virtualhand;

import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.DropperBlock;
import net.minecraft.world.level.block.LeverBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

public final class MechanicalInterfaceDetector {
    private MechanicalInterfaceDetector() {
    }

    public static void register() {
        UseBlockCallback.EVENT.register(MechanicalInterfaceDetector::onUseBlock);
    }

    private static InteractionResult onUseBlock(Player player, Level level, InteractionHand hand, BlockHitResult hit) {
        // Singleplayer's integrated server fires this event too, so ignore everything but the client level.
        if (!level.isClientSide() || hand != InteractionHand.MAIN_HAND || player.isSpectator()) {
            return InteractionResult.PASS;
        }

        // Sneaking while holding an item uses the item instead of flipping the lever.
        boolean holdingItem = !player.getMainHandItem().isEmpty() || !player.getOffhandItem().isEmpty();
        if (player.isSecondaryUseActive() && holdingItem) {
            return InteractionResult.PASS;
        }

        BlockPos leverPos = hit.getBlockPos();
        BlockState leverState = level.getBlockState(leverPos);
        if (!(leverState.getBlock() instanceof LeverBlock)) {
            return InteractionResult.PASS;
        }

        // The callback runs before the lever toggles, so "currently off" means this click turns it on.
        boolean turningOn = !leverState.getValue(LeverBlock.POWERED);
        if (turningOn && isMountedOnDropper(level, leverPos, leverState)) {
            VirtualHandManager.addTargetToHand();
        }

        // PASS lets vanilla handle the lever normally.
        return InteractionResult.PASS;
    }

    private static boolean isMountedOnDropper(Level level, BlockPos leverPos, BlockState leverState) {
        BlockPos supportPos = leverPos.relative(getSupportDirection(leverState));
        return leverPos.distManhattan(supportPos) == 1
                && level.getBlockState(supportPos).getBlock() instanceof DropperBlock;
    }

    /** Returns the direction from the lever to the block it is mounted on. */
    private static Direction getSupportDirection(BlockState leverState) {
        return switch (leverState.getValue(LeverBlock.FACE)) {
            case FLOOR -> Direction.DOWN;
            case CEILING -> Direction.UP;
            case WALL -> leverState.getValue(LeverBlock.FACING).getOpposite();
        };
    }
}
