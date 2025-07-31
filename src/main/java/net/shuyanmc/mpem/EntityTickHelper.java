package net.shuyanmc.mpem;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.raid.Raider;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.registries.ForgeRegistries;
import net.shuyanmc.mpem.api.IOptimizableEntity;
import net.shuyanmc.mpem.config.CoolConfig;

import java.util.Objects;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public final class EntityTickHelper {
    // 缓存物品白名单配置（配置不常变化）
    private static final Cache<String, Set<Item>> ITEM_WHITELIST_CACHE = Caffeine.newBuilder()
            .maximumSize(1)
            .expireAfterWrite(5, TimeUnit.MINUTES)
            .build();

    // 缓存实体类型判断结果（实体类型不会变化）
    private static final Cache<EntityType<?>, Boolean> BOSS_MOB_CACHE = Caffeine.newBuilder()
            .maximumSize(100)
            .build();

    public static boolean shouldCancelTick(Entity entity) {
        if (entity == null || entity.level() == null || !CoolConfig.optimizeEntities.get()) {
            return false;
        }
        if (entity instanceof Player) {
            return false;
        }

        if (!(entity instanceof LivingEntity livingEntity)) {
            return false;
        }

        if (!livingEntity.isAlive()) {
            return false;
        }

        final Level level = entity.level();
        final BlockPos pos = entity.blockPosition();

        if (isAlwaysTicking(entity)) {
            return false;
        }

        if (shouldBypassForRaid(entity, level, pos)) {
            return false;
        }
        if (isBossMob(livingEntity)) {
            return false;
        }
        return !isPlayerNearby(level, pos);
    }

    private static boolean isAlwaysTicking(Entity entity) {
        return entity instanceof IOptimizableEntity optimizable && optimizable.shouldAlwaysTick();
    }

    private static boolean shouldBypassForRaid(Entity entity, Level level, BlockPos pos) {
        if (!CoolConfig.tickRaidersInRaid.get()) {
            return false;
        }

        if (!(level instanceof ServerLevel serverLevel)) {
            return false;
        }

        if (!serverLevel.isRaided(pos)) {
            return false;
        }

        return entity instanceof Raider ||
                (entity instanceof IOptimizableEntity optimizable && optimizable.shouldTickInRaid());
    }

    private static boolean isBossMob(LivingEntity entity) {
        return BOSS_MOB_CACHE.get(entity.getType(), type ->
                type == EntityType.ENDER_DRAGON ||
                        type == EntityType.WITHER ||
                        type == EntityType.ELDER_GUARDIAN ||
                        type == EntityType.WARDEN ||
                        (entity instanceof Mob mob && mob.isNoAi())
        );
    }

    private static Set<Item> getItemWhitelist() {
        return ITEM_WHITELIST_CACHE.get("whitelist", k ->
                CoolConfig.itemWhitelist.get().stream()
                        .map(s -> ForgeRegistries.ITEMS.getValue(new ResourceLocation(s)))
                        .filter(Objects::nonNull)
                        .collect(Collectors.toSet())
        );
    }

    private static boolean isPlayerNearby(Level level, BlockPos pos) {
        final int horizontalRange = CoolConfig.horizontalRange.get();
        final int verticalRange = CoolConfig.verticalRange.get();

        AABB checkArea = new AABB(
                pos.getX() - horizontalRange,
                pos.getY() - verticalRange,
                pos.getZ() - horizontalRange,
                pos.getX() + horizontalRange,
                pos.getY() + verticalRange,
                pos.getZ() + horizontalRange
        );

        return level.getEntitiesOfClass(Player.class, checkArea).stream()
                .anyMatch(Player::isAlive);
    }
}