package net.shuyanmc.mpem;

import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModContainer;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.common.Mod;

import java.lang.reflect.Method;
import java.util.function.Consumer;

public class FastEventSubscriber {

    public static void registerFastEvents(Object target, IEventBus bus) {
        for (Method method : target.getClass().getDeclaredMethods()) {
            if (method.isAnnotationPresent(SubscribeEvent.class)) {
                Class<?>[] params = method.getParameterTypes();
                if (params.length == 1 && Event.class.isAssignableFrom(params[0])) {
                    @SuppressWarnings("unchecked")
                    Class<? extends Event> eventType = (Class<? extends Event>) params[0];
                    SubscribeEvent annotation = method.getAnnotation(SubscribeEvent.class);
                    Consumer<? extends Event> listener = createOptimizedListener(target, method);
                    bus.addListener(
                            annotation.priority(),
                            annotation.receiveCanceled(),
                            eventType,
                            (Consumer) listener
                    );
                }
            }
        }
    }

    private static <T extends Event> Consumer<T> createOptimizedListener(Object target, Method method) {
        return event -> {
            try {
                method.invoke(target, event);
            } catch (Exception e) {
                throw new RuntimeException("Error handling event", e);
            }
        };
    }

    public static void registerFastModEvents(Object modInstance, IEventBus eventBus) {
        String modId = getModId(modInstance.getClass());
        if (modId == null) return;

        ModContainer modContainer = ModList.get().getModContainerById(modId)
                .orElseThrow(() -> new IllegalStateException("Mod container not found"));
        eventBus.register(modInstance);
    }

    private static String getModId(Class<?> clazz) {
        Mod modAnnotation = clazz.getAnnotation(Mod.class);
        return modAnnotation != null ? modAnnotation.value() : null;
    }
}