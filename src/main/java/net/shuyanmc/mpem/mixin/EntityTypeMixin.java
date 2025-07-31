package net.shuyanmc.mpem.mixin;

import net.minecraft.world.entity.EntityType;
import net.shuyanmc.mpem.api.IOptimizableEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
@Mixin(EntityType.class)
public abstract class EntityTypeMixin implements IOptimizableEntity {
    @Unique private boolean alwaysTick;
    @Unique private boolean tickInRaid;

    @Override
    public boolean shouldAlwaysTick() {
        return this.alwaysTick;
    }

    @Override
    public void setAlwaysTick(boolean value) {
        this.alwaysTick = value;
    }

    @Override
    public boolean shouldTickInRaid() {
        return this.tickInRaid;
    }

    @Override
    public void setTickInRaid(boolean value) {
        this.tickInRaid = value;
    }
}