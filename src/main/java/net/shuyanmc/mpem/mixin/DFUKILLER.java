package net.shuyanmc.mpem.mixin;

import com.mojang.datafixers.DataFixerBuilder;
import net.minecraft.util.datafix.DataFixers;
import net.shuyanmc.mpem.AsyncHandler;
import net.shuyanmc.mpem.LazyDataFixerBuilder;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
@AsyncHandler

@Mixin(value = DataFixers.class)
public class DFUKILLER {
    @Redirect(method = "createFixerUpper", at = @At(value = "NEW", target = "com/mojang/datafixers/DataFixerBuilder"))
private static DataFixerBuilder create$replaceBuilder(int dataVersion) {
        return new LazyDataFixerBuilder(dataVersion);

    }
}