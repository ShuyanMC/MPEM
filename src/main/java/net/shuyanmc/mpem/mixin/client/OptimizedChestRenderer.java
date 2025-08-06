package net.shuyanmc.mpem.mixin.client;

import net.minecraft.client.renderer.blockentity.BlockEntityRenderDispatcher;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.level.block.entity.EnderChestBlockEntity;
import net.shuyanmc.mpem.config.CoolConfig;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(BlockEntityRenderDispatcher.class)
public abstract class OptimizedChestRenderer {
    @Inject(method = "getRenderer", at = @At("HEAD"), cancellable = true)
    private <E extends BlockEntity> void handleRendering(
            E entity,
            CallbackInfoReturnable<BlockEntityRenderer<E>> cir) {

        if (!CoolConfig.ENABLE_OPTIMIZATION.get() ||
                entity == null ||
                entity.getLevel() == null ||
                entity.isRemoved()) {
            return;
        }

        // Handle both regular and ender chests based on config
        boolean isChest = entity instanceof ChestBlockEntity;
        boolean isEnderChest = entity instanceof EnderChestBlockEntity;

        if (!isChest && !isEnderChest) return;
        if (isChest && !CoolConfig.ENABLE_OPTIMIZATION.get()) return;
        if (isEnderChest && !CoolConfig.OPTIMIZE_ENDER_CHESTS.get()) return;

        try {
            BlockPos pos = entity.getBlockPos();
            Level level = entity.getLevel();

            if (level == null || pos == null) {
                logDebug("Invalid level or position for chest at " + pos);
                return;
            }

            Player player = getNearestPlayerSafely(level, pos);
            if (player == null) {
                cir.setReturnValue(null);
                return;
            }

            if (isBeyondRenderDistance(pos, player.blockPosition())) {
                cir.setReturnValue(null);
                logDebug("Skipped distant chest at " + pos);
            }
        } catch (Exception e) {
            logDebug("Error optimizing chest: " + e.getMessage());
        }
    }

    private Player getNearestPlayerSafely(Level level, BlockPos pos) {
        try {
            return level.getNearestPlayer(
                    pos.getX(), pos.getY(), pos.getZ(),
                    CoolConfig.MAX_RENDER_DISTANCE.get() * 16,
                    false
            );
        } catch (Exception e) {
            logDebug("Player detection failed: " + e.getMessage());
            return null;
        }
    }

    private boolean isBeyondRenderDistance(BlockPos pos, BlockPos playerPos) {
        double maxDistance = CoolConfig.MAX_RENDER_DISTANCE.get() * 16;
        return pos.distSqr(playerPos) > (maxDistance * maxDistance);
    }

    private void logDebug(String message) {
        if (CoolConfig.DEBUG_LOGGING.get()) {
            System.out.println("[MPEM] " + message);
        }
    }
}