package net.shuyanmc.mpem.events;

import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.shuyanmc.mpem.AsyncHandler;
import net.shuyanmc.mpem.config.CoolConfig;
@AsyncHandler
@Mod.EventBusSubscriber
public class EntitySyncHandler {
    
    @SubscribeEvent
    public static void onEntityJoinLevel(EntityJoinLevelEvent event) {
        if (!CoolConfig.reduceEntityUpdates.get()) return;
        
        if (event.getEntity() instanceof ServerPlayer player) {
            if (player.distanceToSqr(event.getEntity()) > 4096.0) {
                event.setCanceled(true);
            }
        }
    }
}