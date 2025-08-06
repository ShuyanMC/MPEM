package net.shuyanmc.mpem.client;

import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.client.event.RenderNameTagEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.shuyanmc.mpem.AsyncHandler;

@AsyncHandler
public class ItemCountRenderer {
    @SubscribeEvent(priority = EventPriority.LOW)
    public static void onNameTagRender(RenderNameTagEvent event) {
        if (!(event.getEntity() instanceof ItemEntity)) {
            return;
        }

        ItemEntity itemEntity = (ItemEntity) event.getEntity();
        ItemStack stack = itemEntity.getItem();

        if (stack.getCount() <= 1) {
            return;
        }

        Component customName = itemEntity.getCustomName();
        if (customName != null) {
            event.setContent(customName);
        }
    }
}