// net/shuyanmc/mpem/mixin/client/LevelRendererMixin.java
package net.shuyanmc.mpem.mixin.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.shuyanmc.mpem.engine.cull.CullingEngineManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

@Mixin(LevelRenderer.class)
public abstract class LevelRendererMixin {
    private final Set<BlockEntity> mpem$culledBlockEntities = new HashSet<>();

    @Inject(method = "setupRender", at = @At("HEAD"))
    private void mpem$onSetupRender(CallbackInfo ci) {
        // 更新可见区块
        CullingEngineManager.updateVisibleChunks(
                Minecraft.getInstance().options.getEffectiveRenderDistance()
        );
    }

    @Redirect(
            method = "renderLevel",
            at = @At(
                    value = "INVOKE",
                    target = "Ljava/util/Set;iterator()Ljava/util/Iterator;"
            )
    )
    private Iterator<BlockEntity> mpem$filterBlockEntities(Set<BlockEntity> original) {
        mpem$culledBlockEntities.clear();
        for (BlockEntity be : original) {
            if (CullingEngineManager.getInstance().shouldRenderBlockEntity(be)) {
                mpem$culledBlockEntities.add(be);
            }
        }
        return mpem$culledBlockEntities.iterator();
    }
}