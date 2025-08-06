package net.shuyanmc.mpem.mixin;
import net.minecraft.server.level.ServerLevel;
import net.shuyanmc.mpem.AsyncHandler;
import net.shuyanmc.mpem.config.CoolConfig;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
@AsyncHandler
@Mixin(ServerLevel.class)
public abstract class MixinServerLevel {
    @ModifyVariable(method = "tickChunk", at = @At("HEAD"), argsOnly = true)
    private int modifyChunkLoadRate(int chunks) {
        return Math.min(chunks, CoolConfig.chunkUnloadDelay.get());
    }
}