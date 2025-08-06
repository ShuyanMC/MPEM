// MinecraftMixin.java
package net.shuyanmc.mpem.mixin.client;

import net.minecraft.client.Minecraft;
import net.shuyanmc.mpem.FrameRateController;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Minecraft.class)
public class MinecraftMixin {
    @Inject(method = "getFramerateLimit", at = @At("HEAD"), cancellable = true)
    private void onGetFramerateLimit(CallbackInfoReturnable<Integer> cir) {
        Minecraft instance = Minecraft.getInstance();
        if (!instance.isWindowActive()) {
            cir.setReturnValue(FrameRateController.getInactiveFrameRateLimit());
        }
    }
}