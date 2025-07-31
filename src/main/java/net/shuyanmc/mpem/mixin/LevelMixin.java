package net.shuyanmc.mpem.mixin;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.shuyanmc.mpem.EntityTickHelper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.function.Consumer;

@Mixin(Level.class)
public abstract class LevelMixin {
    @Inject(
            method = "guardEntityTick",
            at = @At("HEAD"),
            cancellable = true
    )
    private void onEntityTick(Consumer<Entity> consumer, Entity entity, CallbackInfo ci) {
        if (EntityTickHelper.shouldCancelTick(entity)) {
            ci.cancel();
        }
    }
}