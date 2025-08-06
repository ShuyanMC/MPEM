package net.shuyanmc.mpem.util;

import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.monster.Monster;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber
public class EntityCleaner {

    @SubscribeEvent
    public static void onCommandRegister(RegisterCommandsEvent event) {
        event.getDispatcher().register(
                Commands.literal("killmobs")
                        .requires(source -> {
                            // 单人模式且启用作弊，或者多人服务器有4级权限
                            return source.hasPermission(4) ||
                                    (!source.getServer().isDedicatedServer() && source.hasPermission(2));
                        })
                        .executes(context -> {
                            int[] count = {0};
                            context.getSource().getServer().getAllLevels().forEach(level -> {
                                level.getAllEntities().forEach(entity -> {
                                    if (entity instanceof Monster) {
                                        entity.discard();
                                        count[0]++;
                                    }
                                });
                            });
                            context.getSource().sendSuccess(() ->
                                            Component.literal("已清除 " + count[0] + " 个怪物实体"),
                                    true
                            );
                            return count[0];
                        })
        );
    }
}