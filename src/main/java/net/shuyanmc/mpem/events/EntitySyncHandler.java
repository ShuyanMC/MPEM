package net.shuyanmc.mpem.events;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.shuyanmc.mpem.AsyncHandler;
import net.shuyanmc.mpem.config.CoolConfig;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

@AsyncHandler
@Mod.EventBusSubscriber
public class EntitySyncHandler {
    private static final Cache<UUID, Long> SYNC_COOLDOWN_CACHE = Caffeine.newBuilder()
            .maximumSize(5000)
            .expireAfterWrite(10, TimeUnit.SECONDS)
            .build();
    private static final Cache<UUID, Long> PLAYER_POSITION_CACHE = Caffeine.newBuilder()
            .maximumSize(1000)
            .expireAfterWrite(30, TimeUnit.SECONDS)
            .build();

    @SubscribeEvent
    public static void onEntityJoinLevel(EntityJoinLevelEvent event) {
        if (!CoolConfig.reduceEntityUpdates.get()) {
            return;
        }
        Entity entity = event.getEntity();
        if (!(entity instanceof ServerPlayer)) {
            handleNonPlayerEntity(event);
            return;
        }

        ServerPlayer player = (ServerPlayer) entity;
        PLAYER_POSITION_CACHE.put(player.getUUID(), System.currentTimeMillis());
    }

    private static void handleNonPlayerEntity(EntityJoinLevelEvent event) {
        Entity entity = event.getEntity();
        long currentTime = System.currentTimeMillis();

        for (Player player : event.getLevel().players()) {
            if (PLAYER_POSITION_CACHE.getIfPresent(player.getUUID()) == null) {
                continue;
            }

            double distanceSqr = player.distanceToSqr(entity);

            if (distanceSqr <= 1024.0) {
                return;
            }
            if (distanceSqr <= 4096.0) {
                Long lastSync = SYNC_COOLDOWN_CACHE.getIfPresent(entity.getUUID());
                if (lastSync == null || currentTime - lastSync > 2000) { // 2秒冷却
                    SYNC_COOLDOWN_CACHE.put(entity.getUUID(), currentTime);
                    return;
                }
                break;
            }

            event.setCanceled(true);
            SYNC_COOLDOWN_CACHE.put(entity.getUUID(), currentTime);
            return;
        }
    }
}